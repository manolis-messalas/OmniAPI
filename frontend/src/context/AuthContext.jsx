import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

function loadStoredAuth() {
  const accessToken = localStorage.getItem('omniapi_access_token')
  const expiresAt = Number(localStorage.getItem('omniapi_expires_at') || 0)
  return { accessToken, expiresAt }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(loadStoredAuth)

  const setTokens = useCallback(({ accessToken, expiresAt }) => {
    localStorage.setItem('omniapi_access_token', accessToken)
    localStorage.setItem('omniapi_expires_at', String(expiresAt))
    setAuth({ accessToken, expiresAt })
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('omniapi_access_token')
    localStorage.removeItem('omniapi_expires_at')
    setAuth({ accessToken: null, expiresAt: 0 })
  }, [])

  const isAuthenticated = !!auth.accessToken && Date.now() < auth.expiresAt

  return (
    <AuthContext.Provider value={{ ...auth, isAuthenticated, setTokens, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
