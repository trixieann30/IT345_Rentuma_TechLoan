import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { authService } from './api'

export default function LoginPage() {
  const navigate = useNavigate()
  const [form,     setForm]     = useState({ email: '', password: '' })
  const [errors,   setErrors]   = useState({})
  const [apiError, setApiError] = useState('')
  const [loading,  setLoading]  = useState(false)

  const [showForgot,    setShowForgot]    = useState(false)
  const [forgotEmail,   setForgotEmail]   = useState('')
  const [forgotLoading, setForgotLoading] = useState(false)
  const [forgotMsg,     setForgotMsg]     = useState('')
  const [forgotError,   setForgotError]   = useState('')

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
    setApiError('')
  }

  function validate() {
    const errs = {}
    if (!form.email) errs.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Invalid email format'
    if (!form.password) errs.password = 'Password is required'
    else if (form.password.length < 6) errs.password = 'Password must be at least 6 characters'
    return errs
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setLoading(true)
    setApiError('')
    try {
      const res = await authService.login(form)
      const { token, refreshToken, user } = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))
      navigate('/dashboard')
    } catch (err) {
      setApiError(err.response?.data?.error?.message || err.response?.data?.message || 'Login failed. Please check your credentials.')
    } finally {
      setLoading(false)
    }
  }

  async function handleForgotSubmit(e) {
    e.preventDefault()
    if (!forgotEmail) { setForgotError('Email is required'); return }
    setForgotLoading(true)
    setForgotError('')
    setForgotMsg('')
    try {
      await authService.forgotPassword(forgotEmail)
      setForgotMsg('If that email is registered, a reset link has been sent. Check your inbox.')
    } catch {
      setForgotError('Something went wrong. Please try again.')
    } finally {
      setForgotLoading(false)
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    setLoading(true)
    setApiError('')
    try {
      const res = await authService.googleLogin(credentialResponse.credential)
      const { token, refreshToken, user } = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))
      navigate('/dashboard')
    } catch (err) {
      setApiError(err.response?.data?.error?.message || 'Google login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex">

      {/* ── Left Hero Panel ────────────────────────────────────────────── */}
      <div
        className="hidden lg:flex flex-col justify-between w-[45%] xl:w-[42%] p-12 relative overflow-hidden"
        style={{ background: 'linear-gradient(150deg, #BE1B39 0%, #8C1229 45%, #120709 100%)' }}
      >
        {/* Dot grid */}
        <div className="absolute inset-0 pointer-events-none"
          style={{ backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.13) 1px, transparent 1px)', backgroundSize: '24px 24px' }} />
        {/* Decorative circles */}
        <div className="absolute -top-20 -right-20 w-80 h-80 rounded-full opacity-10" style={{ background: '#F4C430' }} />
        <div className="absolute bottom-20 -left-16 w-64 h-64 rounded-full opacity-[0.07]" style={{ background: '#E06060' }} />
        <div className="absolute top-1/2 right-0 w-40 h-40 rounded-full opacity-[0.06]" style={{ background: '#F4C430' }} />

        {/* Brand */}
        <div className="relative">
          <div className="flex items-center gap-3 mb-16">
            <div className="w-11 h-11 rounded-xl flex items-center justify-center" style={{ background: 'rgba(244,196,48,0.15)', border: '1px solid rgba(244,196,48,0.3)' }}>
              <span className="font-black text-xl" style={{ color: '#F4C430' }}>T</span>
            </div>
            <div>
              <p className="text-white font-bold text-xl leading-tight">TechLoan</p>
              <p className="text-[12px] leading-tight" style={{ color: '#9B6070' }}>CIT-U Lab Equipment System</p>
            </div>
          </div>

          <h1 className="text-4xl xl:text-5xl font-black text-white leading-tight mb-4">
            Borrow smarter.<br />
            <span style={{ color: '#F4C430' }}>Return on time.</span>
          </h1>
          <p className="text-base leading-relaxed" style={{ color: '#C09098' }}>
            The all-in-one platform for CIT-U students and faculty to reserve, track, and manage lab equipment loans.
          </p>
        </div>

        {/* Feature bullets */}
        <div className="relative space-y-4">
          {[
            ['QR Code Slips',     'Instant digital borrowing slips with scannable QR codes'],
            ['Real-time Updates', 'Notifications for every reservation status change'],
            ['Online Payments',   'Pay penalty fines via GCash or Maya instantly'],
          ].map(([title, desc]) => (
            <div key={title} className="flex items-start gap-3">
              <div className="w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5" style={{ background: 'rgba(244,196,48,0.2)' }}>
                <svg className="w-3 h-3" fill="none" stroke="#F4C430" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div>
                <p className="text-sm font-semibold text-white">{title}</p>
                <p className="text-xs leading-snug" style={{ color: '#9B6070' }}>{desc}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Footer note */}
        <p className="relative text-xs" style={{ color: '#4A2535' }}>
          © 2025 TechLoan · Cebu Institute of Technology – University
        </p>
      </div>

      {/* ── Right Form Panel ───────────────────────────────────────────── */}
      <div className="flex-1 flex items-center justify-center px-6 py-12 bg-white">
        <div className="w-full max-w-[400px]">

          {/* Mobile brand mark */}
          <div className="flex lg:hidden items-center gap-2.5 mb-8">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center" style={{ background: '#BE1B39' }}>
              <span className="font-black text-white text-base">T</span>
            </div>
            <p className="font-bold text-gray-900 text-lg">TechLoan</p>
          </div>

          <h2 className="text-3xl font-black text-gray-900 mb-1">Welcome back</h2>
          <p className="text-gray-500 text-sm mb-8">Sign in to manage your tech loans</p>

          {apiError && (
            <div className="flex items-start gap-2.5 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-6">
              <svg className="w-4 h-4 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="label">Email Address</label>
              <div className="relative">
                <input
                  name="email"
                  type="email"
                  placeholder="you@cit.edu"
                  value={form.email}
                  onChange={handleChange}
                  className={`input-field pl-10 ${errors.email ? 'input-error' : ''}`}
                />
                <svg className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                    d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              {errors.email && <p className="error-text">{errors.email}</p>}
            </div>

            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input
                  name="password"
                  type="password"
                  placeholder="Your password"
                  value={form.password}
                  onChange={handleChange}
                  className={`input-field pl-10 ${errors.password ? 'input-error' : ''}`}
                />
                <svg className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              {errors.password && <p className="error-text">{errors.password}</p>}
              <div className="flex justify-end mt-1">
                <button
                  type="button"
                  onClick={() => { setShowForgot(true); setForgotMsg(''); setForgotError('') }}
                  className="text-xs font-medium hover:underline"
                  style={{ color: '#BE1B39' }}
                >
                  Forgot password?
                </button>
              </div>
            </div>

            <button type="submit" className="btn-primary" disabled={loading}>
              {loading
                ? <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                    </svg>
                    Signing in…
                  </span>
                : 'Sign In'
              }
            </button>
          </form>

          <div className="divider">
            <div className="divider-line" />
            <span className="text-xs text-gray-400 font-medium">OR</span>
            <div className="divider-line" />
          </div>

          <div className="flex justify-center mb-6">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => setApiError('Google login failed. Please try again.')}
              text="signin_with"
              theme="outline"
              size="large"
            />
          </div>

          <p className="text-center text-sm text-gray-600">
            Don't have an account?{' '}
            <Link to="/register" className="font-semibold hover:underline" style={{ color: '#BE1B39' }}>
              Create one
            </Link>
          </p>
        </div>
      </div>

      {/* ── Forgot Password Modal ───────────────────────────────────────── */}
      {showForgot && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-8">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-xl font-black text-gray-900">Reset Password</h3>
              <button
                onClick={() => setShowForgot(false)}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <p className="text-sm text-gray-500 mb-5">
              Enter your registered email and we'll send you a link to reset your password.
            </p>

            {forgotMsg ? (
              <div className="flex items-start gap-2.5 bg-green-50 border border-green-200 text-green-700 text-sm rounded-xl px-4 py-3 mb-4">
                <svg className="w-4 h-4 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                {forgotMsg}
              </div>
            ) : (
              <form onSubmit={handleForgotSubmit} className="space-y-4">
                {forgotError && (
                  <div className="flex items-start gap-2.5 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3">
                    <svg className="w-4 h-4 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                    </svg>
                    {forgotError}
                  </div>
                )}
                <div className="relative">
                  <input
                    type="email"
                    placeholder="you@cit.edu"
                    value={forgotEmail}
                    onChange={e => setForgotEmail(e.target.value)}
                    className="input-field pl-10"
                  />
                  <svg className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                      d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </div>
                <button type="submit" className="btn-primary" disabled={forgotLoading}>
                  {forgotLoading
                    ? <span className="flex items-center justify-center gap-2">
                        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                        </svg>
                        Sending…
                      </span>
                    : 'Send Reset Link'
                  }
                </button>
              </form>
            )}

            {forgotMsg && (
              <button
                onClick={() => setShowForgot(false)}
                className="w-full mt-3 text-sm font-medium text-gray-500 hover:text-gray-700 transition"
              >
                Close
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
