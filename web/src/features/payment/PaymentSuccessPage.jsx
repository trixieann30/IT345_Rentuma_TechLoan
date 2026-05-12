import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { paymentService } from './api'

export default function PaymentSuccessPage() {
  const [searchParams] = useSearchParams()
  const [status,  setStatus]  = useState('processing')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const paymentId = searchParams.get('id')
    if (!paymentId) { setStatus('error'); setMessage('No payment ID found in the URL.'); return }
    capturePayment(paymentId)
  }, [])

  async function capturePayment(paymentId) {
    try {
      await paymentService.confirm(paymentId)
      setStatus('success')
    } catch (err) {
      setStatus('error')
      setMessage(err.response?.data?.error || 'Failed to verify payment. Please contact support.')
    }
  }

  if (status === 'processing') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4">
        <div className="w-12 h-12 rounded-full border-4 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
        <p className="text-gray-600 font-medium">Verifying your GCash / Maya payment…</p>
        <p className="text-xs text-gray-400">This may take a few seconds</p>
      </div>
    )
  }

  if (status === 'success') {
    return (
      <div className="flex items-center justify-center min-h-[80vh] p-4">
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 max-w-sm w-full overflow-hidden">
          <div className="h-1" style={{ background: 'linear-gradient(90deg, #10B981, #059669)' }} />
          <div className="p-8 text-center">
            <div className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-5"
              style={{ background: '#ECFDF5' }}>
              <svg className="w-8 h-8 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-xl font-black text-gray-900 mb-2">Payment Successful!</h2>
            <p className="text-gray-500 text-sm mb-1">Your GCash / Maya payment has been verified.</p>
            <p className="text-gray-500 text-sm mb-6">The penalty has been cleared from your account.</p>

            <div className="rounded-xl p-4 mb-6 space-y-1" style={{ background: '#F7F5F6' }}>
              <div className="flex items-center justify-center gap-3">
                <div className="w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold" style={{ background: '#1473E6' }}>G</div>
                <span className="text-gray-400 text-sm">via</span>
                <div className="w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold" style={{ background: '#00C27C' }}>M</div>
              </div>
              <p className="text-xs text-gray-400 text-center mt-2">Powered by PayMongo</p>
            </div>

            <Link
              to="/penalties"
              className="btn-primary block text-center py-2.5 text-sm"
            >
              Back to Penalties
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center min-h-[80vh] p-4">
      <div className="bg-white rounded-2xl shadow-md border border-gray-100 max-w-sm w-full overflow-hidden">
        <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #E06060)' }} />
        <div className="p-8 text-center">
          <div className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-5"
            style={{ background: '#FDF2F4' }}>
            <svg className="w-8 h-8" fill="none" stroke="#BE1B39" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <h2 className="text-xl font-black text-gray-900 mb-2">Payment Failed</h2>
          <p className="text-gray-500 text-sm mb-6">{message}</p>
          <Link
            to="/penalties"
            className="btn-secondary block text-center py-2.5 text-sm"
          >
            Back to Penalties
          </Link>
        </div>
      </div>
    </div>
  )
}
