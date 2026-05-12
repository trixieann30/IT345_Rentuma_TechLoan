import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService }        from '../auth/api'
import { reservationService } from '../reservation/api'
import { inventoryService }   from '../inventory/api'

export default function DashboardPage() {
  const navigate = useNavigate()
  const [user,            setUser]            = useState(() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
  })
  const [borrows,         setBorrows]         = useState([])
  const [pendingRequests, setPendingRequests] = useState([])
  const [inventoryCount,  setInventoryCount]  = useState(0)
  const [loading,         setLoading]         = useState(true)

  useEffect(() => {
    fetchCurrentUser()
    fetchBorrowRequests()

    const id = setInterval(() => {
      fetchCurrentUser()
      fetchBorrowRequests()
    }, 10_000)
    return () => clearInterval(id)
  }, [])

  async function fetchCurrentUser() {
    try {
      const res = await authService.me()
      setUser(res.data)
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch {
      localStorage.clear()
      navigate('/login')
    }
  }

  async function fetchBorrowRequests() {
    try {
      const res = await reservationService.getReservations()
      setBorrows(res.data)
      const stored = localStorage.getItem('user')
      if (stored && JSON.parse(stored).role === 'CUSTODIAN') {
        const pend = await reservationService.getReservations('PENDING')
        setPendingRequests(pend.data)
        const inv = await inventoryService.getAll()
        setInventoryCount(inv.data.length)
      }
    } catch {}
    finally { setLoading(false) }
  }

  async function handleStatusChange(borrowId, action) {
    try {
      if (action === 'approve') await reservationService.approveReservation(borrowId)
      else if (action === 'reject') {
        const reason = window.prompt('Reason for rejection (optional):') ?? ''
        await reservationService.rejectReservation(borrowId, reason)
      } else if (action === 'return') await reservationService.returnReservation(borrowId)
      else if (action === 'overdue') await reservationService.markOverdue(borrowId)
      fetchBorrowRequests()
      fetchCurrentUser()
    } catch (err) {
      alert(err.response?.data || 'Action failed.')
    }
  }

  if (!user) return (
    <div className="flex items-center justify-center h-full">
      <div className="flex flex-col items-center gap-3">
        <div className="w-10 h-10 rounded-full border-4 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
        <p className="text-sm text-gray-500">Loading dashboard…</p>
      </div>
    </div>
  )

  const isCustodian  = user.role === 'CUSTODIAN'
  const activeBorrows = borrows.filter(b => b.status === 'APPROVED' || b.status === 'PENDING')
  const pendingCount  = borrows.filter(b => b.status === 'PENDING').length
  const returnedCount = borrows.filter(b => b.status === 'RETURNED').length

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">

      {/* ── Welcome banner ──────────────────────────────────────────── */}
      <div className="rounded-2xl overflow-hidden shadow-lg"
        style={{ background: 'linear-gradient(135deg, #BE1B39 0%, #8C1229 55%, #120709 100%)' }}>
        <div className="px-7 py-8 relative overflow-hidden">
          {/* Dot grid */}
          <div className="absolute inset-0"
            style={{ backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.12) 1px, transparent 1px)', backgroundSize: '22px 22px' }} />
          {/* Decorative circles */}
          <div className="absolute -right-10 -top-10 w-52 h-52 rounded-full opacity-10" style={{ background: '#F4C430' }} />
          <div className="absolute right-24 -bottom-8 w-32 h-32 rounded-full opacity-[0.07]" style={{ background: '#F4C430' }} />
          <div className="absolute -left-14 -bottom-14 w-40 h-40 rounded-full opacity-[0.05]" style={{ background: '#F4C430' }} />
          <div className="relative flex items-center justify-between gap-4">
            <div>
              <p className="text-[11px] font-black uppercase tracking-widest mb-2" style={{ color: '#F4C430' }}>
                {isCustodian ? '⚙ Custodian Panel' : user.role === 'FACULTY' ? '🎓 Faculty Portal' : '🎓 Student Portal'}
              </p>
              <h2 className="text-3xl font-black text-white leading-tight">
                Good day, {user.fullName?.split(' ')[0]}!
              </h2>
              <p className="text-sm mt-2 max-w-sm leading-relaxed" style={{ color: '#C09098' }}>
                {isCustodian
                  ? 'Review pending requests and manage all active loans.'
                  : 'Browse inventory and track your active loans.'}
              </p>
            </div>
            <div className="hidden sm:flex flex-col items-end text-right flex-shrink-0 gap-0.5">
              <p className="text-white font-black text-4xl leading-none tabular-nums">
                {new Date().toLocaleDateString('en-PH', { day: 'numeric' })}
              </p>
              <p className="text-sm font-bold" style={{ color: '#F4C430' }}>
                {new Date().toLocaleDateString('en-PH', { month: 'long' })}
              </p>
              <p className="text-xs" style={{ color: 'rgba(255,255,255,0.35)' }}>
                {new Date().toLocaleDateString('en-PH', { weekday: 'long' })}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* ── Penalty alert ───────────────────────────────────────────── */}
      {!isCustodian && user.penaltyPoints > 0 && (
        <div className="flex items-center justify-between bg-red-50 border border-red-200 rounded-2xl px-5 py-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0" style={{ background: '#FEE2E2' }}>
              <svg className="w-5 h-5" fill="#BE1B39" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-bold text-red-800">Penalty Points: {user.penaltyPoints}</p>
              <p className="text-xs text-red-600">You have overdue penalty points that need to be cleared.</p>
            </div>
          </div>
          <button
            onClick={() => navigate('/penalties')}
            className="text-xs font-semibold px-4 py-2 rounded-xl transition-colors"
            style={{ background: '#BE1B39', color: '#fff' }}
            onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
            onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}
          >
            Pay Now
          </button>
        </div>
      )}

      {/* ── Stat cards ──────────────────────────────────────────────── */}
      {isCustodian ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <StatCard label="Pending Approvals" value={pendingRequests.length} icon={<IcPending />} gradFrom="#D4A820" gradTo="#9A7010" />
          <StatCard label="Active Loans"      value={borrows.filter(b => b.status === 'APPROVED').length} icon={<IcActive />} gradFrom="#BE1B39" gradTo="#8C1229" />
          <StatCard label="Inventory Items"   value={inventoryCount} icon={<IcInventory />} gradFrom="#3B82F6" gradTo="#1D4ED8" />
          <StatCard label="Overdue Items"     value={borrows.filter(b => b.status === 'OVERDUE').length} icon={<IcReturned />} gradFrom="#E06060" gradTo="#BE3838" />
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <StatCard label="Active Loans"     value={activeBorrows.length} icon={<IcActive />} gradFrom="#BE1B39" gradTo="#8C1229" />
          <StatCard label="Pending Requests" value={pendingCount}         icon={<IcPending />} gradFrom="#D4A820" gradTo="#9A7010" />
          <StatCard label="Returned Items"   value={returnedCount}        icon={<IcReturned />} gradFrom="#E06060" gradTo="#BE3838" />
        </div>
      )}

      {/* ── Quick actions ───────────────────────────────────────────── */}
      <div>
        <p className="text-xs font-black uppercase tracking-widest text-gray-400 mb-3">Quick Actions</p>
        <div className={`grid grid-cols-1 gap-3 ${isCustodian ? 'sm:grid-cols-4' : 'sm:grid-cols-3'}`}>
          {(isCustodian ? [
            { label: 'Manage Inventory',  desc: 'Add & delete items', route: '/inventory',         iconBg: '#120709',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /></svg> },
            { label: 'Reservation Queue', desc: 'Approve & reject',   route: '/reservation-queue', iconBg: '#BE1B39',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" /></svg> },
            { label: 'QR Scanner',        desc: 'Scan student QR',    route: '/qr-scan',           iconBg: '#D4A820',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" /></svg> },
            { label: 'Overdue Tracker',   desc: 'View overdue',       route: '/overdue-tracker',   iconBg: '#E06060',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg> },
          ] : [
            { label: 'Browse Inventory', desc: 'Reserve equipment', route: '/inventory',       iconBg: '#BE1B39',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /></svg> },
            { label: 'My Reservations',  desc: 'View & download',   route: '/my-reservations', iconBg: '#D4A820',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg> },
            { label: 'My Penalties',     desc: 'View & pay fines',  route: '/penalties',       iconBg: '#E06060',
              icon: <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg> },
          ]).map(({ label, desc, route, icon, iconBg }) => (
            <button
              key={route}
              onClick={() => navigate(route)}
              className="bg-white border border-gray-100 rounded-2xl p-4 text-left hover:shadow-lg hover:-translate-y-0.5 hover:border-gray-200 transition-all duration-200"
            >
              <div className="w-10 h-10 rounded-xl flex items-center justify-center mb-3"
                style={{ background: iconBg }}>
                {icon}
              </div>
              <p className="text-sm font-bold text-gray-900 leading-tight">{label}</p>
              <p className="text-xs text-gray-400 mt-0.5">{desc}</p>
            </button>
          ))}
        </div>
      </div>

      {/* ── Main content ────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Loans / Pending requests */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-gray-100">
            <h3 className="font-bold text-gray-900">
              {isCustodian ? 'Pending Approvals' : 'Active Loans'}
            </h3>
            <p className="text-xs text-gray-500 mt-0.5">
              {isCustodian ? 'Requests waiting for your review' : 'Items you currently have borrowed'}
            </p>
          </div>

          <div className="p-5">
            {loading ? (
              <div className="py-10 flex items-center justify-center gap-3">
                <div className="w-5 h-5 rounded-full border-2 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
                <span className="text-sm text-gray-400">Loading…</span>
              </div>
            ) : isCustodian ? (
              pendingRequests.length === 0 ? (
                <EmptyState icon="✅" title="All clear" sub="No pending requests at the moment." />
              ) : (
                <div className="space-y-3">
                  {pendingRequests.map(r => (
                    <PendingCard key={r.id} request={r} onAction={handleStatusChange} />
                  ))}
                </div>
              )
            ) : (
              activeBorrows.length === 0 ? (
                <EmptyState icon="📦" title="No active loans"
                  sub="Head to the inventory to request equipment."
                  action={<button onClick={() => navigate('/inventory')} className="btn-primary w-auto px-6 py-2 text-sm mt-4">Browse Inventory</button>} />
              ) : (
                <div className="space-y-3">
                  {activeBorrows.map(b => (
                    <LoanCard key={b.id} borrow={b} userRole={user.role} onAction={handleStatusChange} />
                  ))}
                </div>
              )
            )}
          </div>
        </div>

        {/* Sidebar: Activity + Holidays */}
        <div className="space-y-5">

          {/* Recent activity */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm">
            <div className="px-5 py-4 border-b border-gray-100">
              <h3 className="font-bold text-gray-900 text-sm">Recent Activity</h3>
            </div>
            <div className="p-4 space-y-0">
              {borrows.slice(0, 5).map((b, i) => (
                <div key={i} className="flex items-start gap-3 py-3 border-b border-gray-50 last:border-0">
                  <div className="w-7 h-7 rounded-full flex-shrink-0 flex items-center justify-center"
                    style={{ background: STATUS_BG[b.status] || '#F3F4F6' }}>
                    <span className="text-xs">{STATUS_EMOJI[b.status] || '📌'}</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-800 truncate">{b.itemName}</p>
                    <p className="text-xs text-gray-400">{b.status} · {new Date(b.createdAt).toLocaleDateString()}</p>
                  </div>
                </div>
              ))}
              {borrows.length === 0 && (
                <p className="text-xs text-gray-400 text-center py-6">No activity yet</p>
              )}
            </div>
            <div className="px-4 pb-4">
              <button onClick={() => navigate(isCustodian ? '/reservation-queue' : '/my-reservations')} className="btn-secondary text-sm py-2">
                View All
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}

/* ── Sub-components ────────────────────────────────────────────────────── */

const STATUS_BG = {
  PENDING: '#FFFBEB', APPROVED: '#EFF6FF', RETURNED: '#ECFDF5', OVERDUE: '#FEF3C7', REJECTED: '#FEF2F2'
}
const STATUS_EMOJI = {
  PENDING: '⏳', APPROVED: '✅', RETURNED: '📬', OVERDUE: '⚠️', REJECTED: '❌'
}
const STATUS_BADGE = {
  PENDING:  'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  RETURNED: 'badge-returned',
  OVERDUE:  'badge-overdue',
}

function StatCard({ label, value, icon, gradFrom, gradTo }) {
  return (
    <div className="rounded-2xl p-5 flex items-center gap-4 overflow-hidden relative shadow-md"
      style={{ background: `linear-gradient(135deg, ${gradFrom} 0%, ${gradTo} 100%)` }}>
      <div className="absolute -right-4 -bottom-4 w-24 h-24 rounded-full" style={{ background: 'rgba(255,255,255,0.1)' }} />
      <div className="absolute right-10 top-1 w-12 h-12 rounded-full" style={{ background: 'rgba(255,255,255,0.06)' }} />
      <div className="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 relative z-10"
        style={{ background: 'rgba(255,255,255,0.2)' }}>
        <span className="text-white">{icon}</span>
      </div>
      <div className="relative z-10">
        <p className="text-3xl font-black text-white leading-none">{value}</p>
        <p className="text-xs font-semibold mt-1 text-white/70">{label}</p>
      </div>
    </div>
  )
}

function IcActive() {
  return (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )
}
function IcPending() {
  return (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )
}
function IcReturned() {
  return (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
    </svg>
  )
}
function IcInventory() {
  return (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
    </svg>
  )
}

function EmptyState({ icon, title, sub, action }) {
  return (
    <div className="text-center py-12">
      <div className="text-4xl mb-3">{icon}</div>
      <p className="font-semibold text-gray-700">{title}</p>
      <p className="text-sm text-gray-400 mt-1">{sub}</p>
      {action}
    </div>
  )
}

function LoanCard({ borrow, userRole, onAction }) {
  const isCustodian = userRole === 'CUSTODIAN'
  return (
    <div className="border border-gray-100 rounded-xl p-4 hover:border-gray-200 transition-colors">
      <div className="flex items-start justify-between gap-2 mb-3">
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-gray-900 truncate">{borrow.itemName}</p>
          <p className="text-xs text-gray-500 mt-0.5 truncate">{borrow.itemDescription}</p>
        </div>
        <span className={STATUS_BADGE[borrow.status] || 'badge-pending'}>{borrow.status}</span>
      </div>
      <div className="grid grid-cols-2 gap-2 text-xs text-gray-500 mb-3">
        <span>Due: <strong className="text-gray-700">{new Date(borrow.dueDate).toLocaleDateString()}</strong></span>
        <span>Requested: <strong className="text-gray-700">{new Date(borrow.createdAt).toLocaleDateString()}</strong></span>
      </div>
      {isCustodian && (
        <div className="flex gap-2">
          {borrow.status === 'PENDING' && (
            <button onClick={() => onAction(borrow.id, 'approve')}
              className="flex-1 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors"
              style={{ background: '#10B981' }}
              onMouseEnter={e => e.currentTarget.style.background = '#059669'}
              onMouseLeave={e => e.currentTarget.style.background = '#10B981'}>
              ✓ Approve
            </button>
          )}
          {(borrow.status === 'APPROVED' || borrow.status === 'PENDING') && (
            <>
              <button onClick={() => onAction(borrow.id, 'return')}
                className="flex-1 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors"
                style={{ background: '#3B82F6' }}
                onMouseEnter={e => e.currentTarget.style.background = '#2563EB'}
                onMouseLeave={e => e.currentTarget.style.background = '#3B82F6'}>
                ↩ Return
              </button>
              <button onClick={() => onAction(borrow.id, 'overdue')}
                className="flex-1 py-1.5 text-xs font-semibold text-white rounded-lg transition-colors"
                style={{ background: '#EF4444' }}
                onMouseEnter={e => e.currentTarget.style.background = '#DC2626'}
                onMouseLeave={e => e.currentTarget.style.background = '#EF4444'}>
                ⚠ Overdue
              </button>
            </>
          )}
        </div>
      )}
    </div>
  )
}

function PendingCard({ request, onAction }) {
  return (
    <div className="border border-amber-100 bg-amber-50/40 rounded-xl p-4">
      <div className="flex items-start justify-between gap-2 mb-2">
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-gray-900">{request.itemName}</p>
          <div className="flex items-center gap-1.5 flex-wrap mt-0.5">
            <p className="text-xs text-gray-500">{request.borrowerName || request.userEmail}</p>
            {request.borrowerRole && (
              <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-full"
                style={{
                  background: request.borrowerRole === 'STUDENT' ? '#EFF6FF' : '#F0FDF4',
                  color: request.borrowerRole === 'STUDENT' ? '#1D4ED8' : '#15803D',
                }}>
                {request.borrowerRole}
              </span>
            )}
          </div>
        </div>
        <span className="badge-pending">PENDING</span>
      </div>
      <p className="text-xs text-gray-500 mb-3">Due: {new Date(request.dueDate).toLocaleDateString()}</p>
      <div className="flex gap-2">
        <button onClick={() => onAction(request.id, 'approve')}
          className="flex-1 py-1.5 text-xs font-semibold text-white rounded-lg"
          style={{ background: '#10B981' }}>
          ✓ Approve
        </button>
        <button onClick={() => onAction(request.id, 'reject')}
          className="flex-1 py-1.5 text-xs font-semibold rounded-lg border"
          style={{ color: '#BE1B39', borderColor: '#FADADF', background: '#FDF2F4' }}>
          ✕ Reject
        </button>
      </div>
    </div>
  )
}
