import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { authService } from '../services/api'
import TechLoanLogo from '../components/TechLoanLogo'

const ROLES = ['STUDENT', 'FACULTY', 'CUSTODIAN']

export default function RegisterPage() {
  const navigate = useNavigate()

  const [form, setForm] = useState({
    fullName: '', email: '', studentId: '',
    password: '', confirmPassword: '', role: 'STUDENT'
  })
  const [errors, setErrors]     = useState({})
  const [apiError, setApiError] = useState('')
  const [loading, setLoading]   = useState(false)
  const [success, setSuccess]   = useState(false)
  const [selectedRoleForGoogle, setSelectedRoleForGoogle] = useState('STUDENT')
  const [showRoleModal, setShowRoleModal] = useState(false)
  const [googleIdToken, setGoogleIdToken] = useState(null)

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
    setApiError('')
  }

  function validate() {
    const errs = {}
    if (!form.fullName.trim())
      errs.fullName = 'Full name is required'
    else if (form.fullName.trim().length < 2)
      errs.fullName = 'Full name must be at least 2 characters'

    if (!form.email)
      errs.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email))
      errs.email = 'Invalid email format'
    else if (!form.email.toLowerCase().endsWith('@cit.edu'))
      errs.email = 'Must be a CIT-U institutional email (@cit.edu)'

    if (!form.studentId.trim())
      errs.studentId = 'Student/Faculty ID is required'

    if (!form.password)
      errs.password = 'Password is required'
    else if (form.password.length < 8)
      errs.password = 'Password must be at least 8 characters'

    if (!form.confirmPassword)
      errs.confirmPassword = 'Please confirm your password'
    else if (form.password !== form.confirmPassword)
      errs.confirmPassword = 'Passwords do not match'

    if (!form.role)
      errs.role = 'Role is required'

    return errs;
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }

    setLoading(true)
    setApiError('')

    try {
      const res = await authService.register(form)
      const { token, refreshToken, user } = res.data

      // Store tokens and user info
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))

      setSuccess(true)
      setTimeout(() => navigate('/dashboard'), 1500)
    } catch (err) {
      const msg = err.response?.data?.error?.message
        || 'Registration failed. Please try again.'
      setApiError(msg)
    } finally {
      setLoading(false)
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    // Store the ID token and show role selection modal
    setGoogleIdToken(credentialResponse.credential)
    setShowRoleModal(true)
  }

  async function completeGoogleRegistration() {
    setLoading(true)
    setApiError('')

    try {
      const res = await authService.googleRegister(googleIdToken, selectedRoleForGoogle)
      const { token, refreshToken, user } = res.data

      // Store tokens and user info
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))

      setSuccess(true)
      setTimeout(() => navigate('/dashboard'), 1500)
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Google registration failed'
      setApiError(msg)
    } finally {
      setLoading(false)
      setShowRoleModal(false)
    }
  }

  function handleGoogleError() {
    setApiError('Google registration failed. Please try again.')
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
              Create Account
            </h1>
            <p className="text-center text-gray-500 text-sm mb-8">
              Join TechLoan today and manage your loans
            </p>

            {/* Success Message */}
            {success && (
              <div className="alert-success mb-5">
                <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                <span>Account created! Redirecting...</span>
              </div>
            )}

            {/* API Error */}
            {apiError && (
              <div className="alert-error mb-5">
                <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <span>{apiError}</span>
              </div>
            )}

            {/* Registration Form */}
            <form onSubmit={handleSubmit} className="space-y-4">

              {/* Full Name */}
              <div>
                <label className="label">Full Name</label>
                <div className="relative">
                  <input
                    name="fullName"
                    type="text"
                    placeholder="Juan Dela Cruz"
                    value={form.fullName}
                    onChange={handleChange}
                    className={`input-field pl-10 ${errors.fullName ? 'input-error' : ''}`}
                  />
                  <svg className="absolute left-3 top-3 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                {errors.fullName && <p className="error-text">{errors.fullName}</p>}
              </div>

              {/* Email */}
              <div>
                <label className="label">Institutional Email</label>
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

              {/* Student ID */}
              <div>
                <label className="label">Student / Faculty ID</label>
                <div className="relative">
                  <input
                    name="studentId"
                    type="text"
                    placeholder="21-2324-232"
                    value={form.studentId}
                    onChange={handleChange}
                    className={`input-field pl-10 ${errors.studentId ? 'input-error' : ''}`}
                  />
                  <svg className="absolute left-3 top-3 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 6H5a2 2 0 00-2 2v10a2 2 0 002 2h5m0-12h5a2 2 0 012 2v10a2 2 0 01-2 2h-5m0-12V9a2 2 0 010-4h0a2 2 0 010 4v3" />
                  </svg>
                </div>
                {errors.studentId && <p className="error-text">{errors.studentId}</p>}
              </div>

              {/* Role */}
              <div>
                <label className="label">Account Type</label>
                <select
                  name="role"
                  value={form.role}
                  onChange={handleChange}
                  className={`input-field bg-white cursor-pointer ${errors.role ? 'input-error' : ''}`}
                >
                  <option value="">Select a role</option>
                  {ROLES.map(r => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
                {errors.role && <p className="error-text">{errors.role}</p>}
              </div>

              {/* Password */}
              <div>
                <label className="label">Password</label>
                <div className="relative">
                  <input
                    name="password"
                    type="password"
                    placeholder="Min. 8 characters"
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

              {/* Confirm Password */}
              <div>
                <label className="label">Confirm Password</label>
                <div className="relative">
                  <input
                    name="confirmPassword"
                    type="password"
                    placeholder="Re-enter your password"
                    value={form.confirmPassword}
                    onChange={handleChange}
                    className={`input-field pl-10 ${errors.confirmPassword ? 'input-error' : ''}`}
                  />
                  <svg className="absolute left-3 top-3 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                {errors.confirmPassword && <p className="error-text">{errors.confirmPassword}</p>}
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                className="btn-primary mt-6"
                disabled={loading || success}
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                    </svg>
                    Creating account...
                  </span>
                ) : 'Create Account'}
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
                text="signup_with"
                theme="outline"
                size="large"
              />
            </div>

            {/* Sign In Link */}
            <p className="text-center text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-primary font-semibold hover:text-primary-light transition-colors">
                Sign In
              </Link>
            </p>
          </div>
        </div>

        {/* Footer Note */}
        <p className="text-center text-xs text-gray-500 mt-6">
          By registering, you agree to our Terms of Service
        </p>
      </div>

      {/* Role Selection Modal for Google */}
      {showRoleModal && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4 z-50">
          <div className="card-elevated max-w-sm w-full">
            <div className="bg-gradient-to-r from-primary to-primary-light h-1 -m-6 mb-4 rounded-t-2xl" />
            
            <h3 className="text-lg font-bold text-gray-900 mb-2">Select Your Role</h3>
            <p className="text-gray-600 text-sm mb-6">
              Please select your role to complete registration.
            </p>

            <div className="space-y-3 mb-6">
              {ROLES.map(role => (
                <button
                  key={role}
                  onClick={() => setSelectedRoleForGoogle(role)}
                  className={`w-full px-4 py-3 rounded-lg border-2 transition-all text-sm font-medium ${
                    selectedRoleForGoogle === role
                      ? 'border-primary bg-primary text-white shadow-md'
                      : 'border-gray-300 text-gray-700 hover:border-primary hover:bg-gray-50'
                  }`}
                >
                  {role}
                </button>
              ))}
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowRoleModal(false)
                  setGoogleIdToken(null)
                  setSelectedRoleForGoogle('STUDENT')
                }}
                className="flex-1 btn-secondary py-2"
              >
                Cancel
              </button>
              <button
                onClick={async () => await completeGoogleRegistration()}
                disabled={loading}
                className="flex-1 btn-primary py-2"
              >
                {loading ? 'Creating...' : 'Continue'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
