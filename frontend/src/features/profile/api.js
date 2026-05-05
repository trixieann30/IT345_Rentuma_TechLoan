import api from '../../shared/api'

export const profileService = {
  getProfile:      ()       => api.get('/auth/me'),
  getBorrowHistory: (userId) => api.get('/reservations', { params: { userId } }),
  getPenaltySummary: (userId) => api.get(`/users/${userId}/penalties`),
}
