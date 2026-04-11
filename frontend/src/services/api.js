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
<<<<<<< Updated upstream
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
=======
  login: (data) => api.post('/auth/login', data),
  googleLogin: (idToken) => api.post('/auth/google', { idToken, role: 'STUDENT' }),
  googleRegister: (idToken, role, personalEmail) =>
    api.post('/auth/google', { idToken, role, personalEmail }),
  me: () => api.get('/auth/me'),
}

// Reservation endpoints
export const reservationService = {
  createReservation: (data) => api.post('/reservations', data),
  getReservations: (status) =>
    api.get('/reservations', { params: status ? { status } : {} }),
  getReservation: (id) => api.get(`/reservations/${id}`),
  approveReservation: (id) => api.put(`/reservations/${id}/approve`),
  rejectReservation: (id, reason) => api.put(`/reservations/${id}/reject`, { reason }),
  returnReservation: (id) => api.put(`/reservations/${id}/return`),
  markOverdue: (id) => api.put(`/reservations/${id}/overdue`),
  getQR: (id) => api.get(`/reservations/${id}/qr`),
  downloadSlip: (id) =>
    api.get(`/reservations/${id}/slip`, { responseType: 'blob' }),
}

export const borrowService = reservationService

// Inventory endpoints
export const inventoryService = {
  // Read (all roles)
  getAvailable: () => api.get('/inventory/available'),
  getAll: () => api.get('/inventory/all'),
  getById: (id) => api.get(`/inventory/${id}`),
  getByCategory: (category) => api.get(`/inventory/category/${category}`),
  search: (query) => api.get('/inventory/search', { params: { query } }),
  getCategories: () => api.get('/inventory/categories'),

  // Custodian CRUD
  createItem: (data) => api.post('/inventory', data),
  updateItem: (id, data) => api.put(`/inventory/${id}`, data),
  deleteItem: (id) => api.delete(`/inventory/${id}`),

  // Image upload — multipart/form-data
  uploadImage: (id, file) => {
    const formData = new FormData()
    formData.append('image', file)
    return api.post(`/inventory/${id}/upload-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// Penalty endpoints
export const penaltyService = {
  getUserPenalties: (userId) => api.get(`/users/${userId}/penalties`),
}

// Payment endpoints
export const paymentService = {
  initiate: (loanId, amount) => api.post('/payments/initiate', { loanId, amount }),
  getHistory: () => api.get('/payments/history'),
}

export default api
>>>>>>> Stashed changes
