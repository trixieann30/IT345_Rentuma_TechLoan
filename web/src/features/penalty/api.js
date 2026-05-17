import api from '../../shared/api'

export const penaltyService = {
  getUserPenalties:  (userId) => api.get(`/users/${userId}/penalties`),
  getAllPenalties:   ()       => api.get('/penalties'),
}
