import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService, borrowService } from '../services/api'

export default function DashboardPage() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [borrows, setBorrows] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [createForm, setCreateForm] = useState({ itemName: '', itemDescription: '', dueDate: '' })
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) setUser(JSON.parse(stored))

    // Fetch fresh user data from server (to get updated penalty points)
    fetchCurrentUser()

    // Fetch borrow requests
    fetchBorrowRequests()
    
    // Refresh every 3 seconds to see real-time updates from Observer Pattern
    const interval = setInterval(() => {
      fetchCurrentUser()
      fetchBorrowRequests()
    }, 3000)
    return () => clearInterval(interval)
  }, [navigate])

  async function fetchCurrentUser() {
    try {
      const res = await authService.me()
      setUser(res.data)
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch (err) {
      console.error('Failed to fetch current user:', err)
      localStorage.clear()
      navigate('/login')
    }
  }

  async function fetchBorrowRequests() {
    try {
      const res = await borrowService.getMyRequests()
      setBorrows(res.data)
    } catch (err) {
      console.error('Failed to fetch borrow requests:', err)
    } finally {
      setLoading(false)
    }
  }

  async function handleCreateRequest(e) {
    e.preventDefault()
    setSubmitting(true)
    try {
      await borrowService.createRequest({
        itemName: createForm.itemName,
        itemDescription: createForm.itemDescription,
        dueDate: createForm.dueDate + 'T23:59:59',
      })
      setCreateForm({ itemName: '', itemDescription: '', dueDate: '' })
      setShowCreateModal(false)
      fetchBorrowRequests()
      fetchCurrentUser()
    } catch (err) {
      console.error('Failed to create request:', err)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleStatusChange(borrowId, action) {
    try {
      if (action === 'approve') {
        await borrowService.approveRequest(borrowId)
      } else if (action === 'return') {
        await borrowService.returnRequest(borrowId)
      } else if (action === 'overdue') {
        await borrowService.markOverdue(borrowId)
      }
      // Refresh everything immediately after status change
      fetchBorrowRequests()
      fetchCurrentUser()
    } catch (err) {
      console.error('Failed to update request:', err)
    }
  }

  function handleLogout() {
    localStorage.clear()
    navigate('/login')
  }

  if (!user) return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-primary-50">
      <div className="flex flex-col items-center gap-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-gray-200 border-t-primary" />
        <p className="text-gray-600 text-sm">Loading your dashboard...</p>
      </div>
    </div>
  )

  const activeBorrows = borrows.filter(b => b.status === 'APPROVED' || b.status === 'PENDING')
  const pendingCount = borrows.filter(b => b.status === 'PENDING').length

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-gray-50 to-gray-100">

      {/* Header Navigation */}
      <nav className="bg-gradient-to-r from-primary to-primary-light shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
                <span className="text-primary font-bold text-lg">T</span>
              </div>
              <h1 className="text-white text-2xl font-bold">TechLoan</h1>
            </div>
            <div className="flex items-center gap-4">
              <div className="hidden sm:flex flex-col items-end">
                <p className="text-white text-sm font-medium">{user.fullName}</p>
                <p className="text-red-100 text-xs">{user.email}</p>
              </div>
              <button
                onClick={handleLogout}
                className="bg-white text-primary px-4 py-2 rounded-lg font-semibold text-sm
                           hover:shadow-lg hover:scale-105 transition-all active:scale-95"
              >
                Log Out
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">

        {/* Welcome Section */}
        <div className="mb-10">
          <h2 className="text-4xl font-bold text-gray-900">
            Welcome back, <span className="text-primary">{user.fullName?.split(' ')[0]}</span>
          </h2>
          <p className="text-gray-600 mt-2">
            <span className="inline-block bg-primary text-white px-3 py-1 rounded-full text-xs font-semibold mr-2">
              {user.role}
            </span>
            Manage your tech loans and reservations
          </p>
        </div>

        {/* Penalty Alert */}
        {user.penaltyPoints > 0 && (
          <div className="alert-error mb-8">
            <svg className="w-6 h-6 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            <div>
              <strong>Attention!</strong> You have {user.penaltyPoints} penalty point(s) from overdue items. 
              <span className="ml-2 text-xs">(This is the Observer Pattern in action!)</span>
            </div>
          </div>
        )}

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
          <StatCard 
            label="Active Loans" 
            value={activeBorrows.length} 
            color="from-blue-500 to-blue-600"
            description="Currently active"
            badge={activeBorrows.length}
          />
          <StatCard 
            label="Pending Requests" 
            value={pendingCount} 
            color="from-amber-500 to-amber-600"
            description="Awaiting approval"
            badge={pendingCount}
          />
          <StatCard 
            label="Account Status" 
            value={user.penaltyPoints > 0 ? `${user.penaltyPoints} pts` : "Good"} 
            color={user.penaltyPoints > 0 ? "from-red-500 to-red-600" : "from-green-500 to-green-600"}
            description={user.penaltyPoints > 0 ? "Needs attention" : "No penalties"}
            badge={user.penaltyPoints > 0 ? "!" : "✓"}
          />
        </div>

        {/* Quick Actions */}
        <div className="mb-10">
          <h3 className="text-sm font-semibold text-gray-700 mb-4 uppercase tracking-wider">Quick Actions</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <ActionButton 
              title="Request Item" 
              description="Create a new borrow request"
              onClick={() => setShowCreateModal(true)}
            />
            <ActionButton title="View Inventory" description="Explore available tech items" />
            <ActionButton title="My Activity" description="View your borrowing history" />
          </div>
        </div>

        {/* Main Content Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Active Loans - Main */}
          <div className="lg:col-span-2">
            <div className="card-elevated">
              <div className="border-b border-gray-200 pb-4 mb-6">
                <h3 className="text-lg font-bold text-gray-900">Active Loans</h3>
                <p className="text-gray-600 text-sm mt-1">Items currently borrowed by you</p>
              </div>
              
              {loading ? (
                <div className="text-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-4 border-gray-200 border-t-primary mx-auto mb-2" />
                  <p className="text-gray-600 text-sm">Loading loans...</p>
                </div>
              ) : activeBorrows.length === 0 ? (
                <div className="text-center py-16">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                    </svg>
                  </div>
                  <p className="text-gray-600 font-medium mb-2">No active loans</p>
                  <p className="text-gray-500 text-sm mb-6">Request a tech item to get started</p>
                  <button 
                    onClick={() => setShowCreateModal(true)}
                    className="btn-primary"
                  >
                    Create Request
                  </button>
                </div>
              ) : (
                <div className="space-y-4">
                  {activeBorrows.map(borrow => (
                    <LoanCard 
                      key={borrow.id} 
                      borrow={borrow} 
                      onStatusChange={handleStatusChange}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Recent Activity */}
          <div>
            <div className="card-elevated">
              <div className="border-b border-gray-200 pb-4 mb-6">
                <h3 className="text-lg font-bold text-gray-900">Recent Activity</h3>
              </div>
              <div className="space-y-4">
                {borrows.slice(0, 5).map((borrow, idx) => (
                  <ActivityItem 
                    key={idx}
                    timestamp={new Date(borrow.createdAt).toLocaleDateString()}
                    message={`Requested: ${borrow.itemName} (${borrow.status})`}
                  />
                ))}
              </div>
              <button className="w-full mt-6 btn-secondary py-2">View All Activity</button>
            </div>
          </div>
        </div>

      </div>

      {/* Create Request Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4 z-50">
          <div className="card-elevated max-w-md w-full">
            <div className="bg-gradient-to-r from-primary to-primary-light h-1 -m-6 mb-4 rounded-t-2xl" />
            
            <h3 className="text-lg font-bold text-gray-900 mb-2">Request New Item</h3>
            <p className="text-gray-600 text-sm mb-6">Fill in the details to request a tech item</p>

            <form onSubmit={handleCreateRequest} className="space-y-4">
              <div>
                <label className="label">Item Name *</label>
                <input
                  type="text"
                  placeholder="e.g., Laptop, Projector, Microphone"
                  value={createForm.itemName}
                  onChange={(e) => setCreateForm({ ...createForm, itemName: e.target.value })}
                  className="input-field"
                  required
                />
              </div>

              <div>
                <label className="label">Description</label>
                <textarea
                  placeholder="Tell us about the item you need..."
                  value={createForm.itemDescription}
                  onChange={(e) => setCreateForm({ ...createForm, itemDescription: e.target.value })}
                  className="input-field min-h-24 resize-none"
                />
              </div>

              <div>
                <label className="label">Due Date *</label>
                <input
                  type="date"
                  value={createForm.dueDate}
                  onChange={(e) => setCreateForm({ ...createForm, dueDate: e.target.value })}
                  className="input-field"
                  required
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 btn-secondary py-2"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex-1 btn-primary py-2"
                >
                  {submitting ? 'Creating...' : 'Request Item'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

function StatCard({ label, value, color, description, badge }) {
  return (
    <div className="card-elevated overflow-hidden group hover:shadow-2xl transition-all">
      <div className={`bg-gradient-to-br ${color} h-1`} />
      <div className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div className={`w-12 h-12 rounded-lg bg-gradient-to-br ${color} flex items-center justify-center text-white font-bold text-lg`}>
            {typeof badge === 'number' ? badge : badge}
          </div>
          <div className="text-right">
            <p className="text-gray-600 text-xs font-semibold uppercase tracking-wider">{label}</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">{value}</p>
          </div>
        </div>
        <p className="text-gray-600 text-sm">{description}</p>
      </div>
    </div>
  )
}

function ActionButton({ title, description, onClick }) {
  return (
    <button 
      onClick={onClick}
      className="card hover:border-primary group transition-all text-left cursor-pointer"
    >
      <div className="flex items-start gap-4">
        <div className="w-10 h-10 bg-primary text-white rounded-lg flex items-center justify-center flex-shrink-0 group-hover:shadow-lg transition-all">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <div>
          <h4 className="font-semibold text-gray-900 group-hover:text-primary transition-colors">{title}</h4>
          <p className="text-gray-600 text-sm mt-1">{description}</p>
        </div>
      </div>
    </button>
  )
}

function LoanCard({ borrow, onStatusChange }) {
  const statusColors = {
    PENDING: 'bg-amber-50 border-amber-200 text-amber-800',
    APPROVED: 'bg-blue-50 border-blue-200 text-blue-800',
    RETURNED: 'bg-green-50 border-green-200 text-green-800',
    OVERDUE: 'bg-red-50 border-red-200 text-red-800',
  }

  const statusBadgeColors = {
    PENDING: 'bg-amber-100 text-amber-800',
    APPROVED: 'bg-blue-100 text-blue-800',
    RETURNED: 'bg-green-100 text-green-800',
    OVERDUE: 'bg-red-100 text-red-800',
  }

  return (
    <div className={`border-2 rounded-lg p-4 ${statusColors[borrow.status]}`}>
      <div className="flex items-start justify-between mb-3">
        <div>
          <h4 className="font-bold text-gray-900">{borrow.itemName}</h4>
          <p className="text-sm text-gray-700 mt-1">{borrow.itemDescription}</p>
        </div>
        <span className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap ${statusBadgeColors[borrow.status]}`}>
          {borrow.status}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-2 mb-4 text-sm">
        <div>
          <p className="text-gray-600">Due Date</p>
          <p className="font-medium text-gray-900">{new Date(borrow.dueDate).toLocaleDateString()}</p>
        </div>
        <div>
          <p className="text-gray-600">Created</p>
          <p className="font-medium text-gray-900">{new Date(borrow.createdAt).toLocaleDateString()}</p>
        </div>
      </div>

      <div className="flex gap-2">
        {borrow.status === 'PENDING' && (
          <button
            onClick={() => onStatusChange(borrow.id, 'approve')}
            className="flex-1 px-3 py-2 bg-blue-500 text-white rounded font-medium text-sm hover:bg-blue-600 transition-colors"
          >
            Approve
          </button>
        )}
        {(borrow.status === 'APPROVED' || borrow.status === 'PENDING') && (
          <>
            <button
              onClick={() => onStatusChange(borrow.id, 'return')}
              className="flex-1 px-3 py-2 bg-green-500 text-white rounded font-medium text-sm hover:bg-green-600 transition-colors"
            >
              Return
            </button>
            <button
              onClick={() => onStatusChange(borrow.id, 'overdue')}
              className="flex-1 px-3 py-2 bg-red-500 text-white rounded font-medium text-sm hover:bg-red-600 transition-colors"
              title="This will trigger the Observer Pattern - watch your penalty points increase!"
            >
              Mark Overdue
            </button>
          </>
        )}
      </div>
    </div>
  )
}

function ActivityItem({ timestamp, message }) {
  return (
    <div className="flex items-start gap-3 pb-4 border-b border-gray-100 last:border-0 last:pb-0">
      <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
        <div className="w-2 h-2 bg-primary rounded-full" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-gray-900 font-medium text-sm">{message}</p>
        <p className="text-gray-500 text-xs mt-1">{timestamp}</p>
      </div>
    </div>
  )
}
