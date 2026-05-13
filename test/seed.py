#!/usr/bin/env python3
"""
Fashion ERP Mock Data Seeder
Logs in as admin and populates the database with comprehensive mock data.

Usage:
    python3 seed.py [--base-url http://localhost:8080]
"""

import sys
import struct
import zlib
import random
import argparse
from io import BytesIO
from datetime import datetime, timedelta

try:
    import requests
except ImportError:
    print("Missing dependency: pip install requests")
    sys.exit(1)

# ─── Config ───────────────────────────────────────────────────────────────────

ADMIN_EMAIL    = "admin@gmail.com"
ADMIN_PASSWORD = "adminadmin"

# ─── Helpers ──────────────────────────────────────────────────────────────────

GREEN  = "\033[92m"
YELLOW = "\033[93m"
RED    = "\033[91m"
CYAN   = "\033[96m"
RESET  = "\033[0m"

def ok(msg):    print(f"  {GREEN}✓{RESET} {msg}")
def warn(msg):  print(f"  {YELLOW}⚠{RESET}  {msg}")
def err(msg):   print(f"  {RED}✗{RESET} {msg}")
def section(t): print(f"\n{CYAN}{'─'*60}{RESET}\n{CYAN}  {t}{RESET}\n{CYAN}{'─'*60}{RESET}")

def dt_str(d: datetime) -> str:
    return d.strftime("%Y-%m-%dT%H:%M:%S")


# ─── Minimal PNG generator (no Pillow needed) ─────────────────────────────────

def make_png(r: int = 255, g: int = 128, b: int = 64) -> bytes:
    """Build a valid 1×1 RGB PNG in memory."""
    def chunk(tag: bytes, data: bytes) -> bytes:
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    sig  = b"\x89PNG\r\n\x1a\n"
    ihdr = chunk(b"IHDR", struct.pack(">IIBBBBB", 1, 1, 8, 2, 0, 0, 0))
    idat = chunk(b"IDAT", zlib.compress(bytes([0, r, g, b])))   # filter=0, R G B
    iend = chunk(b"IEND", b"")
    return sig + ihdr + idat + iend


# ─── Phone counter (10-digit, unique across the run) ─────────────────────────

_phone = 1_100_000_000

def next_phone() -> str:
    global _phone
    _phone += 1
    return str(_phone)


# ─── Seeder ───────────────────────────────────────────────────────────────────

