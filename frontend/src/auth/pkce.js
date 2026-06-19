function base64UrlEncode(arrayBuffer) {
  const bytes = new Uint8Array(arrayBuffer)
  const binary = Array.from(bytes, b => String.fromCharCode(b)).join('')
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

export function generateCodeVerifier() {
  const array = new Uint8Array(32)
  window.crypto.getRandomValues(array)
  return base64UrlEncode(array)
}

export async function generateCodeChallenge(verifier) {
  const data = new TextEncoder().encode(verifier)
  const digest = await window.crypto.subtle.digest('SHA-256', data)
  return base64UrlEncode(digest)
}

export function generateState() {
  const array = new Uint8Array(16)
  window.crypto.getRandomValues(array)
  return base64UrlEncode(array)
}
