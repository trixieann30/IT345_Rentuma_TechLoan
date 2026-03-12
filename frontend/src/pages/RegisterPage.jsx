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
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">

          {/* Red top bar */}
          <div className="bg-primary h-2" />

          <div className="px-8 py-10">
            <TechLoanLogo />

            <h2 className="text-xl font-semibold text-gray-800 text-center mt-8 mb-6">
              Create an account
            </h2>

            {/* Success message */}
            {success && (
              <div className="bg-green-50 border border-green-200 text-green-700 text-sm
                              rounded-lg px-4 py-3 mb-5 flex items-center gap-2">
                <span>✓</span> Account created! Redirecting to dashboard...
              </div>
            )}

            {/* API Error */}
            {apiError && (
              <div className="bg-red-50 border border-red-200 text-red-600 text-sm
                              rounded-lg px-4 py-3 mb-5 flex items-center gap-2">
                <span>⚠</span> {apiError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">

              {/* Full Name */}
              <div>
                <label className="label">Full Name</label>
                <input
                  name="fullName"
                  type="text"
                  placeholder="Juan Dela Cruz"
                  value={form.fullName}
                  onChange={handleChange}
                  className={`input-field ${errors.fullName ? 'input-error' : ''}`}
                />
                {errors.fullName && <p className="error-text">{errors.fullName}</p>}
              </div>

              {/* Email */}
              <div>
                <label className="label">Institutional Email</label>
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

              {/* Student ID */}
              <div>
                <label className="label">Student / Faculty ID</label>
                <input
                  name="studentId"
                  type="text"
                  placeholder="21-2324-232"
                  value={form.studentId}
                  onChange={handleChange}
                  className={`input-field ${errors.studentId ? 'input-error' : ''}`}
                />
                {errors.studentId && <p className="error-text">{errors.studentId}</p>}
              </div>

              {/* Role */}
              <div>
                <label className="label">Role</label>
                <select
                  name="role"
                  value={form.role}
                  onChange={handleChange}
                  className={`input-field bg-white ${errors.role ? 'input-error' : ''}`}
                >
                  {ROLES.map(r => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
                {errors.role && <p className="error-text">{errors.role}</p>}
              </div>

              {/* Password */}
              <div>
                <label className="label">Password</label>
                <input
                  name="password"
                  type="password"
                  placeholder="Min. 8 characters"
                  value={form.password}
                  onChange={handleChange}
                  className={`input-field ${errors.password ? 'input-error' : ''}`}
                />
                {errors.password && <p className="error-text">{errors.password}</p>}
              </div>

              {/* Confirm Password */}
              <div>
                <label className="label">Confirm Password</label>
                <input
                  name="confirmPassword"
                  type="password"
                  placeholder="Re-enter your password"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  className={`input-field ${errors.confirmPassword ? 'input-error' : ''}`}
                />
                {errors.confirmPassword &&
                  <p className="error-text">{errors.confirmPassword}</p>}
              </div>

              {/* Submit */}
              <button
                type="submit"
                className="btn-primary mt-2"
                disabled={loading || success}
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10"
                        stroke="currentColor" strokeWidth="4"/>
                      <path className="opacity-75" fill="currentColor"
                        d="M4 12a8 8 0 018-8v8z"/>
                    </svg>
                    Creating account...
                  </span>
                ) : 'Create Account'}
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
                text="signup_with"
                theme="outline"
                size="large"
              />
            </div>

            <p className="text-center text-sm text-gray-500 mt-6">
              Already have an account?{' '}
              <Link to="/login" className="text-primary font-semibold hover:underline">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>

      {/* Role Selection Modal for Google */}
      {showRoleModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center px-4 z-50">
          <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-sm">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">Select Your Role</h3>
            <p className="text-gray-500 text-sm mb-6">
              Please select your role in the institution to complete registration.
            </p>

            <div className="space-y-3 mb-6">
              {ROLES.map(role => (
                <button
                  key={role}
                  onClick={() => setSelectedRoleForGoogle(role)}
                  className={`w-full px-4 py-3 rounded-lg border-2 transition-colors text-sm font-medium ${
                    selectedRoleForGoogle === role
                      ? 'border-primary bg-primary text-white'
                      : 'border-gray-300 text-gray-700 hover:border-primary'
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
                className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={async () => await completeGoogleRegistration()}
                disabled={loading}
                className="flex-1 px-4 py-2.5 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light disabled:opacity-60"
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
