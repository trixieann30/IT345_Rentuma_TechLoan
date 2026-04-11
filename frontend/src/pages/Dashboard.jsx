import { useState, useEffect } from 'react'
import { authService, reservationService } from '../../services/api'
import { Link } from 'react-router-dom'

export default function StudentDashboard() {
  const [user, setUser] = useState(null)
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      authService.me(),
      reservationService.getReservations(),
    ]).then(([userRes, resRes]) => {
      setUser(userRes.data)
      setReservations(resRes.data)
    }).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const active = reservations.filter(r => r.status === 'APPROVED')
  const pending = reservations.filter(r => r.status === 'PENDING')
  const overdue = reservations.filter(r => r.status === 'OVERDUE')

  if (loading) return <div className="flex items-center justify-center min-h-screen text-gray-400">Loading...</div>

  return (
    <div className="min-h-screen bg-gray-50 pb-10">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <p className="text-sm text-gray-500">Welcome back,</p>
        <h1 className="text-2xl font-bold text-gray-800">{user?.fullName || 'Student'} 👋</h1>
      </div>

      <div className="px-6 py-5 space-y-5">
        {/* Overdue alert */}
        {overdue.length > 0 && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 flex items-center gap-3">
            <span className="text-xl">⚠️</span>
            <div>
              <p className="font-semibold">You have {overdue.length} overdue item{overdue.length > 1 ? 's' : ''}!</p>
              <p className="text-sm">Please return them as soon as possible to avoid additional penalties.</p>
            </div>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-3 gap-4">
          {[
            { label: 'Active Loans', value: active.length, color: 'text-primary' },
            { label: 'Pending', value: pending.length, color: 'text-yellow-600' },
            { label: 'Penalty Points', value: user?.penaltyPoints ?? 0, color: 'text-red-600' },
          ].map(({ label, value, color }) => (
            <div key={label} className="bg-white rounded-2xl shadow p-4 text-center">
              <p className={`text-3xl font-bold ${color}`}>{value}</p>
              <p className="text-xs text-gray-500 mt-1">{label}</p>
            </div>
          ))}
        </div>

        {/* Quick actions */}
        <div className="flex gap-3">
          <Link to="/inventory" className="flex-1 btn-primary py-3 text-center text-sm font-semibold rounded-xl">
            Browse Inventory
          </Link>
          <Link to="/my-reservations" className="flex-1 py-3 text-center text-sm font-semibold rounded-xl bg-white border border-gray-200 text-gray-700 hover:border-primary hover:text-primary transition-colors">
            My Reservations
          </Link>
        </div>

        {/* Active loans table */}
        {active.length > 0 && (
          <div className="bg-white rounded-2xl shadow p-5">
            <h2 className="font-bold text-gray-800 mb-3">Active Loans</h2>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs text-gray-400 border-b border-gray-100">
                  <th className="pb-2 text-left">Item</th>
                  <th className="pb-2 text-left">Due Date</th>
                  <th className="pb-2 text-left">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {active.map(r => (
                  <tr key={r.id}>
                    <td className="py-2 font-medium text-gray-700">{r.itemName}</td>
                    <td className="py-2 text-gray-500">{r.returnDate || r.dueDate || '—'}</td>
                    <td className="py-2">
                      <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-700">
                        Active
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}