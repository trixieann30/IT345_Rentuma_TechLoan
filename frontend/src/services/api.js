import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Auth endpoints
export const authService = {
  register: (data) => api.post('/auth/register', data),
  login:    (data) => api.post('/auth/login', data),
  
  // Google OAuth endpoints
  googleLogin: (idToken) => 
    api.post('/auth/google', { idToken, role: 'STUDENT' }),
  
  googleRegister: (idToken, role, personalEmail) => 
    api.post('/auth/google', { idToken, role, personalEmail }),
  
  me:       ()     => api.get('/auth/me'),
}

export default api
