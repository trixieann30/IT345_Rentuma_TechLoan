import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService } from '../services/api'

export default function DashboardPage() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) setUser(JSON.parse(stored))

    // Verify token is still valid
    authService.me().catch(() => {
      localStorage.clear()
      navigate('/login')
    })
  }, [navigate])

  function handleLogout() {
    localStorage.clear()
    navigate('/login')
  }

  if (!user) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary" />
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Navbar */}
      <nav className="bg-primary shadow-md">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <h1 className="text-white text-2xl font-bold tracking-tight">TechLoan</h1>
          <div className="flex items-center gap-4">
            <span className="text-red-100 text-sm hidden sm:block">
              {user.email}
            </span>
            <button
              onClick={handleLogout}
              className="bg-white text-primary text-sm font-semibold
                         px-4 py-1.5 rounded-lg hover:bg-red-50 transition-colors"
            >
              Log Out
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto px-4 py-8">

        {/* Welcome */}
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-800">
            👋 Hello, {user.fullName?.split(' ')[0]}!
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            {user.role} · {user.email}
          </p>
        </div>

        {/* Overdue alert — shown if penalty points > 0 */}
        {user.penaltyPoints > 0 && (
          <div className="bg-red-50 border border-red-200 rounded-lg px-4 py-3
                          flex items-center gap-2 mb-6 text-red-700 text-sm">
            <span>⚠</span>
            <span>
              You have <strong>{user.penaltyPoints} penalty point(s)</strong> —{' '}
              <button className="underline font-semibold">View Details</button>
            </span>
          </div>
        )}

        {/* Stats cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
          <StatCard label="Active Loans"         value="0" color="text-primary" />
          <StatCard label="Pending Reservations" value="0" color="text-yellow-600" />
          <StatCard label="Penalty Points"       value={user.penaltyPoints ?? 0}
                    color={user.penaltyPoints > 0 ? "text-red-600" : "text-green-600"} />
        </div>

        {/* Quick Actions */}
        <div className="flex flex-wrap gap-3 mb-8">
          <button className="btn-outline">📦 Browse Inventory</button>
          <button className="btn-outline">📋 My Reservations</button>
        </div>

        {/* Active Loans table placeholder */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-700 mb-4">Active Loans</h3>
          <div className="text-center py-10 text-gray-400 text-sm">
            <p className="text-4xl mb-2">📭</p>
            <p>No active loans. Browse the inventory to get started.</p>
          </div>
        </div>

      </div>
    </div>
  )
}

function StatCard({ label, value, color }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
      <p className="text-gray-500 text-sm mb-1">{label}</p>
      <p className={`text-4xl font-bold ${color}`}>{value}</p>
      <div className="mt-3 h-1 rounded-full bg-gray-100" />
    </div>
  )
}