class Seeder:
    def __init__(self, base_url: str):
        self.base = base_url.rstrip("/") + "/api/v1"
        self.s    = requests.Session()

    # ── low-level ────────────────────────────────────────────────────────────

    def _post(self, path: str, **kwargs):
        resp = self.s.post(f"{self.base}{path}", **kwargs)
        if not resp.ok:
            raise RuntimeError(f"POST {path} → {resp.status_code}: {resp.text[:200]}")
        return resp.json() if resp.text else {}

    def _get(self, path: str, **kwargs):
        resp = self.s.get(f"{self.base}{path}", **kwargs)
        if not resp.ok:
            raise RuntimeError(f"GET {path} → {resp.status_code}: {resp.text[:200]}")
        return resp.json()

    # ── auth ─────────────────────────────────────────────────────────────────

    def login(self):
        section("1 · Authentication")
        self._post("/auth/login", json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD})
        ok(f"Logged in as {ADMIN_EMAIL}")

    # ── images ───────────────────────────────────────────────────────────────

    def upload_images(self, count: int = 20) -> list[str]:
        section("2 · Uploading placeholder images")
        ids = []
        for i in range(count):
            r, g, b = random.randint(30, 240), random.randint(30, 240), random.randint(30, 240)
            png = make_png(r, g, b)
            data = self._post(
                "/images/upload",
                files={"file": (f"placeholder_{i}.png", BytesIO(png), "image/png")},
            )
            ids.append(str(data["imageId"]))
        ok(f"Uploaded {count} placeholder images")
        return ids

    # ── product categories ────────────────────────────────────────────────────

    CATEGORIES = [
        ("T-Shirts",     "Casual and formal t-shirts for every occasion and season"),
        ("Jeans",        "Denim jeans in multiple cuts, washes and fits"),
        ("Dresses",      "Elegant dresses ranging from casual to formal evening wear"),
        ("Jackets",      "Lightweight and heavy jackets for all weather conditions"),
        ("Shoes",        "Footwear collection spanning casual sneakers to formal heels"),
        ("Accessories",  "Belts, scarves, hats, bags and other fashion accessories"),
        ("Hoodies",      "Comfortable hoodies and sweatshirts for everyday wear"),
        ("Shorts",       "Summer shorts, bermudas and athletic bottoms"),
        ("Blazers",      "Tailored blazers and suits for a sharp professional look"),
        ("Activewear",   "Performance sportswear designed for training and lifestyle"),
    ]

    def create_categories(self) -> list[str]:
        section("3 · Product categories")
        ids = []
        for name, desc in self.CATEGORIES:
            d = self._post("/product-categories", json={"name": name, "description": desc})
            ids.append(d["id"])
            ok(f"Category: {name}")
        return ids

    # ── products ─────────────────────────────────────────────────────────────

    PRODUCTS_BY_CAT = {
        "T-Shirts":    ["Classic White Tee", "Graphic Print Tee", "Polo Shirt", "V-Neck Tee", "Striped Tee", "Pocket Tee"],
        "Jeans":       ["Slim Fit Jeans", "Bootcut Jeans", "Skinny Jeans", "Wide Leg Jeans", "Cargo Jeans", "Straight Cut Jeans"],
        "Dresses":     ["Summer Floral Dress", "Evening Gown", "Casual Midi Dress", "Maxi Dress", "Mini Dress", "Wrap Dress"],
        "Jackets":     ["Leather Biker Jacket", "Denim Jacket", "Puffer Jacket", "Trench Coat", "Bomber Jacket"],
        "Shoes":       ["White Sneakers", "Ankle Boots", "Loafers", "High Heels", "Running Shoes", "Oxford Shoes"],
        "Accessories": ["Leather Belt", "Silk Scarf", "Baseball Cap", "Sunglasses", "Tote Bag", "Crossbody Bag"],
        "Hoodies":     ["Zip-Up Hoodie", "Pullover Hoodie", "Cropped Hoodie", "Oversized Hoodie", "Fleece Hoodie"],
        "Shorts":      ["Chino Shorts", "Denim Shorts", "Athletic Shorts", "Linen Shorts"],
        "Blazers":     ["Single Breasted Blazer", "Double Breasted Blazer", "Checked Blazer", "Velvet Blazer"],
        "Activewear":  ["Sports Leggings", "Training Top", "Running Jacket", "Compression Shorts", "Sports Bra"],
    }

    def create_products(self, cat_ids: list[str], image_pool: list[str]) -> list[str]:
        section("4 · Products")
        ids = []
        for idx, (cat_name, _) in enumerate(self.CATEGORIES):
            cat_id = cat_ids[idx]
            for name in self.PRODUCTS_BY_CAT.get(cat_name, []):
                d = self._post("/products", json={
                    "name": name,
                    "productCategoryId": cat_id,
                    "imageId": random.choice(image_pool),
                })
                ids.append(d["id"])
                ok(f"Product: {name}")
        return ids

    # ── product variations ────────────────────────────────────────────────────

    SIZES  = ["XS", "S", "M", "L", "XL", "XXL"]
    COLORS = ["BLACK", "WHITE", "NAVY", "RED", "GREEN", "GREY", "BEIGE", "BLUE", "KHAKI", "BROWN"]

    def create_variations(self, product_ids: list[str]) -> list[dict]:
        section("5 · Product variations")
        variations = []
        sku_n = 1000
        for pid in product_ids:
            combos_used: set[tuple] = set()
            for _ in range(random.randint(3, 6)):
                size  = random.choice(self.SIZES)
                color = random.choice(self.COLORS)
                attempts = 0
                while (size, color) in combos_used and attempts < 20:
                    size  = random.choice(self.SIZES)
                    color = random.choice(self.COLORS)
                    attempts += 1
                if (size, color) in combos_used:
                    continue
                combos_used.add((size, color))

                sku   = f"SKU-{sku_n:05d}-{size}-{color[:3]}"
                price = round(random.uniform(12.0, 350.0), 2)
                qty   = random.randint(10, 300)
                sku_n += 1

                d = self._post("/product-variations", json={
                    "sku":       sku,
                    "price":     price,
                    "productId": pid,
                    "quantity":  qty,
                })
                variations.append({
                    "id":    d["id"],
                    "sku":   sku,
                    "price": price,
                    "qty":   qty,
                })
                ok(f"Variation: {sku}  qty={qty}  price={price}")
        return variations

    # ── vendors ───────────────────────────────────────────────────────────────

    _COMPANIES = [
        "FashionHub Ltd", "StyleSource Inc", "TrendFactory SA",   "GlobalTextiles Co",
        "ModaSupply AG",  "PrimeCloth GmbH",  "EliteWear Partners","UrbanFabric LLC",
        "ChicSuppliers",  "LuxeTextile Group","FabricNation Ltd",  "TrendSetters Co",
        "CoutureSupply",  "ModeExpress",      "StyleBridge LLC",   "NovaTex SA",
        "FastFabric Inc", "EcoCloth Co",      "PremiumWear Ltd",   "FreshMode SAS",
    ]
    _PAYMENT_TERMS = ["Net 30", "Net 60", "Net 15", "2/10 Net 30", "COD", "Net 45", "EOM"]

    def create_vendors(self, product_ids: list[str]) -> list[str]:
        section("6 · Vendors")
        ids = []
        vendor_n = 1
        for pid in product_ids:
            for _ in range(random.randint(1, 3)):
                company = random.choice(self._COMPANIES)
                slug    = company.split()[0].lower().replace("'", "")
                d = self._post("/vendors", json={
                    "companyName":  company,
                    "email":        f"orders.{vendor_n}@{slug}.com",
                    "contactName":  f"Purchasing Agent {vendor_n}",
                    "phoneNumber":  next_phone(),
                    "paymentTerms": random.choice(self._PAYMENT_TERMS),
                    "active":       True,
                    "productId":    pid,
                })
                ids.append(d["id"])
                ok(f"Vendor: {company}  →  product {pid[:8]}…")
                vendor_n += 1
        return ids

    # ── employees ─────────────────────────────────────────────────────────────

    _FIRST = [
        "Youssef", "Fatima",    "Mohammed", "Aicha",   "Omar",   "Khadija",
        "Hassan",  "Zineb",     "Rachid",   "Samira",  "Khalid", "Nadia",
        "Mehdi",   "Soukaina",  "Amine",    "Houda",   "Tarik",  "Laila",
        "Bilal",   "Meryem",
    ]
    _LAST = [
        "Benkhalil", "Alami",   "Benali",  "Tazi",   "Chraibi", "Fassi",
        "Ouali",     "Berrada", "Rhazi",   "Lamrani","Kadiri",  "Mekki",
        "Bouazza",   "Hajji",   "El Amri", "Slaoui", "Idrissi", "Lahlou",
    ]

    def create_employees(self, count: int = 15) -> list[dict]:
        section("7 · Employees")
        employees = []
        used_emails: set[str] = set()
        used_cins:   set[str] = set()

        for i in range(count):
            first = random.choice(self._FIRST)
            last  = random.choice(self._LAST)
            email = f"{first.lower()}.{last.lower().replace(' ', '')}{i}@fashion-erp.ma"
            while email in used_emails:
                email = f"{first.lower()}.{last.lower().replace(' ', '')}{i}_{random.randint(2,99)}@fashion-erp.ma"
            used_emails.add(email)

            cin = f"AB{random.randint(100000, 999999)}"
            while cin in used_cins:
                cin = f"AB{random.randint(100000, 999999)}"
            used_cins.add(cin)

            hired_at = datetime.now() - timedelta(days=random.randint(90, 1460))

            d = self._post("/employee", json={
                "firstName":  first,
                "lastName":   last,
                "phoneNumber": next_phone(),
                "CIN":        cin,
                "email":      email,
                "active":     True,
                "salary":     round(random.uniform(2800.0, 9500.0), 2),
                "commission": round(random.uniform(0.02, 0.18), 4),
                "hiredAt":    dt_str(hired_at),
            })
            employees.append({"id": d["id"], "name": f"{first} {last}"})
            ok(f"Employee: {first} {last}  (CIN {cin})")

        return employees

    # ── isles ─────────────────────────────────────────────────────────────────

    def create_isles(self, employees: list[dict]) -> list[str]:
        section("8 · Isles (work stations)")
        ids  = []
        isle = 1
        for emp in employees:
            for _ in range(random.randint(1, 3)):
                d = self._post("/isle", json={
                    "code":       f"ISLE-{isle:03d}",
                    "employeeId": emp["id"],
                })
                ids.append(d["id"])
                ok(f"Isle ISLE-{isle:03d}  →  {emp['name']}")
                isle += 1
        return ids

    # ── fixed charges ─────────────────────────────────────────────────────────

    _CHARGES = [
        ("Store Rent – Main Branch",   "Monthly rent for the flagship retail location downtown",     18500.0,  True),
        ("Store Rent – Annexe",        "Monthly rent for the secondary outlet in the shopping mall",  9200.0,  True),
        ("Electricity",                "Monthly electricity utility bill covering all store floors",  2800.0,  True),
        ("Water & Sewage",             "Municipal water and sewage charges for all premises",           380.0,  True),
        ("Internet & Telecoms",        "Fibre broadband plus business phone lines",                    950.0,  True),
        ("Business Insurance",         "Annual liability and stock insurance policy (monthly slice)",  1400.0,  True),
        ("CCTV & Security",            "Monthly subscription for monitored alarm and CCTV systems",    550.0,  True),
        ("Professional Cleaning",      "Twice-weekly commercial cleaning contractor fees",             720.0,  True),
        ("Accounting Software",        "Monthly SaaS licence for accounting and invoicing platform",   180.0,  True),
        ("POS System Licence",         "Monthly point-of-sale software subscription per terminal",     240.0,  True),
        ("Warehouse Rental",           "Off-site warehouse space for seasonal overflow inventory",    6200.0,  True),
        ("Pest Control",               "Quarterly pest-control service contract amortised monthly",    120.0,  False),
        ("Window Display Maintenance", "Monthly cost of external window-dressing service",             300.0,  True),
    ]

    def create_fixed_charges(self) -> list[str]:
        section("9 · Fixed charges")
        ids = []
        for name, desc, amount, active in self._CHARGES:
            d = self._post("/finance/fixed-charges", json={
                "name":        name,
                "description": desc,
                "amount":      amount,
                "active":      active,
            })
            ids.append(d["id"])
            status = "active" if active else "inactive"
            ok(f"Fixed charge: {name}  {amount:.2f} MAD  [{status}]")
        return ids

    # ── sales ─────────────────────────────────────────────────────────────────

    def create_sales(
        self,
        employees:  list[dict],
        variations: list[dict],
        count:      int = 80,
    ) -> tuple[list[str], list[str]]:
        section("10 · Sales, sale lines & checkout")
        completed: list[str] = []
        pending:   list[str] = []

        for i in range(count):
            emp      = random.choice(employees)
            discount = random.choice([0.0, 0.0, 0.0, 0.05, 0.10, 0.15, 0.20, 0.25])

            try:
                sale = self._post("/sale", json={
                    "discount":   discount,
                    "employeeId": emp["id"],
                })
            except RuntimeError as e:
                warn(f"Sale {i+1} creation failed: {e}")
                continue

            sale_id = sale["id"]
            lines_ok = 0
            chosen = random.sample(variations, min(random.randint(2, 6), len(variations)))

            for var in chosen:
                qty        = random.randint(1, 3)
                sale_price = round(var["price"] * random.uniform(0.85, 1.05), 2)
                sale_price = max(sale_price, 0.01)
                try:
                    self._post("/sale-line", json={
                        "saleId":             sale_id,
                        "productVariationId": var["id"],
                        "quantity":           qty,
                        "saleAtPrice":        sale_price,
                    })
                    lines_ok += 1
                except RuntimeError as e:
                    warn(f"  Sale {i+1} line failed ({var['sku']}): {e}")

            if lines_ok == 0:
                warn(f"Sale {i+1}: no lines added, left as empty PENDING")
                pending.append(sale_id)
                continue

            # 65% complete, 12% refund, 23% stay pending
            roll = random.random()
            if roll < 0.65:
                try:
                    self._post(f"/sale/{sale_id}/checkout")
                    completed.append(sale_id)
                    ok(f"Sale {i+1}: COMPLETED  ({lines_ok} lines, {discount*100:.0f}% disc)  by {emp['name']}")
                except RuntimeError as e:
                    warn(f"Sale {i+1}: checkout failed: {e}")
                    pending.append(sale_id)
            elif roll < 0.77:
                try:
                    self._post(f"/sale/{sale_id}/checkout")
                    self._post(f"/sale/{sale_id}/refund")
                    ok(f"Sale {i+1}: REFUNDED   ({lines_ok} lines)  by {emp['name']}")
                except RuntimeError as e:
                    warn(f"Sale {i+1}: refund flow failed: {e}")
                    pending.append(sale_id)
            else:
                pending.append(sale_id)
                ok(f"Sale {i+1}: PENDING    ({lines_ok} lines)  by {emp['name']}")

        return completed, pending

    # ── payroll ───────────────────────────────────────────────────────────────

    def process_payrolls(self, employees: list[dict]):
        section("11 · Payroll processing")
        now = datetime.now()
        # Three rolling monthly periods
        periods = [
            (now - timedelta(days=90), now - timedelta(days=61)),
            (now - timedelta(days=60), now - timedelta(days=31)),
            (now - timedelta(days=30), now),
        ]
        for emp in employees:
            period = random.choice(periods)
            start  = dt_str(period[0].replace(hour=0,  minute=0,  second=0))
            end    = dt_str(period[1].replace(hour=23, minute=59, second=59))
            try:
                self._post(
                    f"/finance/payroll/process/{emp['id']}",
                    params={"startDate": start, "endDate": end},
                )
                ok(f"Payroll: {emp['name']}  {start[:10]} → {end[:10]}")
            except RuntimeError as e:
                warn(f"Payroll failed for {emp['name']}: {e}")

    # ── manual transactions ───────────────────────────────────────────────────

    _TX_DESCRIPTIONS = [
        ("PAID",     2500.0,   "Ad-hoc supplier payment"),
        ("RECEIVED", 5000.0,   "Cash deposit from weekend market stall"),
        ("PAID",     800.0,    "Emergency packaging restock"),
        ("RECEIVED", 1200.0,   "Online store payout"),
        ("PAID",     3200.0,   "Trade show booth fee"),
        ("RECEIVED", 750.0,    "Staff canteen receipts"),
        ("PAID",     1500.0,   "Logo embroidery service"),
        ("RECEIVED", 4300.0,   "Wholesale order advance"),
        ("PAID",     600.0,    "Photography session for catalogue"),
        ("RECEIVED", 900.0,    "Gift card redemptions cleared"),
        ("PAID",     2100.0,   "Import duty settlement"),
        ("RECEIVED", 3600.0,   "End-of-season sale overflow proceeds"),
        ("PAID",     450.0,    "Staff uniforms"),
        ("RECEIVED", 1800.0,   "Consignment stock return credit"),
        ("PAID",     1100.0,   "Domain & hosting annual renewal"),
    ]

    def create_manual_transactions(self):
        section("12 · Manual transactions")
        for tx_type, base_amount, label in self._TX_DESCRIPTIONS:
            amount = round(base_amount * random.uniform(0.8, 1.3), 2)
            try:
                self._post("/finance/transactions", json={"type": tx_type, "amount": amount})
                ok(f"Transaction: {tx_type:8s}  {amount:>10.2f} MAD  — {label}")
            except RuntimeError as e:
                warn(f"Transaction failed ({label}): {e}")

    # ── additional users ──────────────────────────────────────────────────────

    def create_users(self) -> list[dict]:
        section("13 · Additional user accounts")
        users_data = [
            ("Ilyass",   "Bougati",  "ilyass.bougati@fashion-erp.ma",   "password123", "0612345678"),
            ("Sara",     "Alaoui",   "sara.alaoui@fashion-erp.ma",       "password123", "0623456789"),
            ("Karim",    "Mansouri", "karim.mansouri@fashion-erp.ma",    "password123", "0634567890"),
            ("Leila",    "Cherkaoui","leila.cherkaoui@fashion-erp.ma",   "password123", "0645678901"),
            ("Hamza",    "Bensouda", "hamza.bensouda@fashion-erp.ma",    "password123", "0656789012"),
        ]
        created = []
        for first, last, email, pwd, phone in users_data:
            try:
                d = self._post("/user", json={
                    "firstName":   first,
                    "lastName":    last,
                    "email":       email,
                    "password":    pwd,
                    "phoneNumber": phone,
                })
                created.append({"id": d["id"], "email": email})
                ok(f"User: {first} {last}  <{email}>")
            except RuntimeError as e:
                warn(f"User {email} failed: {e}")
        return created

    # ── orchestration ─────────────────────────────────────────────────────────

    def run(self):
        print(f"\n{'═'*60}")
        print( "   Fashion ERP — Mock Data Seeder")
        print(f"   Target: {self.base}")
        print(f"{'═'*60}")

        self.login()

        image_pool  = self.upload_images(count=20)
        cat_ids     = self.create_categories()
        product_ids = self.create_products(cat_ids, image_pool)
        variations  = self.create_variations(product_ids)
        self.create_vendors(product_ids)
        employees   = self.create_employees(count=15)
        self.create_isles(employees)
        self.create_fixed_charges()
        completed, pending = self.create_sales(employees, variations, count=80)
        self.process_payrolls(employees)
        self.create_manual_transactions()
        self.create_users()

        section("Summary")
        print(f"  Images uploaded   : 20")
        print(f"  Categories        : {len(cat_ids)}")
        print(f"  Products          : {len(product_ids)}")
        print(f"  Variations        : {len(variations)}")
        print(f"  Employees         : {len(employees)}")
        print(f"  Sales completed   : {len(completed)}")
        print(f"  Sales pending     : {len(pending)}")
        print(f"\n{GREEN}  ✓ Seeding complete!{RESET}\n")


# ─── Entry point ──────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Seed the Fashion ERP database with mock data")
    parser.add_argument(
        "--base-url",
        default="http://localhost:8080",
        help="Base URL of the running server (default: http://localhost:8080)",
    )
    args = parser.parse_args()

    try:
        Seeder(args.base_url).run()
    except KeyboardInterrupt:
        print("\nAborted.")
    except RuntimeError as e:
        err(str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
