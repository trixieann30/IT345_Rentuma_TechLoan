import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { authService } from './api'

export default function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''

  const [form,     setForm]     = useState({ newPassword: '', confirm: '' })
  const [errors,   setErrors]   = useState({})
  const [apiError, setApiError] = useState('')
  const [success,  setSuccess]  = useState(false)
  const [loading,  setLoading]  = useState(false)

  function validate() {
    const errs = {}
    if (!form.newPassword) errs.newPassword = 'Password is required'
    else if (form.newPassword.length < 6) errs.newPassword = 'Password must be at least 6 characters'
    if (!form.confirm) errs.confirm = 'Please confirm your password'
    else if (form.confirm !== form.newPassword) errs.confirm = 'Passwords do not match'
    return errs
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    if (!token) { setApiError('Invalid or missing reset token.'); return }
    setLoading(true)
    setApiError('')
    try {
      await authService.resetPassword(token, form.newPassword)
      setSuccess(true)
    } catch (err) {
      const msg = err.response?.data?.error?.message || err.response?.data?.message || ''
      setApiError(msg.includes('VALID-005')
        ? 'This reset link has expired or is invalid. Please request a new one.'
        : 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8">

        {/* Brand */}
        <div className="flex items-center gap-2.5 mb-8">
          <div className="w-9 h-9 rounded-xl flex items-center justify-center" style={{ background: '#BE1B39' }}>
            <span className="font-black text-white text-base">T</span>
          </div>
          <p className="font-bold text-gray-900 text-lg">TechLoan</p>
        </div>

        {success ? (
          <div className="text-center">
            <div className="w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4" style={{ background: '#F0FDF4' }}>
              <svg className="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-black text-gray-900 mb-2">Password Reset!</h2>
            <p className="text-gray-500 text-sm mb-6">Your password has been updated. You can now sign in with your new password.</p>
            <button
              onClick={() => navigate('/login')}
              className="btn-primary"
            >
              Go to Login
            </button>
          </div>
        ) : (
          <>
            <h2 className="text-2xl font-black text-gray-900 mb-1">Set New Password</h2>
            <p className="text-gray-500 text-sm mb-6">Enter your new password below.</p>

            {!token && (
              <div className="flex items-start gap-2.5 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-5">
                <svg className="w-4 h-4 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                Invalid reset link. Please request a new one.
              </div>
            )}

            {apiError && (
              <div className="flex items-start gap-2.5 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-5">
                <svg className="w-4 h-4 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                {apiError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="label">New Password</label>
                <div className="relative">
                  <input
                    type="password"
                    placeholder="At least 6 characters"
                    value={form.newPassword}
                    onChange={e => { setForm({ ...form, newPassword: e.target.value }); setErrors({ ...errors, newPassword: '' }) }}
                    className={`input-field pl-10 ${errors.newPassword ? 'input-error' : ''}`}
                  />
                  <svg className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                      d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                {errors.newPassword && <p className="error-text">{errors.newPassword}</p>}
              </div>

              <div>
                <label className="label">Confirm Password</label>
                <div className="relative">
                  <input
                    type="password"
                    placeholder="Repeat new password"
                    value={form.confirm}
                    onChange={e => { setForm({ ...form, confirm: e.target.value }); setErrors({ ...errors, confirm: '' }) }}
                    className={`input-field pl-10 ${errors.confirm ? 'input-error' : ''}`}
                  />
                  <svg className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                      d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                {errors.confirm && <p className="error-text">{errors.confirm}</p>}
              </div>

              <button type="submit" className="btn-primary" disabled={loading || !token}>
                {loading
                  ? <span className="flex items-center justify-center gap-2">
                      <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                      </svg>
                      Resetting…
                    </span>
                  : 'Reset Password'
                }
              </button>
            </form>

            <p className="text-center text-sm text-gray-500 mt-5">
              Remembered it?{' '}
              <Link to="/login" className="font-semibold hover:underline" style={{ color: '#BE1B39' }}>
                Back to Login
              </Link>
            </p>
          </>
        )}
      </div>
    </div>
  )
}
