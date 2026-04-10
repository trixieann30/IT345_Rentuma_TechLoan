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
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-gray-50 to-primary-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Main Card */}
        <div className="card-elevated">
          {/* Header with gradient */}
          <div className="bg-gradient-to-r from-primary to-primary-light h-1" />
          
          <div className="px-8 py-10">
            <div className="flex justify-center mb-6">
              <TechLoanLogo />
            </div>

            <h1 className="text-2xl font-bold text-center text-gray-900 mb-2">
              Welcome Back
            </h1>
            <p className="text-center text-gray-500 text-sm mb-8">
              Sign in to your TechLoan account
            </p>

            {/* API Error Alert */}
            {apiError && (
              <div className="alert-error mb-5">
                <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <span>{apiError}</span>
              </div>
            )}

            {/* Success Alert */}
            {successMessage && (
              <div className="alert-success mb-5">
                <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                <span>{successMessage}</span>
              </div>
            )}

            {/* Login Form */}
            <form onSubmit={handleSubmit} className="space-y-5">
              {/* Email Field */}
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
                  <svg className="absolute left-3 top-3 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </div>
                {errors.email && <p className="error-text">{errors.email}</p>}
              </div>

              {/* Password Field */}
              <div>
                <label className="label">Password</label>
                <div className="relative">
                  <input
                    name="password"
                    type="password"
                    placeholder="••••••••"
                    value={form.password}
                    onChange={handleChange}
                    className={`input-field pl-10 ${errors.password ? 'input-error' : ''}`}
                  />
                  <svg className="absolute left-3 top-3 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                {errors.password && <p className="error-text">{errors.password}</p>}
              </div>

              {/* Submit Button */}
              <button type="submit" className="btn-primary mt-7" disabled={loading}>
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                    </svg>
                    Signing in...
                  </span>
                ) : 'Sign In'}
              </button>
            </form>

            {/* Divider */}
            <div className="divider">
              <div className="divider-line" />
              <span className="text-gray-400 text-xs font-medium">OR</span>
              <div className="divider-line" />
            </div>

            {/* Google OAuth Button */}
            <div className="flex justify-center mb-6">
              <GoogleLogin
                onSuccess={handleGoogleSuccess}
                onError={handleGoogleError}
                text="signin_with"
                theme="outline"
                size="large"
              />
            </div>

            {/* Sign Up Link */}
            <p className="text-center text-sm text-gray-600">
              Don't have an account?{' '}
              <Link to="/register" className="text-primary font-semibold hover:text-primary-light transition-colors">
                Create Account
              </Link>
            </p>
          </div>
        </div>

        {/* Footer Note */}
        <p className="text-center text-xs text-gray-500 mt-6">
          By signing in, you agree to our Terms of Service
        </p>
      </div>
    </div>
  )
}
/*******  c4519446-268d-4e64-bc29-fd43f6c1c0f7  *******/
