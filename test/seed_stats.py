#!/usr/bin/env python3
"""
Fashion ERP Stats Seeder
Generates historical stats, predictions, and reports by calling the server-side
test endpoints.

What gets generated
───────────────────
  DAILY  (one call per day): sales stats, employee performance stats, stock stats
  MONTHLY (current month only): financial stats, sales stats, employee stats
    → The server has no endpoint for past-month financial stats; the scheduler
      runs those on the 1st of each month.  Run this script monthly, or trigger
      the monthly cron manually via POST /api/v1/test/stats/trigger-monthly-cron.
  PREDICTIONS: daily sales forecast (7-day horizon) + monthly financial forecast
    → Requires at least 10 days / 10 months of stats to already exist.
  REPORTS: FINANCIAL, SALES, EMPLOYEE_PERFORMANCE (MONTHLY period)
    → Generated synchronously; may take 30-120 s each due to LLM + PDF rendering.
    → Only available when the server runs in the "dev" Spring profile.

Usage
─────
  python3 seed_stats.py --base-url http://192.168.11.200:8080
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --days 365
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --from 2024-01-01 --to 2024-12-31
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --concurrency 16
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --skip-predictions --skip-reports
"""

import sys
import argparse
import time
import threading
from datetime import date, timedelta
from concurrent.futures import ThreadPoolExecutor, as_completed

try:
    import requests
except ImportError:
    print("Missing dependency: pip install requests")
    sys.exit(1)

# ─── Config ───────────────────────────────────────────────────────────────────

ADMIN_EMAIL    = "admin@gmail.com"
ADMIN_PASSWORD = "adminadmin"

# ─── Console helpers ──────────────────────────────────────────────────────────

GREEN  = "\033[92m"
YELLOW = "\033[93m"
RED    = "\033[91m"
CYAN   = "\033[96m"
RESET  = "\033[0m"

_print_lock = threading.Lock()

def ok(msg):
    with _print_lock:
        print(f"  {GREEN}✓{RESET} {msg}")

def warn(msg):
    with _print_lock:
        print(f"  {YELLOW}⚠{RESET}  {msg}")

def err(msg):
    with _print_lock:
        print(f"  {RED}✗{RESET} {msg}")

def section(title):
    with _print_lock:
        print(f"\n{CYAN}{'─'*60}{RESET}\n{CYAN}  {title}{RESET}\n{CYAN}{'─'*60}{RESET}")


# ─── Seeder ───────────────────────────────────────────────────────────────────

