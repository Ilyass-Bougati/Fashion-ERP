'use client'

export function getStoredUser() {
  if (typeof window === 'undefined') return null
  try {
    const stored = localStorage.getItem('erp_user')
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

export function setStoredUser(user: object | null) {
  if (typeof window === 'undefined') return
  if (user) {
    localStorage.setItem('erp_user', JSON.stringify(user))
  } else {
    localStorage.removeItem('erp_user')
  }
}

export function clearAuth() {
  if (typeof window === 'undefined') return
  localStorage.removeItem('erp_user')
}
