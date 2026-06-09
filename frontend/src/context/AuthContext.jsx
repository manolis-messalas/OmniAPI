import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [credentials, setCredentials] = useState(() => localStorage.getItem('omniapi_credentials'))

  const login = (username, password) => {
    const encoded = btoa(`${username}:${password}`)
    setCredentials(encoded)
    localStorage.setItem('omniapi_credentials', encoded)
  }

  const logout = () => {
    setCredentials(null)
    localStorage.removeItem('omniapi_credentials')
  }

  return (
    <AuthContext.Provider value={{ credentials, login, logout, isAuthenticated: !!credentials }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
