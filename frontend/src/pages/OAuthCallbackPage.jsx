import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const TOKEN_ENDPOINT = 'http://localhost:9090/oauth2/token'
const CLIENT_ID = 'omniapi-spa'
const REDIRECT_URI = 'http://localhost:5173/oauth/callback'

export default function OAuthCallbackPage() {
  const navigate = useNavigate()
  const { setTokens } = useAuth()
  const ranOnce = useRef(false)

  useEffect(() => {
    if (ranOnce.current) return
    ranOnce.current = true

    const params = new URLSearchParams(window.location.search)
    const code = params.get('code')
    const returnedState = params.get('state')
    const expectedState = sessionStorage.getItem('oauth_state')
    const codeVerifier = sessionStorage.getItem('oauth_code_verifier')

    if (!code || !returnedState || returnedState !== expectedState || !codeVerifier) {
      navigate('/login', { replace: true })
      return
    }

    const body = new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      redirect_uri: REDIRECT_URI,
      client_id: CLIENT_ID,
      code_verifier: codeVerifier,
    })

    fetch(TOKEN_ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    })
      .then(res => { if (!res.ok) throw new Error('Token exchange failed'); return res.json() })
      .then(data => {
        setTokens({
          accessToken: data.access_token,
          expiresAt: Date.now() + data.expires_in * 1000,
        })
        sessionStorage.removeItem('oauth_state')
        sessionStorage.removeItem('oauth_code_verifier')
        navigate('/admin', { replace: true })
      })
      .catch(() => navigate('/login', { replace: true }))
  }, [navigate, setTokens])

  return <div className="min-h-screen flex items-center justify-center">Signing you in…</div>
}
