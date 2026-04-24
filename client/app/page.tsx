import { Analytics } from "@vercel/analytics/next"

export default function Home() {
  return (
    <>
      <div><h1>Welcome to Fashion ERP</h1></div>
      <Analytics />
    </>
  );
}
