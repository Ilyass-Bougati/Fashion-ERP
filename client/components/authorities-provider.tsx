'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { users } from '@/lib/api'

interface AuthoritiesState {
  authorities: string[]
  loaded: boolean
}

const AuthoritiesContext = createContext<AuthoritiesState>({ authorities: [], loaded: false })

export function AuthoritiesProvider({ children }: { children: React.ReactNode }) {
  const [authorities, setAuthorities] = useState<string[]>([])
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    users.me.authorities()
      .then(setAuthorities)
      .catch(() => setAuthorities([]))
      .finally(() => setLoaded(true))
  }, [])

  return (
    <AuthoritiesContext.Provider value={{ authorities, loaded }}>
      {children}
    </AuthoritiesContext.Provider>
  )
}

export function useAuthorities() {
  return useContext(AuthoritiesContext).authorities
}

// True once the authorities have been fetched (success or failure), so callers
// can distinguish "not loaded yet" from "loaded but the user has none".
export function useAuthoritiesLoaded() {
  return useContext(AuthoritiesContext).loaded
}
