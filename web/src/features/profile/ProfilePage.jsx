import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { profileService } from './api'

const BADGE = {
  PENDING:  'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  RETURNED: 'badge-returned',
  OVERDUE:  'badge-overdue',
}

export default function ProfilePage() {
  const navigate  = useNavigate()
  const [user,    setUser]    = useState(null)
  const [history, setHistory] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)

  const [editing,   setEditing]   = useState(false)
  const [editForm,  setEditForm]  = useState({ fullName: '', studentId: '', personalEmail: '', currentPassword: '', newPassword: '', confirmPassword: '' })
  const [saving,    setSaving]    = useState(false)
  const [saveError, setSaveError] = useState('')
  const [saveOk,    setSaveOk]    = useState(false)
  const [showPwSection, setShowPwSection] = useState(false)

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

  function startEdit() {
    setEditForm({
      fullName: user?.fullName || '',
      studentId: user?.studentId || '',
      personalEmail: user?.personalEmail || '',
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    })
    setSaveError('')
    setSaveOk(false)
    setShowPwSection(false)
    setEditing(true)
  }

  async function handleSave() {
    setSaveError('')
    if (!editForm.fullName.trim()) return setSaveError('Full name is required.')
    if (showPwSection) {
      if (!editForm.currentPassword) return setSaveError('Current password is required.')
      if (!editForm.newPassword) return setSaveError('New password is required.')
      if (editForm.newPassword.length < 6) return setSaveError('New password must be at least 6 characters.')
      if (editForm.newPassword !== editForm.confirmPassword) return setSaveError('Passwords do not match.')
    }

    setSaving(true)
    try {
      const payload = {
        fullName: editForm.fullName.trim(),
        studentId: editForm.studentId.trim() || null,
        personalEmail: editForm.personalEmail.trim() || null,
      }
      if (showPwSection && editForm.newPassword) {
        payload.currentPassword = editForm.currentPassword
        payload.newPassword = editForm.newPassword
      }
      const res = await profileService.updateProfile(payload)
      setUser(prev => ({ ...prev, ...res.data }))
      setSaveOk(true)
      setTimeout(() => { setEditing(false); setSaveOk(false) }, 1200)
    } catch (e) {
      setSaveError(e?.response?.data?.error?.message || 'Failed to save changes.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 rounded-full border-4 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
    </div>
  )

  const activeLoans  = history.filter(r => r.status === 'APPROVED').length
  const unpaidPoints = summary?.totalPoints ?? 0
  const initials     = user?.fullName?.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'U'

  return (
    <div className="p-6 space-y-5 max-w-4xl mx-auto">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">My Profile</h1>
        <p className="text-sm text-gray-500 mt-0.5">
          {user?.role === 'CUSTODIAN' ? 'Manage your account details' : 'Account information and borrowing history'}
        </p>
      </div>

      {/* User hero card */}
      <div className="rounded-2xl overflow-hidden"
        style={{ background: 'linear-gradient(135deg, #BE1B39 0%, #8C1229 60%, #120709 100%)' }}>
        <div className="px-7 py-6 flex items-center gap-5 relative overflow-hidden">
          <div className="absolute right-6 top-1/2 -translate-y-1/2 w-28 h-28 rounded-full opacity-10" style={{ background: '#F4C430' }} />
          <div className="w-16 h-16 rounded-2xl flex items-center justify-center flex-shrink-0 text-xl font-black text-white relative"
            style={{ background: 'rgba(244,196,48,0.20)', border: '2px solid rgba(244,196,48,0.35)' }}>
            {initials}
          </div>
          <div className="relative flex-1">
            <p className="text-xl font-black text-white">{user?.fullName}</p>
            <p className="text-sm mt-0.5" style={{ color: '#C09098' }}>{user?.email}</p>
            {user?.institutionalEmail && (
              <p className="text-xs mt-1 text-gray-100">CIT-U email: {user.institutionalEmail}</p>
            )}
            <div className="flex items-center gap-2 mt-2">
              <span className="text-xs font-bold px-2.5 py-0.5 rounded-full"
                style={{ background: 'rgba(244,196,48,0.2)', color: '#F4C430', border: '1px solid rgba(244,196,48,0.3)' }}>
                {user?.role}
              </span>
              {user?.studentId && (
                <span className="text-xs" style={{ color: '#9B6070' }}>ID: {user.studentId}</span>
              )}
            </div>
          </div>
          {!editing && (
            <button
              onClick={startEdit}
              className="relative z-10 flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-semibold transition-all"
              style={{ background: 'rgba(255,255,255,0.15)', color: '#fff', border: '1px solid rgba(255,255,255,0.25)' }}
              onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.25)'}
              onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.15)'}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit Profile
            </button>
          )}
        </div>
      </div>

      {/* Stats row — hidden for custodians */}
      {user?.role !== 'CUSTODIAN' && (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {[
              { label: 'Active Loans',   value: activeLoans,                                          color: '#BE1B39', bg: '#FDF2F4' },
              { label: 'Total Records',  value: history.length,                                        color: '#D4A820', bg: '#FFFBEB' },
              { label: 'Returned',       value: history.filter(r => r.status === 'RETURNED').length,   color: '#E06060', bg: '#FFF3F3' },
              { label: 'Penalty Points', value: unpaidPoints, color: unpaidPoints > 0 ? '#BE1B39' : '#10B981', bg: unpaidPoints > 0 ? '#FDF2F4' : '#ECFDF5' },
            ].map(({ label, value, color, bg }) => (
              <div key={label} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 text-center">
                <p className="text-3xl font-black" style={{ color }}>{value}</p>
                <p className="text-xs text-gray-500 font-medium mt-1">{label}</p>
              </div>
            ))}
          </div>

          {unpaidPoints > 0 && (
            <div className="flex items-center justify-between bg-red-50 border border-red-200 rounded-2xl px-5 py-4">
              <div>
                <p className="text-sm font-bold text-red-800">You have {unpaidPoints} unpaid penalty points</p>
                <p className="text-xs text-red-600 mt-0.5">Equivalent to ₱{(unpaidPoints * 50).toLocaleString()}.00</p>
              </div>
              <Link to="/penalties"
                className="px-4 py-2 text-xs font-semibold text-white rounded-xl transition-colors"
                style={{ background: '#BE1B39' }}>
                Pay Now →
              </Link>
            </div>
          )}
        </>
      )}

      {/* Edit form / Account details */}
      {editing ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 space-y-4">
          <h3 className="text-sm font-bold text-gray-400 uppercase tracking-widest">Edit Profile</h3>

          {saveError && <div className="alert-error text-sm">{saveError}</div>}
          {saveOk    && <div className="alert-success text-sm">Profile updated successfully!</div>}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Full Name *</label>
              <input className="input-field" value={editForm.fullName}
                onChange={e => setEditForm(f => ({ ...f, fullName: e.target.value }))} />
            </div>
            <div>
              <label className="label">Student / Faculty ID</label>
              <input className="input-field" value={editForm.studentId}
                onChange={e => setEditForm(f => ({ ...f, studentId: e.target.value }))} />
            </div>
            <div className="sm:col-span-2">
              <label className="label">Personal Email</label>
              <input type="email" className="input-field" value={editForm.personalEmail}
                onChange={e => setEditForm(f => ({ ...f, personalEmail: e.target.value }))}
                placeholder="Optional personal email" />
            </div>
          </div>

          {/* Change password toggle */}
          <button
            type="button"
            onClick={() => setShowPwSection(s => !s)}
            className="flex items-center gap-2 text-sm font-semibold transition-colors"
            style={{ color: '#BE1B39' }}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            {showPwSection ? 'Cancel password change' : 'Change password'}
          </button>

          {showPwSection && (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 p-4 rounded-xl" style={{ background: '#FDF2F4' }}>
              <div>
                <label className="label">Current Password</label>
                <input type="password" className="input-field" value={editForm.currentPassword}
                  onChange={e => setEditForm(f => ({ ...f, currentPassword: e.target.value }))} />
              </div>
              <div>
                <label className="label">New Password</label>
                <input type="password" className="input-field" value={editForm.newPassword}
                  onChange={e => setEditForm(f => ({ ...f, newPassword: e.target.value }))} />
              </div>
              <div>
                <label className="label">Confirm Password</label>
                <input type="password" className="input-field" value={editForm.confirmPassword}
                  onChange={e => setEditForm(f => ({ ...f, confirmPassword: e.target.value }))} />
              </div>
            </div>
          )}

          <div className="flex gap-3 pt-1">
            <button
              onClick={() => setEditing(false)}
              className="px-5 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors">
              Cancel
            </button>
            <button
              onClick={handleSave}
              disabled={saving}
              className="px-6 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-50"
              style={{ background: '#BE1B39' }}
              onMouseEnter={e => { if (!saving) e.currentTarget.style.background = '#9C1530' }}
              onMouseLeave={e => { if (!saving) e.currentTarget.style.background = '#BE1B39' }}>
              {saving ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h3 className="text-sm font-bold text-gray-400 uppercase tracking-widest mb-4">Account Details</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              ['Full Name',      user?.fullName],
              ['Email',          user?.email],
              ['Student ID',     user?.studentId],
              ['Role',           user?.role],
            ].map(([label, value]) => (
              <div key={label} className="flex flex-col gap-1">
                <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider">{label}</p>
                <p className="font-semibold text-gray-800">{value || '—'}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Borrowing history — hidden for custodians */}
      {user?.role !== 'CUSTODIAN' && <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-gray-100">
          <h3 className="font-bold text-gray-900">Borrowing History</h3>
          <p className="text-xs text-gray-500 mt-0.5">{history.length} total record{history.length !== 1 ? 's' : ''}</p>
        </div>

        {history.length === 0 ? (
          <div className="text-center py-10">
            <div className="w-12 h-12 rounded-2xl flex items-center justify-center mx-auto mb-3" style={{ background: '#FDF2F4' }}>
              <svg className="w-6 h-6" fill="none" stroke="#BE1B39" strokeWidth="1.5" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
            <p className="text-gray-500 text-sm font-medium">No borrowing history yet</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead style={{ background: '#F7F5F6' }}>
                <tr>
                  {['Item', 'Quantity', 'Return Date', 'Status'].map(h => (
                    <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {history.map(r => (
                  <tr key={r.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-5 py-3 font-semibold text-gray-800">{r.itemName}</td>
                    <td className="px-5 py-3 text-gray-600">{r.quantity}</td>
                    <td className="px-5 py-3 text-gray-600">{r.returnDate || '—'}</td>
                    <td className="px-5 py-3">
                      <span className={BADGE[r.status] || 'badge-pending'}>{r.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>}
    </div>
  )
}
