import api from '../../shared/api'

export const authService = {
  register:        (data)                         => api.post('/auth/register', data),
  login:           (data)                         => api.post('/auth/login', data),
  googleLogin:     (idToken)                      => api.post('/auth/google', { idToken, role: 'STUDENT' }),
  googleRegister:  (idToken, role, citEmail)      => api.post('/auth/google', { idToken, role, institutionalEmail: citEmail }),
  me:              ()                             => api.get('/auth/me'),
  verifyEmail:     (token)                        => api.get('/auth/verify-email', { params: { token } }),
  forgotPassword:  (email)                        => api.post('/auth/forgot-password', { email }),
  resetPassword:   (token, newPassword)           => api.post('/auth/reset-password', { token, newPassword }),
}
