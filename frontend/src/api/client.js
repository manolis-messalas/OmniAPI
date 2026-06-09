import axios from 'axios'

const client = axios.create({
  baseURL: '/api/rest',
})

client.interceptors.request.use(config => {
  const credentials = localStorage.getItem('omniapi_credentials')
  if (credentials) {
    config.headers.Authorization = `Basic ${credentials}`
  }
  return config
})

export default client
