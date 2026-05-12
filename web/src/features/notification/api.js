import api from '../../shared/api'

export const notificationService = {
  getAll:       ()   => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead:     (id) => api.put(`/notifications/${id}/read`),
  markAllRead:  ()   => api.put('/notifications/read-all'),
}
