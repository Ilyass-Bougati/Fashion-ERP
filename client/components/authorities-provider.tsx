'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { users } from '@/lib/api'

const AuthoritiesContext = createContext<string[]>([])

export function AuthoritiesProvider({ children }: { children: React.ReactNode }) {
  const [authorities, setAuthorities] = useState<string[]>([])

  useEffect(() => {
    users.me.authorities()
      .then(setAuthorities)
      .catch(() => setAuthorities([]))
  }, [])

  return (
    <AuthoritiesContext.Provider value={authorities}>
      {children}
    </AuthoritiesContext.Provider>
  )
}

export function useAuthorities() {
  return useContext(AuthoritiesContext)
}
