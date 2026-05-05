import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { profileService } from './api'

export default function ProfilePage() {
  const navigate = useNavigate()
  const [user, setUser]             = useState(null)
  const [history, setHistory]       = useState([])
  const [penaltySummary, setSummary] = useState(null)
  const [loading, setLoading]       = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const userRes = await profileService.getProfile()
        const u = userRes.data
        setUser(u)

        const [histRes, penRes] = await Promise.all([
          profileService.getBorrowHistory(u.id),
          profileService.getPenaltySummary(u.id),
        ])
        setHistory(histRes.data)
        setSummary(penRes.data)
      } catch {
        navigate('/login')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [navigate])

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="animate-spin rounded-full h-10 w-10 border-4 border-gray-200 border-t-primary" />
    </div>
  )

  const activeLoans = history.filter(r => r.status === 'APPROVED').length
  const unpaidPoints = penaltySummary?.totalPoints ?? 0

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center gap-4">
        <Link to="/dashboard" className="text-primary text-sm font-semibold hover:underline">← Dashboard</Link>
        <h1 className="text-2xl font-bold text-primary">My Profile</h1>
      </div>

      <div className="max-w-3xl mx-auto px-4 py-8 space-y-6">

        {/* User Info Card */}
        <div className="bg-white rounded-2xl shadow p-6">
          <h2 className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-4">Account Information</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              ['Full Name',   user?.fullName],
              ['Email',       user?.email],
              ['Student ID',  user?.studentId],
              ['Role',        user?.role],
            ].map(([label, value]) => (
              <div key={label}>
                <p className="text-xs text-gray-400 mb-0.5">{label}</p>
                <p className="font-semibold text-gray-800">{value || '—'}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Stats Row */}
        <div className="grid grid-cols-2 gap-4">
          <div className="bg-white rounded-2xl shadow p-5 text-center">
            <p className="text-3xl font-bold text-blue-600">{activeLoans}</p>
            <p className="text-sm text-gray-500 mt-1">Active Loans</p>
          </div>
          <div className={`rounded-2xl shadow p-5 text-center ${unpaidPoints > 0 ? 'bg-red-50' : 'bg-green-50'}`}>
            <p className={`text-3xl font-bold ${unpaidPoints > 0 ? 'text-red-600' : 'text-green-600'}`}>
              {unpaidPoints}
            </p>
            <p className="text-sm text-gray-500 mt-1">Unpaid Penalty Points</p>
            {unpaidPoints > 0 && (
              <Link to="/penalties" className="text-xs text-red-500 font-semibold underline mt-2 inline-block">
                Pay Now →
              </Link>
            )}
          </div>
        </div>

        {/* Borrow History */}
        <div className="bg-white rounded-2xl shadow p-6">
          <h2 className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-4">Borrowing History</h2>
          {history.length === 0 ? (
            <p className="text-gray-400 text-sm text-center py-6">No borrowing history yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-gray-400 border-b border-gray-100">
                    <th className="pb-2 font-semibold">Item</th>
                    <th className="pb-2 font-semibold">Qty</th>
                    <th className="pb-2 font-semibold">Return Date</th>
                    <th className="pb-2 font-semibold">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {history.map(r => (
                    <tr key={r.id}>
                      <td className="py-2 font-medium text-gray-800">{r.itemName}</td>
                      <td className="py-2 text-gray-600">{r.quantity}</td>
                      <td className="py-2 text-gray-600">{r.returnDate || '—'}</td>
                      <td className="py-2">
                        <StatusBadge status={r.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  )
}

function StatusBadge({ status }) {
  const map = {
    PENDING:  'bg-yellow-100 text-yellow-700',
    APPROVED: 'bg-green-100 text-green-700',
    REJECTED: 'bg-red-100 text-red-600',
    RETURNED: 'bg-blue-100 text-blue-700',
    OVERDUE:  'bg-orange-100 text-orange-700',
  }
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${map[status] || 'bg-gray-100 text-gray-600'}`}>
      {status}
    </span>
  )
}
