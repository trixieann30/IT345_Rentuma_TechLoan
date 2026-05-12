import api from '../../shared/api'

export const holidayService = {
  getUpcoming: (limit = 5) => api.get('/holidays/upcoming', { params: { limit } }),
}
