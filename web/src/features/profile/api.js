import api from '../../shared/api'

export const profileService = {
  getProfile:        ()       => api.get('/auth/me'),
  updateProfile:     (data)   => api.put('/auth/me', data),
  getBorrowHistory:  (userId) => api.get('/reservations', { params: { userId } }),
  getPenaltySummary: (userId) => api.get(`/users/${userId}/penalties`),
}
