import api from '../../shared/api'

export const authService = {
  register: (data) => api.post('/auth/register', data),
  login:    (data) => api.post('/auth/login', data),
  googleLogin:    (idToken)                      => api.post('/auth/google', { idToken, role: 'STUDENT' }),
  googleRegister: (idToken, role, personalEmail) => api.post('/auth/google', { idToken, role, personalEmail }),
  me: () => api.get('/auth/me'),
}