class StatsSeeder:
    def __init__(self, base_url: str, concurrency: int = 8):
        self.root          = base_url.rstrip("/")               # e.g. http://host:8080
        self.base          = self.root + "/api/v1"              # versioned API prefix
        self.concurrency   = concurrency
        self._session      = requests.Session()
        self._cookies: dict[str, str] = {}
        self._cookie_gen   = 0          # bumped on every token refresh
        self._refresh_lock = threading.Lock()

    # ── auth ─────────────────────────────────────────────────────────────────

    def login(self):
        section("Authentication")
        resp = self._session.post(
            f"{self.base}/auth/login",
            json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
        )
        if not resp.ok:
            raise RuntimeError(f"Login → {resp.status_code}: {resp.text[:200]}")
        # Copy cookies from response directly (works even for localhost, which
        # http.cookiejar otherwise drops because the domain has no dot).
        for cookie in resp.cookies:
            self._session.cookies.set(cookie.name, cookie.value)
            self._cookies[cookie.name] = cookie.value
        ok(f"Logged in as {ADMIN_EMAIL}")

    # ── session factory ───────────────────────────────────────────────────────

    def _new_session(self) -> requests.Session:
        """Return a fresh Session pre-loaded with the current auth cookies."""
        s = requests.Session()
        for name, value in self._cookies.items():
            s.cookies.set(name, value)
        return s

    # ── token refresh ─────────────────────────────────────────────────────────

    def _refresh_token(self) -> None:
        """Refresh the access token and update the shared cookie snapshot.
        Must be called while holding _refresh_lock."""
        resp = self._session.post(f"{self.base}/auth/refresh")
        if not resp.ok:
            raise RuntimeError(f"Token refresh failed: HTTP {resp.status_code}")
        for cookie in resp.cookies:
            self._session.cookies.set(cookie.name, cookie.value)
            self._cookies[cookie.name] = cookie.value
        self._cookie_gen += 1
        ok("Access token refreshed")

    # ── single-day worker ─────────────────────────────────────────────────────

    def _seed_day(self, d: date) -> tuple[date, bool, str]:
        """POST run-today-stats for one date.  Returns (date, ok, error_msg).
        On 403 refreshes the token once and retries."""
        gen      = self._cookie_gen
        s        = self._new_session()
        date_str = d.strftime("%Y-%m-%d")
        url      = f"{self.base}/test/stats/run-today-stats/{date_str}"
        try:
            resp = s.post(url)
            if resp.status_code == 403:
                with self._refresh_lock:
                    if self._cookie_gen == gen:   # first thread to notice expiry
                        self._refresh_token()
                s    = self._new_session()        # pick up updated cookies
                resp = s.post(url)
            if not resp.ok:
                return d, False, f"HTTP {resp.status_code}: {resp.text[:120]}"
            return d, True, ""
        except Exception as exc:
            return d, False, str(exc)

    # ── daily range ───────────────────────────────────────────────────────────

    def seed_daily(self, start: date, end: date) -> int:
        """Generate DAILY stats for every day in [start, end].
        Returns the number of failures."""
        days  = [start + timedelta(days=i) for i in range((end - start).days + 1)]
        total = len(days)
        section(
            f"Daily stats  {start} → {end}  "
            f"({total} days, {self.concurrency} concurrent)"
        )

        done = errors = 0
        t0   = time.monotonic()

        with ThreadPoolExecutor(max_workers=self.concurrency) as pool:
            futures = {pool.submit(self._seed_day, d): d for d in days}
            for future in as_completed(futures):
                d, success, msg = future.result()
                done += 1

                if not success:
                    errors += 1
                    warn(f"{d}  failed: {msg}")

                if done % 50 == 0 or done == total:
                    elapsed = time.monotonic() - t0
                    rate    = done / elapsed if elapsed > 0 else 0
                    eta     = (total - done) / rate if rate > 0 else 0
                    ok(
                        f"Progress {done:>4}/{total}  "
                        f"({rate:.1f} req/s  ETA {eta:.0f}s)"
                    )

        ok(f"Daily done — {total - errors} succeeded, {errors} failed")
        return errors

    # ── monthly (current month only) ──────────────────────────────────────────

    def seed_monthly(self):
        """Generate MONTHLY stats for the current calendar month.
        Includes: financial, sales, employee performance stats.
        NOTE: The server has no endpoint to generate monthly stats for arbitrary
        past months; those are handled by the scheduler on the 1st of each month.
        To back-fill past months manually, trigger the monthly cron:
          POST /api/v1/test/stats/trigger-monthly-cron
        (it always targets the previous calendar month)."""
        section("Monthly stats (current month)")
        try:
            resp = self._session.post(f"{self.base}/test/stats/run-current-month")
            if not resp.ok:
                warn(f"Monthly stats failed: HTTP {resp.status_code}: {resp.text[:120]}")
            else:
                ok("Financial + sales + employee stats saved for current month")
        except Exception as exc:
            warn(f"Monthly stats request failed: {exc}")

    # ── predictions ───────────────────────────────────────────────────────────

    def seed_predictions(self):
        """Trigger daily and monthly prediction generation.
        Daily predictions: 7-day sales forecast (requires ≥10 days of daily stats).
        Monthly predictions: 3-month financial forecast (requires ≥10 months of stats)."""
        section("Predictions")
        for label, path in [
            ("Daily  (sales 7-day forecast)",         "/test/predictions/run-daily"),
            ("Monthly (financial 3-month forecast)",  "/test/predictions/run-monthly"),
        ]:
            try:
                resp = self._session.post(f"{self.base}{path}", timeout=120)
                if resp.ok:
                    ok(label)
                else:
                    warn(f"{label}  →  HTTP {resp.status_code}: {resp.text[:120]}")
            except Exception as exc:
                warn(f"{label}  →  {exc}")

    # ── reports ───────────────────────────────────────────────────────────────

    def seed_reports(self):
        """Generate FINANCIAL, SALES, and EMPLOYEE_PERFORMANCE monthly reports.
        Each call blocks until the PDF is rendered (LLM + Thymeleaf → MinIO upload).
        Uses a 5-minute timeout per report.
        Only works when the server runs with the 'dev' Spring profile."""
        section("Reports  (dev profile only — LLM + PDF generation, ~30-120 s each)")
        for category in ("FINANCIAL", "SALES", "EMPLOYEE_PERFORMANCE"):
            try:
                resp = self._session.post(
                    f"{self.root}/test/api/report",
                    json=category,              # Jackson expects a JSON string for enums
                    timeout=300,
                )
                if resp.ok:
                    ok(f"{category} report generated")
                elif resp.status_code == 404:
                    warn(f"{category} report  →  404 (server not running in dev profile?)")
                else:
                    warn(f"{category} report  →  HTTP {resp.status_code}: {resp.text[:120]}")
            except requests.exceptions.Timeout:
                warn(f"{category} report  →  timed out after 5 minutes")
            except Exception as exc:
                warn(f"{category} report  →  {exc}")

    # ── orchestration ─────────────────────────────────────────────────────────

    def run(self, start: date, end: date, skip_stats: bool = False, skip_predictions: bool = False, skip_reports: bool = False):
        print(f"\n{'═'*60}")
        print( "   Fashion ERP — Stats + Predictions + Reports Seeder")
        print(f"   Target     : {self.root}")
        print(f"   Daily range: {start} → {end}  ({(end - start).days + 1} days)")
        print(f"   Concurrency: {self.concurrency}")
        print(f"{'═'*60}")

        self.login()
        daily_errors = 0
        if not skip_stats:
            daily_errors = self.seed_daily(start, end)
            self.seed_monthly()
        if not skip_predictions:
            self.seed_predictions()
        if not skip_reports:
            self.seed_reports()

        section("Summary")
        if not skip_stats:
            print(f"  Daily stats  (sales/employees/stock) : {(end - start).days + 1 - daily_errors} days seeded")
            if daily_errors:
                print(f"  Daily failures                       : {daily_errors}")
            print(f"  Monthly stats (financial/sales/emp)  : current month")
        else:
            print(f"  Stats                                : skipped")
        if not skip_predictions:
            print(f"  Predictions   (daily + monthly)      : triggered")
        if not skip_reports:
            print(f"  Reports       (fin / sales / emp)    : triggered")
        print(f"\n{GREEN}  ✓ Done!{RESET}\n")


