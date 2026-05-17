import { useState, useEffect } from 'react'
import { penaltyService } from './api'

const STATUS_FILTERS = ['ALL', 'UNPAID', 'PAID']

export default function PenaltyManagement() {
  const [penalties,    setPenalties]    = useState([])
  const [loading,      setLoading]      = useState(true)
  const [error,        setError]        = useState('')
  const [filter,       setFilter]       = useState('ALL')

  useEffect(() => { fetchAll() }, [])

  async function fetchAll() {
    setLoading(true)
    setError('')
    try {
      const res = await penaltyService.getAllPenalties()
      setPenalties(Array.isArray(res.data) ? res.data : [])
    } catch {
      setError('Failed to load penalties.')
    } finally {
      setLoading(false)
    }
  }

  const filtered = penalties.filter(p => {
    if (filter === 'UNPAID') return !p.paid
    if (filter === 'PAID')   return  p.paid
    return true
  })

  const totalUnpaid      = penalties.filter(p => !p.paid).reduce((s, p) => s + p.penaltyPoints, 0)
  const totalCollected   = penalties.filter(p =>  p.paid).reduce((s, p) => s + p.penaltyPoints, 0)
  const unpaidBorrowers  = new Set(penalties.filter(p => !p.paid).map(p => p.userId)).size

  return (
    <div className="p-6 space-y-5 max-w-7xl mx-auto">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">Penalty Management</h1>
        <p className="text-sm text-gray-500 mt-0.5">All borrower penalties and payment statuses</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
          <p className="text-2xl font-black" style={{ color: '#BE1B39' }}>{unpaidBorrowers}</p>
          <p className="text-xs font-semibold text-gray-500 mt-0.5">Borrowers with Unpaid Fines</p>
        </div>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
          <p className="text-2xl font-black" style={{ color: '#F4C430' }}>
            ₱{(totalUnpaid * 50).toLocaleString()}
          </p>
          <p className="text-xs font-semibold text-gray-500 mt-0.5">Total Outstanding ({totalUnpaid} pts)</p>
        </div>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 col-span-2 sm:col-span-1">
          <p className="text-2xl font-black" style={{ color: '#10B981' }}>
            ₱{(totalCollected * 50).toLocaleString()}
          </p>
          <p className="text-xs font-semibold text-gray-500 mt-0.5">Total Collected ({totalCollected} pts)</p>
        </div>
      </div>

      {/* Filter pills */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex gap-2">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className="px-4 py-1.5 rounded-xl text-sm font-semibold border transition-all"
            style={filter === s
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
              {[...Array(7)].map((_, j) => <div key={j} className="h-4 bg-gray-100 rounded flex-1" />)}
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-gray-100 shadow-sm">
          <div className="text-5xl mb-3">✅</div>
          <p className="font-semibold text-gray-700">No penalties found</p>
          <p className="text-sm text-gray-400 mt-1">
            {filter !== 'ALL' ? `No ${filter.toLowerCase()} penalties` : 'No penalty records yet'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead style={{ background: '#F7F5F6' }}>
                <tr>
                  {['Borrower', 'Item', 'Days Overdue', 'Penalty Pts', 'Amount', 'Status', 'Date'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filtered.map(p => (
                  <tr key={p.id}
                    className="transition-colors"
                    style={!p.paid ? { background: '#FFFBEB' } : {}}
                    onMouseEnter={e => { e.currentTarget.style.background = !p.paid ? '#FEF9C3' : '#F9FAFB' }}
                    onMouseLeave={e => { e.currentTarget.style.background = !p.paid ? '#FFFBEB' : '' }}
                  >
                    <td className="px-4 py-3.5">
                      <p className="font-semibold text-gray-800">{p.userName}</p>
                      <p className="text-xs text-gray-400">{p.userEmail}</p>
                    </td>
                    <td className="px-4 py-3.5 font-medium text-gray-700">{p.itemName}</td>
                    <td className="px-4 py-3.5">
                      <span className="badge-overdue">{p.daysOverdue} day{p.daysOverdue !== 1 ? 's' : ''}</span>
                    </td>
                    <td className="px-4 py-3.5">
                      <span className="font-bold text-sm" style={{ color: '#BE1B39' }}>{p.penaltyPoints} pts</span>
                    </td>
                    <td className="px-4 py-3.5 font-semibold text-gray-800">
                      ₱{(p.penaltyPoints * 50).toLocaleString()}.00
                    </td>
                    <td className="px-4 py-3.5">
                      <span className={p.paid ? 'badge-approved' : 'badge-rejected'}>
                        {p.paid ? 'PAID' : 'UNPAID'}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 text-xs text-gray-400 whitespace-nowrap">
                      {p.calculatedAt ? new Date(p.calculatedAt).toLocaleDateString() : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
