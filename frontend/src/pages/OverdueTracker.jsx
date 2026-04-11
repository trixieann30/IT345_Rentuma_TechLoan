import { useState, useEffect } from 'react'
import { reservationService } from '../../services/api'

export default function OverdueTracker() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState({})

  useEffect(() => { fetchOverdue() }, [])

  const fetchOverdue = async () => {
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

  const handleReturn = async (id) => {
    setActionLoading(prev => ({ ...prev, [id]: true }))
    try {
      await reservationService.returnReservation(id)
      await fetchOverdue()
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to mark as returned.')
    } finally {
      setActionLoading(prev => ({ ...prev, [id]: false }))
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <h1 className="text-2xl font-bold text-primary">Overdue Tracker</h1>
        <p className="text-sm text-gray-500 mt-0.5">
          {items.length} overdue item{items.length !== 1 ? 's' : ''} currently
        </p>
      </div>

      {error && (
        <div className="mx-6 mt-4 alert-error">
          <span>⚠</span> {error}
          <button className="ml-auto text-xs underline" onClick={() => setError('')}>Dismiss</button>
        </div>
      )}

      <div className="px-6 py-4 pb-10">
        {loading ? (
          <div className="text-center py-20 text-gray-400">Loading...</div>
        ) : items.length === 0 ? (
          <div className="text-center py-20">
            <div className="text-5xl mb-3">🎉</div>
            <p className="text-gray-500 font-medium">No overdue items!</p>
            <p className="text-sm text-gray-400 mt-1">All items have been returned on time.</p>
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Student/Faculty</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Item</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Qty</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Due Date</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Days Late</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Penalty pts</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {items.map(r => {
                  const daysLate = r.dueDate
                    ? Math.max(0, Math.floor((new Date() - new Date(r.dueDate)) / 86400000))
                    : 0
                  return (
                    <tr key={r.id} className="hover:bg-red-50/30 transition-colors">
                      <td className="px-4 py-3">
                        <div className="font-semibold text-gray-800">{r.borrowerName || r.userEmail}</div>
                        <div className="text-xs text-gray-400">{r.userEmail}</div>
                      </td>
                      <td className="px-4 py-3 font-medium text-gray-700">{r.itemName}</td>
                      <td className="px-4 py-3 text-gray-600">{r.quantity}</td>
                      <td className="px-4 py-3 text-red-600 font-semibold">{r.returnDate || r.dueDate || '—'}</td>
                      <td className="px-4 py-3">
                        <span className="px-2 py-1 bg-red-100 text-red-700 rounded-full text-xs font-bold">
                          {daysLate} days
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className="px-2 py-1 bg-orange-100 text-orange-700 rounded-full text-xs font-bold">
                          {Math.min(daysLate, 30)} pts
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => handleReturn(r.id)}
                          disabled={!!actionLoading[r.id]}
                          className="px-3 py-1.5 text-xs font-semibold bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-60 transition-colors"
                        >
                          {actionLoading[r.id] ? '...' : '✓ Mark Returned'}
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}