# ─── Entry point ──────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Seed Fashion ERP stats tables with historical data",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Seed last 2 years (default)
  python3 seed_stats.py --base-url http://192.168.11.200:8080

  # Seed last 365 days
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --days 365

  # Seed a specific date range
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --from 2024-01-01 --to 2024-12-31

  # Use more parallelism (be mindful of server load)
  python3 seed_stats.py --base-url http://192.168.11.200:8080 --concurrency 16
""",
    )
    parser.add_argument(
        "--base-url",
        default="http://localhost:8080",
        help="Backend base URL (default: http://localhost:8080)",
    )
    parser.add_argument(
        "--days",
        type=int,
        default=730,
        metavar="N",
        help="Number of past days to seed, counting back from today (default: 730)",
    )
    parser.add_argument(
        "--from",
        dest="from_date",
        default=None,
        metavar="YYYY-MM-DD",
        help="Start date; overrides --days when provided",
    )
    parser.add_argument(
        "--to",
        dest="to_date",
        default=None,
        metavar="YYYY-MM-DD",
        help="End date (default: today)",
    )
    parser.add_argument(
        "--concurrency",
        type=int,
        default=8,
        metavar="N",
        help="Number of parallel requests (default: 8)",
    )
    parser.add_argument(
        "--skip-stats",
        action="store_true",
        help="Skip daily and monthly stats generation",
    )
    parser.add_argument(
        "--skip-predictions",
        action="store_true",
        help="Skip prediction generation",
    )
    parser.add_argument(
        "--skip-reports",
        action="store_true",
        help="Skip report generation",
    )
    args = parser.parse_args()

    today = date.today()
    end   = date.fromisoformat(args.to_date)   if args.to_date   else today
    start = date.fromisoformat(args.from_date) if args.from_date else today - timedelta(days=args.days - 1)

    if start > end:
        err(f"--from {start} is after --to {end}")
        sys.exit(1)

    if args.concurrency < 1:
        err("--concurrency must be at least 1")
        sys.exit(1)

    try:
        StatsSeeder(args.base_url, concurrency=args.concurrency).run(
            start, end,
            skip_stats=args.skip_stats,
            skip_predictions=args.skip_predictions,
            skip_reports=args.skip_reports,
        )
    except KeyboardInterrupt:
        print("\nAborted.")
    except RuntimeError as exc:
        err(str(exc))
        sys.exit(1)


if __name__ == "__main__":
    main()
