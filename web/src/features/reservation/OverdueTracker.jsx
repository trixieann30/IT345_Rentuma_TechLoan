import { useState, useEffect } from 'react'
import { reservationService } from './api'

export default function OverdueTracker() {
  const [items,        setItems]        = useState([])
  const [loading,      setLoading]      = useState(true)
  const [error,        setError]        = useState('')
  const [actionLoading, setActionLoading] = useState({})

  useEffect(() => { fetchOverdue() }, [])

  async function fetchOverdue() {
    setLoading(true)
    try {
      const res = await reservationService.getReservations('OVERDUE')
      setItems(res.data)
    } catch {
      setError('Failed to load overdue items.')
    } finally {
      setLoading(false)
    }
  }

  async function handleReturn(id) {
    setActionLoading(p => ({ ...p, [id]: true }))
    try {
      await reservationService.returnReservation(id)
      await fetchOverdue()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to mark as returned.')
    } finally {
      setActionLoading(p => ({ ...p, [id]: false }))
    }
  }

  return (
    <div className="p-6 space-y-5 max-w-6xl mx-auto">

      {/* Header */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900">Overdue Tracker</h1>
          <p className="text-sm text-gray-500 mt-0.5">Items that have not been returned past their due date</p>
        </div>
        {items.length > 0 && (
          <div className="rounded-xl px-4 py-2 text-center flex-shrink-0"
            style={{ background: '#FDF2F4', border: '1px solid #FADADF' }}>
            <p className="text-2xl font-black" style={{ color: '#BE1B39' }}>{items.length}</p>
            <p className="text-xs text-gray-500 font-medium">Overdue</p>
          </div>
        )}
      </div>

      {error && (
        <div className="alert-error">
          {error}
          <button className="ml-auto text-xs underline" onClick={() => setError('')}>Dismiss</button>
        </div>
      )}

      {loading ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden animate-pulse">
          <div className="h-12" style={{ background: '#F7F5F6' }} />
          {[...Array(4)].map((_, i) => (
            <div key={i} className="flex gap-4 px-4 py-4 border-t border-gray-50">
              {[...Array(7)].map((_, j) => <div key={j} className="h-4 bg-gray-100 rounded flex-1" />)}
            </div>
          ))}
        </div>
      ) : items.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm text-center py-20">
          <div className="text-5xl mb-3">🎉</div>
          <p className="font-semibold text-gray-700">No overdue items</p>
          <p className="text-sm text-gray-400 mt-1">All items have been returned on time.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="px-5 py-3 border-b border-gray-100 flex items-center gap-2"
            style={{ background: '#FEF2F2' }}>
            <svg className="w-4 h-4" fill="#BE1B39" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            <p className="text-xs font-semibold" style={{ color: '#BE1B39' }}>
              {items.length} item{items.length !== 1 ? 's' : ''} overdue — resolve immediately
            </p>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead style={{ background: '#F7F5F6' }}>
                <tr>
                  {['Student / Faculty', 'Item', 'Qty', 'Due Date', 'Days Late', 'Est. Penalty', 'Action'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {items.map(r => {
                  const daysLate = r.dueDate
                    ? Math.max(0, Math.floor((new Date() - new Date(r.dueDate)) / 86_400_000))
                    : 0
                  const estPenalty = Math.min(daysLate, 30)
                  return (
                    <tr key={r.id} className="hover:bg-red-50/20 transition-colors">
                      <td className="px-4 py-3.5">
                        <p className="font-semibold text-gray-800">{r.borrowerName || r.userEmail}</p>
                        <p className="text-xs text-gray-400">{r.userEmail}</p>
                      </td>
                      <td className="px-4 py-3.5 font-medium text-gray-700">{r.itemName}</td>
                      <td className="px-4 py-3.5 text-gray-600">{r.quantity}</td>
                      <td className="px-4 py-3.5 font-semibold whitespace-nowrap" style={{ color: '#BE1B39' }}>
                        {r.returnDate || r.dueDate || '—'}
                      </td>
                      <td className="px-4 py-3.5">
                        <span className="badge-overdue">{daysLate} day{daysLate !== 1 ? 's' : ''}</span>
                      </td>
                      <td className="px-4 py-3.5">
                        <span className="badge-rejected">{estPenalty} pts</span>
                      </td>
                      <td className="px-4 py-3.5">
                        <button
                          onClick={() => handleReturn(r.id)}
                          disabled={!!actionLoading[r.id]}
                          className="px-3 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors disabled:opacity-60"
                          style={{ background: '#10B981' }}
                          onMouseEnter={e => e.currentTarget.style.background = '#059669'}
                          onMouseLeave={e => e.currentTarget.style.background = '#10B981'}
                        >
                          {actionLoading[r.id] ? '…' : '✓ Mark Returned'}
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
