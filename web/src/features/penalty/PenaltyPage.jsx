import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { penaltyService } from './api'
import { paymentService } from '../payment/api'

export default function PenaltyPage() {
  const navigate = useNavigate()
  const [summary,     setSummary]     = useState(null)
  const [loading,     setLoading]     = useState(true)
  const [confirmModal, setConfirm]    = useState(null)
  const [processing,  setProcessing]  = useState({})
  const [error,       setError]       = useState('')

  const user = JSON.parse(localStorage.getItem('user') || '{}')

  useEffect(() => {
    if (!user.id) { navigate('/login'); return }
    fetchPenalties()
  }, [])

  async function fetchPenalties() {
    setLoading(true)
    try {
      const res = await penaltyService.getUserPenalties(user.id)
      setSummary(res.data)
    } catch {
      setError('Failed to load penalties.')
    } finally {
      setLoading(false)
    }
  }

  async function handlePayFine(penalty) {
    setProcessing(p => ({ ...p, [penalty.id]: true }))
    setError('')
    try {
      const res = await paymentService.initiate(penalty.id)
      setConfirm({ penalty, paymentId: res.data.id, checkoutUrl: res.data.checkoutUrl })
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to initiate payment.')
    } finally {
      setProcessing(p => ({ ...p, [penalty.id]: false }))
    }
  }

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 rounded-full border-4 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
    </div>
  )

  const penalties = summary?.penalties ?? []
  const hasPenalties = summary?.totalPoints > 0

  return (
    <div className="p-6 space-y-5 max-w-4xl mx-auto">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">My Penalties</h1>
        <p className="text-sm text-gray-500 mt-0.5">View and pay outstanding penalty fines</p>
      </div>

      {error && (
        <div className="alert-error">
          {error}
          <button onClick={() => setError('')} className="ml-auto text-xs underline">Dismiss</button>
        </div>
      )}

      {/* Summary card */}
      <div className="rounded-2xl overflow-hidden"
        style={{ background: hasPenalties
          ? 'linear-gradient(135deg, #BE1B39 0%, #8C1229 100%)'
          : 'linear-gradient(135deg, #059669 0%, #047857 100%)' }}>
        <div className="px-7 py-6 relative overflow-hidden">
          <div className="absolute right-4 top-4 opacity-10 text-8xl font-black text-white">
            {hasPenalties ? '⚠' : '✓'}
          </div>
          <p className="text-sm font-semibold mb-1" style={{ color: 'rgba(255,255,255,0.7)' }}>
            Total Unpaid Penalty Points
          </p>
          <p className="text-5xl font-black text-white">{summary?.totalPoints ?? 0}</p>
          <p className="text-sm mt-2" style={{ color: 'rgba(255,255,255,0.7)' }}>
            {hasPenalties
              ? `Equivalent to ₱${(summary.totalPoints * 50).toLocaleString()}.00 — payable via GCash or Maya`
              : 'All penalties have been cleared. Great job!'}
          </p>
        </div>
      </div>

      {/* Penalties table */}
      {penalties.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-10 text-center">
          <div className="text-5xl mb-3">🎉</div>
          <p className="font-semibold text-gray-700">No penalty records</p>
          <p className="text-sm text-gray-400 mt-1">You have no penalties at the moment.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-gray-100">
            <h3 className="font-bold text-gray-900">Penalty Records</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead style={{ background: '#F7F5F6' }}>
                <tr className="text-left">
                  {['Item', 'Days Overdue', 'Penalty Pts', 'Amount', 'Status', ''].map(h => (
                    <th key={h} className="px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {penalties.map(p => (
                  <tr key={p.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-5 py-3.5 font-semibold text-gray-800">{p.itemName}</td>
                    <td className="px-5 py-3.5 text-gray-600">{p.daysOverdue} days</td>
                    <td className="px-5 py-3.5">
                      <span className="font-bold" style={{ color: '#BE1B39' }}>{p.penaltyPoints} pts</span>
                    </td>
                    <td className="px-5 py-3.5 font-semibold text-gray-800">
                      ₱{(p.penaltyPoints * 50).toLocaleString()}.00
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={p.paid ? 'badge-approved' : 'badge-rejected'}>
                        {p.paid ? 'PAID' : 'UNPAID'}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      {!p.paid && (
                        <button
                          onClick={() => handlePayFine(p)}
                          disabled={processing[p.id]}
                          className="px-4 py-1.5 text-xs font-semibold text-white rounded-xl transition-colors disabled:opacity-60"
                          style={{ background: '#BE1B39' }}
                          onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
                          onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}
                        >
                          {processing[p.id] ? 'Processing…' : 'Pay Fine'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Payment modal */}
      {confirmModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #F4C430)' }} />
            <div className="p-6">
              <h3 className="text-lg font-bold text-gray-900 mb-1">Pay Penalty Fine</h3>
              <p className="text-sm text-gray-500 mb-5">
                Settling penalty for <strong>{confirmModal.penalty.itemName}</strong>
              </p>

              {/* Amount summary */}
              <div className="rounded-xl p-4 mb-5 space-y-2" style={{ background: '#F7F5F6' }}>
                {[
                  ['Penalty Points', `${confirmModal.penalty.penaltyPoints} pts`],
                  ['Days Overdue',   confirmModal.penalty.daysOverdue],
                  ['Amount Due',     `₱${(confirmModal.penalty.penaltyPoints * 50).toLocaleString()}.00`],
                ].map(([k, v], i) => (
                  <div key={k} className={`flex justify-between text-sm ${i === 2 ? 'border-t border-gray-200 pt-2 font-bold' : ''}`}>
                    <span className="text-gray-500">{k}</span>
                    <span className={i === 2 ? 'text-gray-900' : 'font-semibold text-gray-700'}>{v}</span>
                  </div>
                ))}
              </div>

              {/* Payment method icons */}
              <div className="flex items-center justify-center gap-4 mb-5">
                <div className="flex flex-col items-center gap-1.5">
                  <div className="w-14 h-14 rounded-2xl flex items-center justify-center text-white font-bold" style={{ background: '#1473E6' }}>
                    <span className="text-lg">G</span>
                  </div>
                  <span className="text-xs font-medium text-gray-500">GCash</span>
                </div>
                <div className="text-gray-300 font-light text-xl">|</div>
                <div className="flex flex-col items-center gap-1.5">
                  <div className="w-14 h-14 rounded-2xl flex items-center justify-center text-white font-bold" style={{ background: '#00C27C' }}>
                    <span className="text-lg">M</span>
                  </div>
                  <span className="text-xs font-medium text-gray-500">Maya</span>
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setConfirm(null)}
                  className="flex-1 py-2.5 rounded-xl border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={() => window.location.href = confirmModal.checkoutUrl}
                  className="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white transition-colors"
                  style={{ background: '#1473E6' }}
                  onMouseEnter={e => e.currentTarget.style.background = '#0D62C5'}
                  onMouseLeave={e => e.currentTarget.style.background = '#1473E6'}
                >
                  Proceed to Pay
                </button>
              </div>

              <p className="text-center text-xs text-gray-400 mt-3">
                Powered by PayMongo · Sandbox Mode
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
