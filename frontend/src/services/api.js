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

// Borrow request endpoints
export const borrowService = {
  createRequest: (data) => api.post('/borrow/create', data),
  getMyRequests: () => api.get('/borrow/my-requests'),
  getRequest: (id) => api.get(`/borrow/${id}`),
  approveRequest: (id) => api.put(`/borrow/${id}/approve`),
  returnRequest: (id) => api.put(`/borrow/${id}/return`),
  markOverdue: (id) => api.put(`/borrow/${id}/overdue`),
}

export default api
