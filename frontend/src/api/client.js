import axios from 'axios'

const client = axios.create({
  baseURL: '/api/rest',
})

client.interceptors.request.use(config => {
  const accessToken = localStorage.getItem('omniapi_access_token')
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

client.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('omniapi_access_token')
      localStorage.removeItem('omniapi_expires_at')
      window.location.assign('/login')
    }
    return Promise.reject(error)
  }
)

export default client
