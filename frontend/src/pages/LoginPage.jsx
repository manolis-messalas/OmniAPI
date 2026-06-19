import { useState } from 'react'
import { generateCodeVerifier, generateCodeChallenge, generateState } from '../auth/pkce'

const AUTH_ENDPOINT = 'http://localhost:9090/oauth2/authorize'
const LOGIN_ENDPOINT = 'http://localhost:9090/api/auth/login'
const CLIENT_ID = 'omniapi-spa'
const REDIRECT_URI = 'http://localhost:5173/oauth/callback'
const SCOPE = 'openid api.read api.write'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch(LOGIN_ENDPOINT, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username, password }),
      })

      if (!response.ok) {
        setError('Invalid username or password')
        return
      }

      // Credentials verified — now start the OAuth2 Authorization Code + PKCE flow.
      // The session cookie (JSESSIONID) set by the login response above will be sent
      // when the browser navigates to /oauth2/authorize, so Spring sees the authenticated
      // principal and issues the code without redirecting to /login again.
      const verifier = generateCodeVerifier()
      const challenge = await generateCodeChallenge(verifier)
      const state = generateState()

      sessionStorage.setItem('oauth_code_verifier', verifier)
      sessionStorage.setItem('oauth_state', state)

      const params = new URLSearchParams({
        response_type: 'code',
        client_id: CLIENT_ID,
        redirect_uri: REDIRECT_URI,
        scope: SCOPE,
        state,
        code_challenge: challenge,
        code_challenge_method: 'S256',
      })

      window.location.assign(`${AUTH_ENDPOINT}?${params.toString()}`)
    } catch {
      setError('Unable to connect to server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-sm">
        <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">OmniAPI Admin</h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
            <input
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              required
              autoFocus
              className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          {error && <p className="text-red-600 text-sm">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded font-medium hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  )
}
