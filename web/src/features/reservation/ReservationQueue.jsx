import { useState, useEffect } from 'react'
import { reservationService } from './api'

const STATUS_FILTERS = ['ALL', 'PENDING', 'APPROVED', 'RELEASED', 'REJECTED', 'RETURNED', 'OVERDUE']

const BADGE = {
  PENDING:  'badge-pending',
  APPROVED: 'badge-approved',
  RELEASED: 'badge-released',
  REJECTED: 'badge-rejected',
  RETURNED: 'badge-returned',
  OVERDUE:  'badge-overdue',
}

export default function ReservationQueue() {
  const [reservations, setReservations] = useState([])
  const [loading,      setLoading]      = useState(true)
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [error,        setError]        = useState('')
  const [rejectTarget, setRejectTarget] = useState(null)
  const [rejectReason, setRejectReason] = useState('')
  const [rejecting,    setRejecting]    = useState(false)
  const [actionLoading, setActionLoading] = useState({})

  useEffect(() => { fetchReservations() }, [statusFilter])

  async function fetchReservations() {
    setLoading(true)
    setError('')
    try {
      const res = await reservationService.getReservations(statusFilter === 'ALL' ? null : statusFilter)
      setReservations(res.data)
    } catch {
      setError('Failed to load reservations.')
    } finally {
      setLoading(false)
    }
  }

  async function handleApprove(id) {
    setActionLoading(p => ({ ...p, [id]: 'approving' }))
    try {
      await reservationService.approveReservation(id)
      await fetchReservations()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to approve.')
    } finally {
      setActionLoading(p => ({ ...p, [id]: null }))
    }
  }

  async function handleReject() {
    if (!rejectTarget) return
    setRejecting(true)
    try {
      await reservationService.rejectReservation(rejectTarget.id, rejectReason)
      setRejectTarget(null)
      setRejectReason('')
      await fetchReservations()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to reject.')
    } finally {
      setRejecting(false)
    }
  }

  async function handleRelease(id) {
    setActionLoading(p => ({ ...p, [id]: 'releasing' }))
    try {
      await reservationService.releaseReservation(id)
      setStatusFilter('RELEASED')
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to release item.')
    } finally {
      setActionLoading(p => ({ ...p, [id]: null }))
    }
  }

  async function handleReturn(id) {
    setActionLoading(p => ({ ...p, [id]: 'returning' }))
    try {
      await reservationService.returnReservation(id)
      await fetchReservations()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to mark as returned.')
    } finally {
      setActionLoading(p => ({ ...p, [id]: null }))
    }
  }

  return (
    <div className="p-6 space-y-5 max-w-7xl mx-auto">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">Reservation Queue</h1>
        <p className="text-sm text-gray-500 mt-0.5">Review and manage all borrowing requests</p>
      </div>

      {/* Stats overview */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          { label: 'Pending',  status: 'PENDING',  color: '#F4C430', bg: '#FFFBEB' },
          { label: 'Approved', status: 'APPROVED', color: '#10B981', bg: '#ECFDF5' },
          { label: 'Released', status: 'RELEASED', color: '#8B5CF6', bg: '#F5F3FF' },
          { label: 'Overdue',  status: 'OVERDUE',  color: '#EF4444', bg: '#FEF2F2' },
        ].map(({ label, status, color, bg }) => (
          <button
            key={status}
            onClick={() => setStatusFilter(status)}
            className="rounded-2xl border p-4 text-left transition-all hover:shadow-md"
            style={{
              background: statusFilter === status ? bg : '#fff',
              borderColor: statusFilter === status ? color : '#F3F4F6',
            }}
          >
            <p className="text-2xl font-black" style={{ color }}>
              {reservations.filter(r => r.status === status).length || 0}
            </p>
            <p className="text-xs font-semibold text-gray-500 mt-0.5">{label}</p>
          </button>
        ))}
      </div>

      {/* Filter pills */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex gap-2 flex-wrap">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className="px-4 py-1.5 rounded-xl text-sm font-semibold border transition-all"
            style={statusFilter === s
              ? { background: '#BE1B39', color: '#fff', borderColor: '#BE1B39' }
              : { background: '#fff', color: '#6B7280', borderColor: '#E5E7EB' }}
          >
            {s}
          </button>
        ))}
      </div>

      {error && (
        <div className="alert-error">
          {error}
          <button className="ml-auto text-xs underline" onClick={() => setError('')}>Dismiss</button>
        </div>
      )}

      {/* Table */}
      {loading ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden animate-pulse">
          <div className="h-12" style={{ background: '#F7F5F6' }} />
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex gap-4 px-4 py-4 border-t border-gray-50">
              {[...Array(6)].map((_, j) => <div key={j} className="h-4 bg-gray-100 rounded flex-1" />)}
            </div>
          ))}
        </div>
      ) : reservations.length === 0 ? (
        <div className="text-center py-20">
          <div className="text-5xl mb-3">✅</div>
          <p className="font-semibold text-gray-700">No reservations found</p>
          <p className="text-sm text-gray-400 mt-1">
            {statusFilter !== 'ALL' ? `No ${statusFilter.toLowerCase()} reservations` : 'No reservations yet'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead style={{ background: '#F7F5F6' }}>
                <tr>
                  {['Student / Faculty', 'Item', 'Qty', 'Purpose', 'Return Date', 'Status', 'Actions'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {reservations.map(r => (
                  <tr key={r.id}
                    className="transition-colors"
                    style={r.status === 'OVERDUE'
                      ? { background: '#FEF2F2', borderLeft: '3px solid #EF4444' }
                      : {}
                    }
                    onMouseEnter={e => { if (r.status !== 'OVERDUE') e.currentTarget.style.background = '#F9FAFB' }}
                    onMouseLeave={e => { e.currentTarget.style.background = r.status === 'OVERDUE' ? '#FEF2F2' : '' }}
                  >
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-semibold text-gray-800 text-sm">{r.borrowerName || r.userEmail}</p>
                        {r.borrowerRole && (
                          <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-full"
                            style={{
                              background: r.borrowerRole === 'STUDENT' ? '#EFF6FF' : '#F0FDF4',
                              color: r.borrowerRole === 'STUDENT' ? '#1D4ED8' : '#15803D',
                            }}>
                            {r.borrowerRole}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-gray-400">{r.userEmail}</p>
                    </td>
                    <td className="px-4 py-3.5">
                      <p className="font-medium text-gray-700">{r.itemName}</p>
                      {r.itemDescription && (
                        <p className="text-xs text-gray-400 truncate max-w-[140px]">{r.itemDescription}</p>
                      )}
                    </td>
                    <td className="px-4 py-3.5 text-gray-600 font-medium">{r.quantity}</td>
                    <td className="px-4 py-3.5 text-gray-600 max-w-[180px]">
                      <span className="line-clamp-2 text-xs">{r.purpose || '—'}</span>
                    </td>
                    <td className="px-4 py-3.5 text-gray-700 whitespace-nowrap text-sm font-medium">
                      {r.returnDate || r.dueDate || '—'}
                    </td>
                    <td className="px-4 py-3.5">
                      <span className={BADGE[r.status] || 'badge-pending'}>{r.status}</span>
                    </td>
                    <td className="px-4 py-3.5">
                      <div className="flex gap-1.5 flex-wrap">
                        {r.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleApprove(r.id)}
                              disabled={!!actionLoading[r.id]}
                              className="px-3 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors disabled:opacity-60"
                              style={{ background: '#10B981' }}
                              onMouseEnter={e => e.currentTarget.style.background = '#059669'}
                              onMouseLeave={e => e.currentTarget.style.background = '#10B981'}
                            >
                              {actionLoading[r.id] === 'approving' ? '…' : '✓ Approve'}
                            </button>
                            <button
                              onClick={() => { setRejectTarget(r); setRejectReason('') }}
                              disabled={!!actionLoading[r.id]}
                              className="px-3 py-1.5 text-xs font-semibold rounded-lg border transition-colors disabled:opacity-60"
                              style={{ color: '#BE1B39', background: '#FDF2F4', borderColor: '#FADADF' }}
                              onMouseEnter={e => e.currentTarget.style.background = '#FADADF'}
                              onMouseLeave={e => e.currentTarget.style.background = '#FDF2F4'}
                            >
                              ✕ Reject
                            </button>
                          </>
                        )}
                        {r.status === 'APPROVED' && (
                          <button
                            onClick={() => handleRelease(r.id)}
                            disabled={!!actionLoading[r.id]}
                            className="px-3 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors disabled:opacity-60"
                            style={{ background: '#8B5CF6' }}
                            onMouseEnter={e => e.currentTarget.style.background = '#7C3AED'}
                            onMouseLeave={e => e.currentTarget.style.background = '#8B5CF6'}
                          >
                            {actionLoading[r.id] === 'releasing' ? '…' : '↗ Release'}
                          </button>
                        )}
                        {(r.status === 'RELEASED' || r.status === 'OVERDUE') && (
                          <button
                            onClick={() => handleReturn(r.id)}
                            disabled={!!actionLoading[r.id]}
                            className="px-3 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors disabled:opacity-60"
                            style={{ background: '#3B82F6' }}
                            onMouseEnter={e => e.currentTarget.style.background = '#2563EB'}
                            onMouseLeave={e => e.currentTarget.style.background = '#3B82F6'}
                          >
                            {actionLoading[r.id] === 'returning' ? '…' : '↩ Returned'}
                          </button>
                        )}
                        {(r.status === 'REJECTED' || r.status === 'RETURNED') && (
                          <span className="text-xs text-gray-300 italic">—</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Reject modal */}
      {rejectTarget && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #E06060)' }} />
            <div className="p-6">
              <div className="w-10 h-10 rounded-xl flex items-center justify-center mb-4" style={{ background: '#FDF2F4' }}>
                <svg className="w-5 h-5" fill="none" stroke="#BE1B39" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h2 className="text-lg font-bold text-gray-900 mb-1">Reject Reservation</h2>
              <p className="text-sm text-gray-500 mb-4">
                Rejecting <strong>{rejectTarget.itemName}</strong> requested by{' '}
                <strong>{rejectTarget.borrowerName || rejectTarget.userEmail}</strong>
              </p>
              <label className="label">Reason for rejection</label>
              <textarea
                className="input-field h-24 resize-none"
                placeholder="e.g. Item is currently under maintenance…"
                value={rejectReason}
                onChange={e => setRejectReason(e.target.value)}
              />
              <div className="flex gap-3 mt-4">
                <button
                  onClick={() => { setRejectTarget(null); setRejectReason('') }}
                  className="flex-1 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleReject}
                  disabled={rejecting}
                  className="flex-1 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-60"
                  style={{ background: '#BE1B39' }}
                  onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
                  onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}
                >
                  {rejecting ? 'Rejecting…' : 'Confirm Reject'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
