import api from '../../shared/api'

export const paymentService = {
  initiate:   (penaltyId)   => api.post('/payments/initiate', { penaltyId }),
  confirm:    (paymentId)   => api.post(`/payments/${paymentId}/confirm`),
  getHistory: (userId)      => api.get('/payments/history', { params: { userId } }),
}
