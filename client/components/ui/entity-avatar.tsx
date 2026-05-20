'use client'

import { useState, useEffect } from 'react'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'
import { images } from '@/lib/api'

interface EntityAvatarProps {
  imageId?: string | null
  fallback: string
  className?: string
}

export function EntityAvatar({ imageId, fallback, className }: EntityAvatarProps) {
  const [url, setUrl] = useState<string | null>(null)

  useEffect(() => {
    if (!imageId) return
    images.getUrl(imageId).then(r => setUrl(r.url)).catch(() => {})
  }, [imageId])

  return (
    <Avatar className={className}>
      {url && <AvatarImage src={url} alt={fallback} />}
      <AvatarFallback>{fallback.slice(0, 2).toUpperCase()}</AvatarFallback>
    </Avatar>
  )
}
