import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { authService } from '../services/api'
import TechLoanLogo from '../components/TechLoanLogo'

/*************  ✨ Windsurf Command 🌟  *************/
/**
 * LoginPage component
 * 
 * This component handles user login and Google OAuth
 * authentication.
 * 
 * @returns {JSX.Element} The rendered component
 */
export default function LoginPage() {
  const navigate = useNavigate()

  /**
   * Form state and validation error state
   * 
   * @type {Object} Form state
   * @property {string} email Email address
   * @property {string} password Password
   */
  const [form, setForm] = useState({ email: '', password: '' })

  /**
   * Validation errors state
   * 
   * @type {Object} Validation errors
   * @property {string} email Email address validation error
   * @property {string} password Password validation error
   */
  const [errors, setErrors] = useState({})

  /**
   * API error state
   * 
   * @type {string} API error message
   */
  const [apiError, setApiError] = useState('')

  /**
   * Success message state
   * 
   * @type {string} Success message
   */
  const [successMessage, setSuccessMessage] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
    setApiError('')
  }

  function validate() {
    const errs = {}
    if (!form.email)    errs.email    = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Invalid email format'
    if (!form.password) errs.password = 'Password is required'
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

      // Store tokens and user info
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))

      // Show success message
      setSuccessMessage(`Welcome back, ${user.fullName}!`)
      
      // Navigate to dashboard after a brief delay to show message
      setTimeout(() => {
        navigate('/dashboard')
      }, 1500)
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Invalid email or password'
      setApiError(msg)
    } finally {
      setLoading(false)
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    setLoading(true)
    setApiError('')

    try {
      const res = await authService.googleLogin(credentialResponse.credential)
      const { token, refreshToken, user } = res.data

      // Store tokens and user info
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))

      // Show success message
      setSuccessMessage(`Welcome back, ${user.fullName}!`)
      
      // Navigate to dashboard after a brief delay
      setTimeout(() => {
        navigate('/dashboard')
      }, 1500)
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Google login failed'
      setApiError(msg)
    } finally {
      setLoading(false)
    }
  }

  function handleGoogleError() {
    setApiError('Google login failed. Please try again.')
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">

          {/* Red top bar */}
          <div className="bg-primary h-2" />

          <div className="px-8 py-10">
            <TechLoanLogo />

            <h2 className="text-xl font-semibold text-gray-800 text-center mt-8 mb-6">
              Log in to your account
            </h2>

            {/* API Error */}
            {apiError && (
              <div className="bg-red-50 border border-red-200 text-red-600 text-sm
                              rounded-lg px-4 py-3 mb-5 flex items-center gap-2">
                <span>⚠</span> {apiError}
              </div>
            )}

            {/* Success Message */}
            {successMessage && (
              <div className="bg-green-50 border border-green-200 text-green-600 text-sm
                              rounded-lg px-4 py-3 mb-5 flex items-center gap-2">
                <span>✓</span> {successMessage}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              {/* Email */}
              <div>
                <label className="label">Email</label>
                <input
                  name="email"
                  type="email"
                  placeholder="you@cit.edu"
                  value={form.email}
                  onChange={handleChange}
                  className={`input-field ${errors.email ? 'input-error' : ''}`}
                />
                {errors.email && <p className="error-text">{errors.email}</p>}
              </div>

              {/* Password */}
              <div>
                <label className="label">Password</label>
                <input
                  name="password"
                  type="password"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={handleChange}
                  className={`input-field ${errors.password ? 'input-error' : ''}`}
                />
                {errors.password && <p className="error-text">{errors.password}</p>}
              </div>

              {/* Submit */}
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10"
                        stroke="currentColor" strokeWidth="4"/>
                      <path className="opacity-75" fill="currentColor"
                        d="M4 12a8 8 0 018-8v8z"/>
                    </svg>
                    Logging in...
                  </span>
                ) : 'Log In'}
              </button>
            </form>

            {/* Divider */}
            <div className="flex items-center gap-3 my-5">
              <hr className="flex-1 border-gray-200" />
              <span className="text-gray-400 text-xs">OR</span>
              <hr className="flex-1 border-gray-200" />
            </div>

            {/* Google OAuth Button */}
            <div className="flex justify-center">
              <GoogleLogin
                onSuccess={handleGoogleSuccess}
                onError={handleGoogleError}
                text="signin_with"
                theme="outline"
                size="large"
              />
            </div>

            <p className="text-center text-sm text-gray-500 mt-6">
              Don't have an account?{' '}
              <Link to="/register" className="text-primary font-semibold hover:underline">
                Register
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
/*******  c4519446-268d-4e64-bc29-fd43f6c1c0f7  *******/
