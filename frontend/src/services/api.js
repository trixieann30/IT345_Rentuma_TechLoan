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
  register: (data)   => api.post('/auth/register', data),
  login:    (data)   => api.post('/auth/login', data),

  // Google OAuth endpoints
  googleLogin:    (idToken)                          => api.post('/auth/google', { idToken, role: 'STUDENT' }),
  googleRegister: (idToken, role, personalEmail)     => api.post('/auth/google', { idToken, role, personalEmail }),

  me: () => api.get('/auth/me'),
}

// Reservation endpoints — path changed from /borrow to /reservations
// Body shape changed to: { inventoryId, quantity, purpose, returnDate }
export const reservationService = {
  // Student: create a new reservation
  // body: { inventoryId: Long, quantity: number, purpose: string, returnDate: 'YYYY-MM-DD' }
  createReservation: (data)  => api.post('/reservations', data),

  // Get reservations — custodian gets all, student gets own
  getReservations:   (status) =>
    api.get('/reservations', { params: status ? { status } : {} }),

  getReservation: (id) => api.get(`/reservations/${id}`),

  // Custodian actions
  approveReservation: (id)           => api.put(`/reservations/${id}/approve`),
  rejectReservation:  (id, reason)   => api.put(`/reservations/${id}/reject`, { reason }),
  returnReservation:  (id)           => api.put(`/reservations/${id}/return`),
  markOverdue:        (id)           => api.put(`/reservations/${id}/overdue`),
}

// Keep old borrowService as an alias so any existing component code doesn't break immediately.
// TODO: migrate all components from borrowService → reservationService and remove this.
export const borrowService = reservationService

// Inventory endpoints
export const inventoryService = {
  // Read (all roles)
  getAvailable:   ()         => api.get('/inventory/available'),
  getAll:         ()         => api.get('/inventory/all'),
  getById:        (id)       => api.get(`/inventory/${id}`),
  getByCategory:  (category) => api.get(`/inventory/category/${category}`),
  search:         (query)    => api.get('/inventory/search', { params: { query } }),
  getCategories:  ()         => api.get('/inventory/categories'),

  // Custodian CRUD — NEW
  // createItem body: { name, description, category, quantity, condition, specifications? }
  createItem: (data)       => api.post('/inventory', data),
  // updateItem body: any subset of { name, description, category, quantity, condition, available }
  updateItem: (id, data)   => api.put(`/inventory/${id}`, data),
  deleteItem: (id)         => api.delete(`/inventory/${id}`),
}

export default api