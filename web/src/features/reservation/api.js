import api from '../../shared/api'

export const reservationService = {
  createReservation:  (data)         => api.post('/reservations', data),
  getReservations:    (status)       => api.get('/reservations', { params: status ? { status } : {} }),
  getReservation:     (id)           => api.get(`/reservations/${id}`),
  approveReservation: (id)           => api.put(`/reservations/${id}/approve`),
  rejectReservation:  (id, reason)   => api.put(`/reservations/${id}/reject`, { reason }),
  returnReservation:  (id)           => api.put(`/reservations/${id}/return`),
  markOverdue:        (id)           => api.put(`/reservations/${id}/overdue`),
  getQR:              (id)           => api.get(`/reservations/${id}/qr`, { responseType: 'blob' }),
  downloadSlip:       (id)           => api.get(`/reservations/${id}/slip`, { responseType: 'blob' }),
}
