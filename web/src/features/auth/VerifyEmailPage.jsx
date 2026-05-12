import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { authService } from './api'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const [status, setStatus] = useState('verifying') // verifying | success | error
  const [message, setMessage] = useState('')

  useEffect(() => {
    const token = searchParams.get('token')
    if (!token) {
      setStatus('error')
      setMessage('No verification token found in the link.')
      return
    }
    authService.verifyEmail(token)
      .then(() => setStatus('success'))
      .catch(e => {
        setStatus('error')
        setMessage(e?.response?.data?.error?.message || 'Invalid or expired verification link.')
      })
  }, [searchParams])

  return (
    <div className="min-h-screen flex items-center justify-center px-4"
      style={{ background: 'linear-gradient(135deg, #FDF2F4 0%, #fff 60%)' }}>
      <div className="w-full max-w-md text-center">

        <div className="flex items-center justify-center gap-3 mb-10">
          <div className="w-11 h-11 rounded-xl flex items-center justify-center" style={{ background: '#BE1B39' }}>
            <span className="font-black text-white text-xl">T</span>
          </div>
          <p className="font-bold text-gray-900 text-xl">TechLoan</p>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          {status === 'verifying' && (
            <>
              <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-5" style={{ background: '#FDF2F4' }}>
                <div className="w-7 h-7 rounded-full border-4 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
              </div>
              <h2 className="text-xl font-black text-gray-900 mb-2">Verifying your email…</h2>
              <p className="text-sm text-gray-500">Please wait a moment.</p>
            </>
          )}

          {status === 'success' && (
            <>
              <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-5" style={{ background: '#ECFDF5' }}>
                <svg className="w-7 h-7 text-emerald-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h2 className="text-xl font-black text-gray-900 mb-2">Email Verified!</h2>
              <p className="text-sm text-gray-500 mb-6">Your account is now active. You can log in to TechLoan.</p>
              <Link
                to="/login"
                className="inline-block w-full py-3 text-sm font-semibold text-white rounded-xl transition-colors"
                style={{ background: '#BE1B39' }}
                onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
                onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}
              >
                Go to Login
              </Link>
            </>
          )}

          {status === 'error' && (
            <>
              <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-5" style={{ background: '#FDF2F4' }}>
                <svg className="w-7 h-7" fill="none" stroke="#BE1B39" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h2 className="text-xl font-black text-gray-900 mb-2">Verification Failed</h2>
              <p className="text-sm text-gray-500 mb-6">{message}</p>
              <Link
                to="/register"
                className="inline-block w-full py-3 text-sm font-semibold text-white rounded-xl transition-colors"
                style={{ background: '#BE1B39' }}
              >
                Back to Register
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
