import { useState, useEffect, useRef } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { notificationService } from '../../features/notification/api'

const STUDENT_NAV = [
  { to: '/dashboard',       label: 'Dashboard',       Icon: IcHome },
  { to: '/inventory',       label: 'Browse Inventory', Icon: IcPackage },
  { to: '/my-reservations', label: 'My Reservations',  Icon: IcCalendar },
  { to: '/penalties',       label: 'My Penalties',     Icon: IcAlert },
  { to: '/profile',         label: 'Profile',          Icon: IcUser },
]

const CUSTODIAN_NAV = [
  { to: '/dashboard',         label: 'Dashboard',        Icon: IcHome },
  { to: '/inventory',         label: 'Manage Inventory', Icon: IcPackage },
  { to: '/reservation-queue', label: 'Reservations',     Icon: IcClipboard },
  { to: '/overdue-tracker',   label: 'Overdue Tracker',  Icon: IcClock },
  { to: '/qr-scan',           label: 'QR Scanner',       Icon: IcQr },
  { to: '/profile',           label: 'Profile',          Icon: IcUser },
]

export default function Layout() {
  const navigate = useNavigate()
  const [user] = useState(() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
  })
  const [notifications, setNotifications] = useState([])
  const [unreadCount,   setUnreadCount]   = useState(0)
  const [showNotif,     setShowNotif]     = useState(false)
  const [sidebarOpen,   setSidebarOpen]   = useState(false)
  const notifRef = useRef(null)

  useEffect(() => {
    fetchNotifications()
    const id = setInterval(fetchNotifications, 15_000)
    return () => clearInterval(id)
  }, [])

  useEffect(() => {
    function onClickOut(e) {
      if (notifRef.current && !notifRef.current.contains(e.target)) setShowNotif(false)
    }
    document.addEventListener('mousedown', onClickOut)
    return () => document.removeEventListener('mousedown', onClickOut)
  }, [])

  async function fetchNotifications() {
    try {
      const [listRes, countRes] = await Promise.all([
        notificationService.getAll(),
        notificationService.getUnreadCount(),
      ])
      setNotifications(listRes.data)
      setUnreadCount(countRes.data.count)
    } catch {}
  }

  async function handleMarkRead(id) {
    await notificationService.markRead(id)
    setNotifications(p => p.map(n => n.id === id ? { ...n, read: true } : n))
    setUnreadCount(p => Math.max(0, p - 1))
  }

  async function handleMarkAllRead() {
    await notificationService.markAllRead()
    setNotifications(p => p.map(n => ({ ...n, read: true })))
    setUnreadCount(0)
  }

  function logout() {
    localStorage.clear()
    navigate('/login')
  }

  const isCustodian = user?.role === 'CUSTODIAN'
  const nav = isCustodian ? CUSTODIAN_NAV : STUDENT_NAV
  const initials = user?.fullName?.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'U'

  return (
    <div className="flex h-screen overflow-hidden" style={{ background: '#F7F5F6' }}>

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/50 z-20 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* ── Sidebar ─────────────────────────────────────────────────────── */}
      <aside
        className={`fixed lg:static inset-y-0 left-0 z-30 w-64 flex-shrink-0 flex flex-col
                    transform transition-transform duration-300 lg:translate-x-0
                    ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}
        style={{ background: '#2E1520' }}
      >
        {/* Logo */}
        <div className="flex items-center gap-3 px-5 h-16 flex-shrink-0 relative"
          style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
          <div className="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 shadow-lg"
            style={{ background: 'linear-gradient(135deg, #BE1B39, #8C1229)' }}>
            <span className="text-white font-black text-base select-none">T</span>
          </div>
          <div>
            <p className="text-white font-bold text-[15px] leading-tight">TechLoan</p>
            <p className="text-[11px] leading-tight font-medium" style={{ color: '#F4C430', opacity: 0.7 }}>CIT-U Lab System</p>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 py-5 overflow-y-auto px-3 space-y-0.5">
          <p className="text-[10px] font-bold uppercase tracking-widest px-3 mb-3" style={{ color: '#9A6078' }}>
            {isCustodian ? 'Custodian Tools' : 'Student Portal'}
          </p>
          {nav.map(({ to, label, Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 ${
                  isActive ? '' : 'hover:bg-white/[0.04]'
                }`
              }
              style={({ isActive }) => isActive
                ? { background: 'rgba(244,196,48,0.10)', color: '#F4C430' }
                : { color: '#C09098' }
              }
            >
              {({ isActive }) => (
                <>
                  <Icon />
                  <span className="flex-1">{label}</span>
                  {isActive && <span className="w-1.5 h-1.5 rounded-full flex-shrink-0" style={{ background: '#F4C430' }} />}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* User + Logout */}
        <div className="p-4 border-t flex-shrink-0" style={{ borderColor: '#3E1E2C' }}>
          <div className="flex items-center gap-2.5 px-1 mb-3">
            <div className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white flex-shrink-0"
              style={{ background: '#BE1B39' }}>
              {initials}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white truncate leading-tight">{user?.fullName || 'User'}</p>
              <p className="text-[11px] leading-tight truncate" style={{ color: '#A06878' }}>{user?.role}</p>
            </div>
          </div>
          <button
            onClick={logout}
            className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-sm font-medium transition-all hover:bg-white/[0.05]"
            style={{ color: '#A06878' }}
            onMouseEnter={e => { e.currentTarget.style.color = '#E06060' }}
            onMouseLeave={e => { e.currentTarget.style.color = '#A06878' }}
          >
            <IcLogout />
            Sign Out
          </button>
        </div>
      </aside>

      {/* ── Main ────────────────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">

        {/* Top header */}
        <header className="h-16 bg-white flex items-center px-4 sm:px-6 gap-3 flex-shrink-0"
          style={{ borderBottom: '1px solid #F3F4F6', boxShadow: '0 1px 3px rgba(0,0,0,0.04), 0 1px 0 rgba(0,0,0,0.02)' }}>
          <button
            onClick={() => setSidebarOpen(v => !v)}
            className="lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>

          <div className="flex-1" />

          {/* Penalty badge */}
          {user?.penaltyPoints > 0 && (
            <button
              onClick={() => navigate('/penalties')}
              className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors"
              style={{ background: '#FEF2F2', color: '#BE1B39', border: '1px solid #FECACA' }}
              onMouseEnter={e => e.currentTarget.style.background = '#FEE2E2'}
              onMouseLeave={e => e.currentTarget.style.background = '#FEF2F2'}
            >
              <svg className="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              {user.penaltyPoints} penalty pts
            </button>
          )}

          {/* Notification bell */}
          <div className="relative" ref={notifRef}>
            <button
              onClick={() => {
                const opening = !showNotif
                setShowNotif(v => !v)
                if (opening && unreadCount > 0) handleMarkAllRead()
              }}
              className="relative w-10 h-10 rounded-xl flex items-center justify-center transition-all duration-150"
              style={{
                background: (showNotif || unreadCount > 0) ? '#FDF2F4' : '#F9FAFB',
                border: (showNotif || unreadCount > 0) ? '1px solid #FADADF' : '1px solid #E5E7EB',
              }}
            >
              <svg className="w-5 h-5 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                style={{ color: (showNotif || unreadCount > 0) ? '#BE1B39' : '#6B7280' }}>
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                  d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-0.5 text-white text-[10px] font-black rounded-full flex items-center justify-center animate-pulse"
                  style={{ background: '#BE1B39', boxShadow: '0 0 0 2px white' }}>
                  {unreadCount > 9 ? '9+' : unreadCount}
                </span>
              )}
            </button>

            {showNotif && (
              <div className="absolute right-0 top-12 w-80 bg-white rounded-2xl shadow-2xl border border-gray-100 z-50 overflow-hidden">
                <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                  <h4 className="font-bold text-gray-900 text-sm">Notifications</h4>
                  {unreadCount > 0 && (
                    <button onClick={handleMarkAllRead} className="text-xs font-semibold hover:underline"
                      style={{ color: '#BE1B39' }}>
                      Mark all read
                    </button>
                  )}
                </div>
                <div className="max-h-80 overflow-y-auto divide-y divide-gray-50">
                  {notifications.length === 0 ? (
                    <div className="py-10 text-center">
                      <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-2">
                        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                        </svg>
                      </div>
                      <p className="text-gray-400 text-sm">No notifications yet</p>
                    </div>
                  ) : notifications.map(n => (
                    <button
                      key={n.id}
                      onClick={() => handleMarkRead(n.id)}
                      className={`w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors ${!n.read ? 'bg-red-50/30' : ''}`}
                    >
                      <div className="flex items-start gap-2.5">
                        {!n.read && <span className="w-2 h-2 rounded-full mt-1.5 flex-shrink-0" style={{ background: '#BE1B39' }} />}
                        <div className={!n.read ? '' : 'ml-4'}>
                          <p className="text-sm font-semibold text-gray-900 leading-snug">{n.title}</p>
                          <p className="text-xs text-gray-500 mt-0.5 leading-snug">{n.message}</p>
                          <p className="text-xs text-gray-400 mt-1">{new Date(n.createdAt).toLocaleString()}</p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* User avatar → profile */}
          <button
            onClick={() => navigate('/profile')}
            className="flex items-center gap-2.5 rounded-xl px-2 py-1.5 hover:bg-gray-50 transition-colors"
            title="View Profile"
          >
            <div className="w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold text-white flex-shrink-0"
              style={{ background: '#BE1B39' }}>
              {initials}
            </div>
            <div className="hidden sm:block leading-tight text-left">
              <p className="text-sm font-semibold text-gray-900">{user?.fullName?.split(' ')[0]}</p>
              <p className="text-xs text-gray-400">{user?.role}</p>
            </div>
          </button>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

/* ── Icons ─────────────────────────────────────────────────────────────── */
function IcHome() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
    </svg>
  )
}
function IcPackage() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
    </svg>
  )
}
function IcCalendar() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  )
}
function IcAlert() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
    </svg>
  )
}
function IcUser() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    </svg>
  )
}
function IcClipboard() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
    </svg>
  )
}
function IcClock() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )
}
function IcQr() {
  return (
    <svg width="17" height="17" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
    </svg>
  )
}
function IcLogout() {
  return (
    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
    </svg>
  )
}
