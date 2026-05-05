import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { penaltyService } from './api'
import { paymentService } from '../payment/api'

export default function PenaltyPage() {
  const navigate = useNavigate()
  const [summary, setSummary]       = useState(null)
  const [loading, setLoading]       = useState(true)
  const [confirmModal, setConfirm]  = useState(null)  // { penalty, paymentId }
  const [processing, setProcessing] = useState({})
  const [error, setError]           = useState('')
  const [success, setSuccess]       = useState('')

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
      setConfirm({ penalty, paymentId: res.data.id })
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to initiate payment.')
    } finally {
      setProcessing(p => ({ ...p, [penalty.id]: false }))
    }
  }

  async function handleConfirmPayment() {
    if (!confirmModal) return
    setProcessing(p => ({ ...p, confirm: true }))
    try {
      await paymentService.confirm(confirmModal.paymentId)
      setConfirm(null)
      setSuccess('Payment confirmed! Penalty marked as paid.')
      await fetchPenalties()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to confirm payment.')
      setConfirm(null)
    } finally {
      setProcessing(p => ({ ...p, confirm: false }))
    }
  }

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="animate-spin rounded-full h-10 w-10 border-4 border-gray-200 border-t-primary" />
    </div>
  )

  const penalties = summary?.penalties ?? []

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center gap-4">
        <Link to="/dashboard" className="text-primary text-sm font-semibold hover:underline">← Dashboard</Link>
        <h1 className="text-2xl font-bold text-primary">My Penalties</h1>
      </div>

      <div className="max-w-3xl mx-auto px-4 py-8 space-y-6">

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm flex justify-between">
            <span>⚠ {error}</span>
            <button onClick={() => setError('')} className="underline text-xs">Dismiss</button>
          </div>
        )}
        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 rounded-xl px-4 py-3 text-sm flex justify-between">
            <span>✓ {success}</span>
            <button onClick={() => setSuccess('')} className="underline text-xs">Dismiss</button>
          </div>
        )}

        {/* Summary Card */}
        <div className={`rounded-2xl shadow p-6 ${summary?.totalPoints > 0 ? 'bg-red-50' : 'bg-green-50'}`}>
          <p className="text-sm text-gray-500 mb-1">Total Unpaid Penalty Points</p>
          <p className={`text-4xl font-bold ${summary?.totalPoints > 0 ? 'text-red-600' : 'text-green-600'}`}>
            {summary?.totalPoints ?? 0}
          </p>
          {summary?.totalPoints === 0 && (
            <p className="text-green-600 text-sm mt-2 font-medium">All penalties have been cleared.</p>
          )}
        </div>

        {/* Penalties Table */}
        {penalties.length === 0 ? (
          <div className="bg-white rounded-2xl shadow p-10 text-center text-gray-400 text-sm">
            No penalty records found.
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr className="text-left text-gray-400">
                  <th className="px-5 py-3 font-semibold">Item</th>
                  <th className="px-5 py-3 font-semibold">Days Overdue</th>
                  <th className="px-5 py-3 font-semibold">Points</th>
                  <th className="px-5 py-3 font-semibold">Status</th>
                  <th className="px-5 py-3 font-semibold"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {penalties.map(p => (
                  <tr key={p.id}>
                    <td className="px-5 py-3 font-medium text-gray-800">{p.itemName}</td>
                    <td className="px-5 py-3 text-gray-600">{p.daysOverdue}</td>
                    <td className="px-5 py-3 font-bold text-red-600">{p.penaltyPoints}</td>
                    <td className="px-5 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                        p.paid ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                      }`}>
                        {p.paid ? 'PAID' : 'UNPAID'}
                      </span>
                    </td>
                    <td className="px-5 py-3">
                      {!p.paid && (
                        <button
                          onClick={() => handlePayFine(p)}
                          disabled={processing[p.id]}
                          className="px-3 py-1.5 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-red-800 transition-colors disabled:opacity-60"
                        >
                          {processing[p.id] ? 'Processing...' : 'Pay Fine'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Confirmation Modal */}
      {confirmModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-2">Confirm Payment</h3>
            <p className="text-sm text-gray-600 mb-4">
              You are about to pay the fine for <strong>{confirmModal.penalty.itemName}</strong>.
            </p>
            <div className="bg-gray-50 rounded-xl p-4 mb-5 space-y-1.5 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-400">Penalty Points</span>
                <span className="font-bold text-red-600">{confirmModal.penalty.penaltyPoints} pts</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Days Overdue</span>
                <span className="font-semibold">{confirmModal.penalty.daysOverdue}</span>
              </div>
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirm(null)}
                className="flex-1 py-2.5 border border-gray-200 rounded-xl text-sm font-semibold text-gray-600 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmPayment}
                disabled={processing.confirm}
                className="flex-1 py-2.5 bg-primary text-white rounded-xl text-sm font-semibold hover:bg-red-800 transition-colors disabled:opacity-60"
              >
                {processing.confirm ? 'Confirming...' : 'Confirm Payment'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
