import type { NextRequest } from 'next/server'

const BACKEND = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params

  const upstream = await fetch(`${BACKEND}/api/v1/reports/${id}/download`, {
    headers: { cookie: request.headers.get('cookie') ?? '' },
  })

  if (!upstream.ok) {
    return new Response(null, { status: upstream.status })
  }

  const responseHeaders: Record<string, string> = {
    'Content-Disposition': upstream.headers.get('Content-Disposition') ?? 'attachment',
    'Content-Type': upstream.headers.get('Content-Type') ?? 'application/octet-stream',
  }

  const contentLength = upstream.headers.get('content-length')
  if (contentLength) responseHeaders['Content-Length'] = contentLength

  return new Response(upstream.body, {
    status: upstream.status,
    headers: responseHeaders,
  })
}
