import { useState, useEffect } from 'react'
import { reservationService } from '../../services/api'

const STATUS_FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'RETURNED']

export default function ReservationQueue() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [error, setError] = useState('')

  // Reject modal
  const [rejectTarget, setRejectTarget] = useState(null)
  const [rejectReason, setRejectReason] = useState('')
  const [rejecting, setRejecting] = useState(false)

  // Action loading per row
  const [actionLoading, setActionLoading] = useState({})

  useEffect(() => { fetchReservations() }, [statusFilter])

  const fetchReservations = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await reservationService.getReservations(
        statusFilter === 'ALL' ? null : statusFilter
      )
      setReservations(res.data)
    } catch {
      setError('Failed to load reservations.')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (id) => {
    setActionLoading(prev => ({ ...prev, [id]: 'approving' }))
    try {
      await reservationService.approveReservation(id)
      await fetchReservations()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to approve reservation.')
    } finally {
      setActionLoading(prev => ({ ...prev, [id]: null }))
    }
  }

  const handleReject = async () => {
    if (!rejectTarget) return
    setRejecting(true)
    try {
      await reservationService.rejectReservation(rejectTarget.id, rejectReason)
      setRejectTarget(null)
      setRejectReason('')
      await fetchReservations()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to reject reservation.')
    } finally {
      setRejecting(false)
    }
  }

  const handleReturn = async (id) => {
    setActionLoading(prev => ({ ...prev, [id]: 'returning' }))
    try {
      await reservationService.returnReservation(id)
      await fetchReservations()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to mark as returned.')
    } finally {
      setActionLoading(prev => ({ ...prev, [id]: null }))
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <h1 className="text-2xl font-bold text-primary">Reservation Queue</h1>
        <p className="text-sm text-gray-500 mt-0.5">
          Manage student and faculty borrowing requests
        </p>
      </div>

      {/* Status filter tabs */}
      <div className="px-6 pt-4 flex gap-2 flex-wrap">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`px-4 py-2 rounded-full text-sm font-semibold transition-colors ${
              statusFilter === s
                ? 'bg-primary text-white'
                : 'bg-white text-gray-600 border border-gray-200 hover:border-primary hover:text-primary'
            }`}
          >
            {s}
          </button>
        ))}
      </div>

      {/* Error */}
      {error && (
        <div className="mx-6 mt-4 alert-error">
          <span>⚠</span> {error}
          <button className="ml-auto text-xs underline" onClick={() => setError('')}>Dismiss</button>
        </div>
      )}

      {/* Table */}
      <div className="px-6 py-4 pb-10">
        {loading ? (
          <div className="text-center py-20 text-gray-400">Loading reservations...</div>
        ) : reservations.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            No {statusFilter !== 'ALL' ? statusFilter.toLowerCase() : ''} reservations found.
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Student/Faculty</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Item</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Qty</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Purpose</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Return Date</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Status</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {reservations.map(r => (
                  <tr key={r.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-3">
                      <div className="font-semibold text-gray-800">{r.borrowerName || r.userEmail}</div>
                      <div className="text-xs text-gray-400">{r.userEmail}</div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="font-medium text-gray-700">{r.itemName}</div>
                      {r.itemDescription && (
                        <div className="text-xs text-gray-400 truncate max-w-[160px]">{r.itemDescription}</div>
                      )}
                    </td>
                    <td className="px-4 py-3 text-gray-700">{r.quantity}</td>
                    <td className="px-4 py-3 text-gray-600 max-w-[200px]">
                      <span className="line-clamp-2">{r.purpose || '—'}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-700 whitespace-nowrap">
                      {r.returnDate || r.dueDate || '—'}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={r.status} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2 flex-wrap">
                        {r.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleApprove(r.id)}
                              disabled={!!actionLoading[r.id]}
                              className="px-3 py-1.5 text-xs font-semibold bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-60 transition-colors"
                            >
                              {actionLoading[r.id] === 'approving' ? '...' : '✓ Approve'}
                            </button>
                            <button
                              onClick={() => { setRejectTarget(r); setRejectReason('') }}
                              disabled={!!actionLoading[r.id]}
                              className="px-3 py-1.5 text-xs font-semibold bg-red-50 text-red-600 border border-red-200 rounded-lg hover:bg-red-100 disabled:opacity-60 transition-colors"
                            >
                              ✕ Reject
                            </button>
                          </>
                        )}
                        {r.status === 'APPROVED' && (
                          <button
                            onClick={() => handleReturn(r.id)}
                            disabled={!!actionLoading[r.id]}
                            className="px-3 py-1.5 text-xs font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-60 transition-colors"
                          >
                            {actionLoading[r.id] === 'returning' ? '...' : '↩ Mark Returned'}
                          </button>
                        )}
                        {(r.status === 'REJECTED' || r.status === 'RETURNED') && (
                          <span className="text-xs text-gray-400 italic">No actions</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Reject Modal ── */}
      {rejectTarget && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <h2 className="text-lg font-bold text-gray-800 mb-1">Reject Reservation</h2>
            <p className="text-sm text-gray-500 mb-4">
              Rejecting <strong>{rejectTarget.itemName}</strong> requested by{' '}
              <strong>{rejectTarget.borrowerName || rejectTarget.userEmail}</strong>.
            </p>
            <label className="label">Reason for rejection</label>
            <textarea
              className="input-field h-24 resize-none"
              placeholder="e.g. Item is currently under maintenance..."
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
            />
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => { setRejectTarget(null); setRejectReason('') }}
                className="flex-1 px-4 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleReject}
                disabled={rejecting}
                className="flex-1 px-4 py-2.5 text-sm font-semibold text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors disabled:opacity-60"
              >
                {rejecting ? 'Rejecting...' : 'Confirm Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function StatusBadge({ status }) {
  const map = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    APPROVED: 'bg-green-100 text-green-700',
    REJECTED: 'bg-red-100 text-red-600',
    RETURNED: 'bg-blue-100 text-blue-700',
    OVERDUE: 'bg-orange-100 text-orange-700',
  }
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${map[status] || 'bg-gray-100 text-gray-600'}`}>
      {status}
    </span>
  )
}