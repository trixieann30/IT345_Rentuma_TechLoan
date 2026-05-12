import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { authService } from './api'

const ROLES = ['STUDENT', 'FACULTY']

export default function RegisterPage() {
  const navigate  = useNavigate()
  const [form, setForm] = useState({
    fullName: '', email: '', studentId: '', password: '', confirmPassword: '', role: 'STUDENT'
  })
  const [errors,        setErrors]        = useState({})
  const [apiError,      setApiError]      = useState('')
  const [loading,       setLoading]       = useState(false)
  const [success,       setSuccess]       = useState(false)
  const [agreedToTerms, setAgreedToTerms] = useState(false)
  const [showTerms,     setShowTerms]     = useState(false)
  const [needsVerification, setNeedsVerification] = useState(false)
  const [showRoleModal,      setShowRoleModal]      = useState(false)
  const [googleIdToken,      setGoogleIdToken]      = useState(null)
  const [selectedRoleGoogle, setSelectedRoleGoogle] = useState('STUDENT')
  const [citEmail,          setCitEmail]          = useState('')
  const [verificationEmail,  setVerificationEmail]  = useState('')

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
    setApiError('')
  }

  function validate() {
    const errs = {}
    if (!form.fullName.trim() || form.fullName.trim().length < 2)
      errs.fullName = 'Full name must be at least 2 characters'
    if (!form.email) errs.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Invalid email format'
    else if (!form.email.toLowerCase().endsWith('@cit.edu')) errs.email = 'Must be a CIT-U email (@cit.edu)'
    if (!form.studentId.trim()) errs.studentId = 'Student / Faculty ID is required'
    if (!form.password) errs.password = 'Password is required'
    else if (form.password.length < 8) errs.password = 'Password must be at least 8 characters'
    if (!form.confirmPassword) errs.confirmPassword = 'Please confirm your password'
    else if (form.password !== form.confirmPassword) errs.confirmPassword = 'Passwords do not match'
    if (!form.role) errs.role = 'Role is required'
    if (!agreedToTerms) errs.terms = 'You must agree to the Terms and Conditions'
    return errs
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
      if (token) {
        localStorage.setItem('token', token)
        localStorage.setItem('refreshToken', refreshToken)
        localStorage.setItem('user', JSON.stringify(user))
        setSuccess(true)
        setTimeout(() => navigate('/dashboard'), 1500)
      } else {
        setVerificationEmail(form.email)
        setNeedsVerification(true)
      }
    } catch (err) {
      setApiError(err.response?.data?.error?.message || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  async function completeGoogleRegistration() {
    setLoading(true)
    setApiError('')
    try {
      const email = citEmail.trim().toLowerCase()
      if (!email) throw new Error('CIT-U email is required for Google registration.')
      if (!/\S+@\S+\.\S+/.test(email) || !email.endsWith('@cit.edu')) {
        throw new Error('Please enter a valid CIT-U email address.')
      }

      const res = await authService.googleRegister(googleIdToken, selectedRoleGoogle, email)
      const { token, refreshToken, user } = res.data

      // If no token, email verification is pending
      if (!token) {
        setVerificationEmail(email)
        setNeedsVerification(true)
        setShowRoleModal(false)
        return
      }

      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))
      setSuccess(true)
      setTimeout(() => navigate('/dashboard'), 1500)
    } catch (err) {
      setApiError(err.response?.data?.error?.message || err.message || 'Google registration failed')
    } finally {
      setLoading(false)
      setShowRoleModal(false)
    }
  }

  return (
    <div className="min-h-screen flex">

      {/* ── Left Hero Panel ────────────────────────────────────────────── */}
      <div
        className="hidden lg:flex flex-col justify-between w-[40%] p-12 relative overflow-hidden"
        style={{ background: 'linear-gradient(150deg, #BE1B39 0%, #8C1229 45%, #120709 100%)' }}
      >
        <div className="absolute -top-16 -right-16 w-72 h-72 rounded-full opacity-10" style={{ background: '#F4C430' }} />
        <div className="absolute bottom-16 -left-12 w-56 h-56 rounded-full opacity-[0.07]" style={{ background: '#E06060' }} />

        <div className="relative">
          <div className="flex items-center gap-3 mb-12">
            <div className="w-11 h-11 rounded-xl flex items-center justify-center" style={{ background: 'rgba(244,196,48,0.15)', border: '1px solid rgba(244,196,48,0.3)' }}>
              <span className="font-black text-xl" style={{ color: '#F4C430' }}>T</span>
            </div>
            <div>
              <p className="text-white font-bold text-xl leading-tight">TechLoan</p>
              <p className="text-[12px]" style={{ color: '#9B6070' }}>CIT-U Lab Equipment System</p>
            </div>
          </div>

          <h1 className="text-4xl font-black text-white leading-tight mb-4">
            Join the<br />
            <span style={{ color: '#F4C430' }}>TechLoan</span> community
          </h1>
          <p className="text-sm leading-relaxed" style={{ color: '#C09098' }}>
            Create your account to start borrowing lab equipment with ease. Available for CIT-U students and faculty.
          </p>
        </div>

        <div className="relative grid grid-cols-2 gap-3">
          {[
            ['Students', 'Borrow and track equipment'],
            ['Faculty', 'Reserve for research & classes'],
            ['QR Code Slips', 'Digital borrowing slips'],
            ['Free to use', 'No hidden charges'],
          ].map(([title, desc]) => (
            <div key={title} className="rounded-xl p-3" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.06)' }}>
              <p className="text-sm font-semibold text-white">{title}</p>
              <p className="text-xs mt-0.5" style={{ color: '#7A4555' }}>{desc}</p>
            </div>
          ))}
        </div>

        <p className="relative text-xs" style={{ color: '#4A2535' }}>
          © 2025 TechLoan · Cebu Institute of Technology – University
        </p>
      </div>

      {/* ── Right Form Panel ───────────────────────────────────────────── */}
      <div className="flex-1 flex items-center justify-center px-6 py-10 bg-white overflow-y-auto">
        <div className="w-full max-w-[420px]">

          <div className="flex lg:hidden items-center gap-2.5 mb-8">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center" style={{ background: '#BE1B39' }}>
              <span className="font-black text-white text-base">T</span>
            </div>
            <p className="font-bold text-gray-900 text-lg">TechLoan</p>
          </div>

          <h2 className="text-3xl font-black text-gray-900 mb-1">Create account</h2>
          <p className="text-gray-500 text-sm mb-8">Join TechLoan using your CIT-U email</p>

          {success && (
            <div className="alert-success mb-5">
              <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              Account created! Redirecting to dashboard…
            </div>
          )}

          {needsVerification && (
            <div className="mb-5 rounded-2xl border p-5 text-center space-y-3" style={{ background: '#FFFBEB', borderColor: '#FDE68A' }}>
              <div className="w-12 h-12 rounded-2xl flex items-center justify-center mx-auto" style={{ background: '#FEF3C7' }}>
                <svg className="w-6 h-6 text-amber-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <p className="font-bold text-amber-900">Check your email!</p>
              <p className="text-sm text-amber-700">
                We sent a verification link to <strong>{verificationEmail}</strong>. Click it to activate your account before logging in.
              </p>
              <Link to="/login" className="inline-block text-sm font-semibold underline" style={{ color: '#BE1B39' }}>
                Back to Login
              </Link>
            </div>
          )}

          {apiError && (
            <div className="alert-error mb-5">
              <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              {apiError}
            </div>
          )}

          {!needsVerification && <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Full Name</label>
              <input name="fullName" type="text" placeholder="Juan Dela Cruz"
                value={form.fullName} onChange={handleChange}
                className={`input-field ${errors.fullName ? 'input-error' : ''}`} />
              {errors.fullName && <p className="error-text">{errors.fullName}</p>}
            </div>

            <div>
              <label className="label">Institutional Email</label>
              <input name="email" type="email" placeholder="you@cit.edu"
                value={form.email} onChange={handleChange}
                className={`input-field ${errors.email ? 'input-error' : ''}`} />
              {errors.email && <p className="error-text">{errors.email}</p>}
            </div>

            <div>
              <label className="label">Student / Faculty ID</label>
              <input name="studentId" type="text" placeholder="21-2324-232"
                value={form.studentId} onChange={handleChange}
                className={`input-field ${errors.studentId ? 'input-error' : ''}`} />
              {errors.studentId && <p className="error-text">{errors.studentId}</p>}
            </div>

            <div>
              <label className="label">Account Type</label>
              <select name="role" value={form.role} onChange={handleChange}
                className={`input-field bg-white cursor-pointer ${errors.role ? 'input-error' : ''}`}>
                {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              {errors.role && <p className="error-text">{errors.role}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Password</label>
                <input name="password" type="password" placeholder="Min. 8 chars"
                  value={form.password} onChange={handleChange}
                  className={`input-field ${errors.password ? 'input-error' : ''}`} />
                {errors.password && <p className="error-text">{errors.password}</p>}
              </div>
              <div>
                <label className="label">Confirm Password</label>
                <input name="confirmPassword" type="password" placeholder="Repeat password"
                  value={form.confirmPassword} onChange={handleChange}
                  className={`input-field ${errors.confirmPassword ? 'input-error' : ''}`} />
                {errors.confirmPassword && <p className="error-text">{errors.confirmPassword}</p>}
              </div>
            </div>

            <div>
              <label className={`flex items-start gap-3 cursor-pointer select-none ${errors.terms ? 'text-red-600' : 'text-gray-600'}`}>
                <input
                  type="checkbox"
                  checked={agreedToTerms}
                  onChange={e => { setAgreedToTerms(e.target.checked); setErrors({ ...errors, terms: '' }) }}
                  className="mt-0.5 w-4 h-4 rounded accent-red-700 cursor-pointer flex-shrink-0"
                />
                <span className="text-sm leading-snug">
                  I have read and agree to the{' '}
                  <button
                    type="button"
                    onClick={() => setShowTerms(true)}
                    className="font-semibold underline"
                    style={{ color: '#BE1B39' }}
                  >
                    Terms and Conditions
                  </button>
                </span>
              </label>
              {errors.terms && <p className="error-text mt-1">{errors.terms}</p>}
            </div>

            <button type="submit" className="btn-primary mt-2" disabled={loading || success}>
              {loading
                ? <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                    </svg>
                    Creating account…
                  </span>
                : 'Create Account'
              }
            </button>
          </form>}

          {!needsVerification && <>
            <div className="divider">
              <div className="divider-line" />
              <span className="text-xs text-gray-400 font-medium">OR</span>
              <div className="divider-line" />
            </div>

            <div className="flex justify-center mb-6">
              <GoogleLogin
                onSuccess={c => { setGoogleIdToken(c.credential); setShowRoleModal(true) }}
                onError={() => setApiError('Google registration failed. Please try again.')}
                text="signup_with"
                theme="outline"
                size="large"
              />
            </div>

            <p className="text-center text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="font-semibold hover:underline" style={{ color: '#BE1B39' }}>
                Sign In
              </Link>
            </p>
          </>}
        </div>
      </div>

      {/* Terms and Conditions Modal */}
      {showTerms && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-gray-100">
              <h3 className="text-xl font-black text-gray-900">Terms and Conditions</h3>
              <button onClick={() => setShowTerms(false)} className="text-gray-400 hover:text-gray-600 transition">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div className="overflow-y-auto px-6 py-5 text-sm text-gray-600 space-y-4 flex-1">
              <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold">TechLoan — CIT-U Lab Equipment System</p>

              <div>
                <p className="font-semibold text-gray-800 mb-1">1. Eligibility</p>
                <p>Only registered CIT-U students and faculty with a valid institutional email (<em>@cit.edu</em>) may create an account and borrow equipment.</p>
              </div>

              <div>
                <p className="font-semibold text-gray-800 mb-1">2. Equipment Use</p>
                <p>Borrowed equipment must be used solely for academic or research purposes within CIT-U. Lending equipment to third parties is strictly prohibited.</p>
              </div>

              <div>
                <p className="font-semibold text-gray-800 mb-1">3. Returns and Penalties</p>
                <p>Equipment must be returned on or before the agreed due date. Late returns incur <strong>1 penalty point per day overdue</strong>. Each penalty point is equivalent to <strong>₱50.00</strong>. Accounts with unpaid penalties may be restricted from making new reservations.</p>
              </div>

              <div>
                <p className="font-semibold text-gray-800 mb-1">4. Damage and Loss</p>
                <p>Any damage or loss must be reported to the custodian immediately. The borrower is liable for the repair or replacement cost of damaged or lost items.</p>
              </div>

              <div>
                <p className="font-semibold text-gray-800 mb-1">5. Account Responsibility</p>
                <p>You are responsible for all activity under your account. Do not share your login credentials with anyone.</p>
              </div>

              <div>
                <p className="font-semibold text-gray-800 mb-1">6. Policy Changes</p>
                <p>TechLoan reserves the right to update these terms at any time. Continued use of the platform constitutes acceptance of any revised terms.</p>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-gray-100 flex gap-3">
              <button
                onClick={() => { setAgreedToTerms(true); setErrors(e => ({ ...e, terms: '' })); setShowTerms(false) }}
                className="flex-1 btn-primary py-2.5"
              >
                I Agree
              </button>
              <button onClick={() => setShowTerms(false)} className="flex-1 btn-secondary py-2.5">
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Google Role Modal */}
      {showRoleModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center px-4 z-50">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6">
            <div className="w-10 h-10 rounded-xl flex items-center justify-center mb-4" style={{ background: '#FDF2F4' }}>
              <svg className="w-5 h-5" fill="none" stroke="#BE1B39" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-1">Select Your Role</h3>
            <p className="text-sm text-gray-500 mb-5">Choose how you'll use TechLoan.</p>

            <div className="mb-4">
              <label className="label">CIT-U Email</label>
              <input
                type="email"
                value={citEmail}
                onChange={e => setCitEmail(e.target.value)}
                placeholder="you@cit.edu"
                className="input-field"
              />
              <p className="text-xs text-gray-500 mt-2">This CIT-U email will be verified before you can borrow equipment.</p>
            </div>

            <div className="space-y-2 mb-6">
              {ROLES.map(role => (
                <button
                  key={role}
                  onClick={() => setSelectedRoleGoogle(role)}
                  className={`w-full px-4 py-3 rounded-xl border-2 transition-all text-sm font-semibold text-left ${
                    selectedRoleGoogle === role
                      ? 'text-white border-primary'
                      : 'border-gray-200 text-gray-700 hover:border-gray-300'
                  }`}
                  style={selectedRoleGoogle === role ? { background: '#BE1B39', borderColor: '#BE1B39' } : {}}
                >
                  {role}
                </button>
              ))}
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => { setShowRoleModal(false); setGoogleIdToken(null); setSelectedRoleGoogle('STUDENT'); setCitEmail('') }}
                className="flex-1 btn-secondary py-2.5"
              >
                Cancel
              </button>
              <button
                onClick={completeGoogleRegistration}
                disabled={loading}
                className="flex-1 btn-primary py-2.5"
              >
                {loading ? 'Creating…' : 'Continue'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
