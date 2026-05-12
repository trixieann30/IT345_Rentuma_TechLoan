import { useState, useEffect } from 'react'
import { reservationService } from './api'

const STATUS_FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'RETURNED']

const BADGE = {
  PENDING:  'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  RETURNED: 'badge-returned',
  OVERDUE:  'badge-overdue',
}

export default function MyReservations() {
  const [reservations, setReservations] = useState([])
  const [loading,      setLoading]      = useState(true)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [error,        setError]        = useState('')
  const [slipLoading,  setSlipLoading]  = useState({})
  const [qrModal,      setQrModal]      = useState(null)

  useEffect(() => { fetchReservations() }, [statusFilter])

  async function fetchReservations() {
    setLoading(true)
    setError('')
    try {
      const res = await reservationService.getReservations(statusFilter === 'ALL' ? null : statusFilter)
      setReservations(res.data)
    } catch {
      setError('Failed to load reservations.')
    } finally {
      setLoading(false)
    }
  }

  async function handleDownloadSlip(r) {
    setSlipLoading(p => ({ ...p, [r.id]: true }))
    try {
      const res = await reservationService.downloadSlip(r.id)
      const url  = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const link = document.createElement('a')
      link.href  = url
      link.setAttribute('download', `borrowing-slip-${r.id}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch {
      setError('Failed to download borrowing slip.')
    } finally {
      setSlipLoading(p => ({ ...p, [r.id]: false }))
    }
  }

  async function handleViewQR(r) {
    try {
      const res = await reservationService.getQR(r.id)
      const qrCodeUrl = window.URL.createObjectURL(new Blob([res.data], { type: 'image/png' }))
      setQrModal({ ...r, qrCodeUrl })
    } catch {
      setError('Failed to load QR code.')
    }
  }

  return (
    <div className="p-6 space-y-5 max-w-5xl mx-auto">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">My Reservations</h1>
        <p className="text-sm text-gray-500 mt-0.5">Track all your borrowing requests and download slips</p>
      </div>

      {/* Filter pills */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex gap-2 flex-wrap">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className="px-4 py-1.5 rounded-xl text-sm font-semibold border transition-all"
            style={statusFilter === s
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

      {/* List */}
      {loading ? (
        <div className="space-y-3">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white rounded-2xl border border-gray-100 p-5 animate-pulse">
              <div className="h-4 bg-gray-100 rounded w-1/3 mb-3" />
              <div className="grid grid-cols-4 gap-4">
                {[...Array(4)].map((_, j) => <div key={j} className="h-3 bg-gray-100 rounded" />)}
              </div>
            </div>
          ))}
        </div>
      ) : reservations.length === 0 ? (
        <div className="text-center py-20">
          <div className="text-5xl mb-3">📋</div>
          <p className="font-semibold text-gray-700">No reservations found</p>
          <p className="text-sm text-gray-400 mt-1">
            {statusFilter !== 'ALL' ? `No ${statusFilter.toLowerCase()} reservations` : 'Start by browsing the inventory'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {reservations.map(r => (
            <div key={r.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:border-gray-200 transition-colors">
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2.5 mb-2 flex-wrap">
                    <h3 className="font-bold text-gray-900">{r.itemName}</h3>
                    <span className={BADGE[r.status] || 'badge-pending'}>{r.status}</span>
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-x-6 gap-y-2 text-sm">
                    {[
                      ['Quantity',   r.quantity],
                      ['Return Date', r.returnDate || r.dueDate || '—'],
                      ['Submitted',  r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'],
                      ['Purpose',    r.purpose || '—'],
                    ].map(([lbl, val]) => (
                      <div key={lbl}>
                        <p className="text-[11px] text-gray-400 font-medium">{lbl}</p>
                        <p className="font-semibold text-gray-700 truncate">{val}</p>
                      </div>
                    ))}
                  </div>
                </div>

                {r.status === 'APPROVED' && (
                  <div className="flex flex-col gap-2">
                    <button
                      onClick={() => handleViewQR(r)}
                      className="px-4 py-2 text-xs font-semibold rounded-xl text-white transition-colors"
                      style={{ background: '#BE1B39' }}
                      onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
                      onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}
                    >
                      View QR Code
                    </button>
                    <button
                      onClick={() => handleDownloadSlip(r)}
                      disabled={slipLoading[r.id]}
                      className="px-4 py-2 text-xs font-semibold text-gray-700 bg-gray-100 border border-gray-200 rounded-xl hover:bg-gray-200 transition-colors disabled:opacity-60"
                    >
                      {slipLoading[r.id] ? 'Downloading…' : '↓ Download Slip'}
                    </button>
                  </div>
                )}
              </div>

              {r.status === 'REJECTED' && r.rejectReason && (
                <div className="mt-3 p-3 rounded-xl text-sm text-red-700" style={{ background: '#FEF2F2', border: '1px solid #FECACA' }}>
                  <strong>Rejection reason:</strong> {r.rejectReason}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* QR Modal */}
      {qrModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #F4C430)' }} />
            <div className="p-6 text-center">
              <div className="w-10 h-10 rounded-full flex items-center justify-center mx-auto mb-3" style={{ background: '#ECFDF5' }}>
                <svg className="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <p className="font-bold text-gray-900 mb-0.5">Reservation Approved</p>
              <p className="text-xs text-gray-500 mb-4">Show this QR code to the custodian</p>

              {qrModal.qrCodeUrl
                ? <img src={qrModal.qrCodeUrl} alt="QR Code"
                    className="w-48 h-48 mx-auto rounded-xl border border-gray-100 mb-4" />
                : <div className="w-48 h-48 mx-auto bg-gray-100 rounded-xl flex items-center justify-center text-gray-400 mb-4 text-sm">
                    QR not available
                  </div>
              }

              <div className="rounded-xl p-3 mb-4 text-left space-y-1.5" style={{ background: '#F7F5F6' }}>
                {[['Item', qrModal.itemName], ['Quantity', qrModal.quantity], ['Return Date', qrModal.returnDate || qrModal.dueDate || '—']].map(([label, value]) => (
                  <div key={label} className="flex justify-between text-sm">
                    <span className="text-gray-400">{label}</span>
                    <span className="font-semibold text-gray-700">{value}</span>
                  </div>
                ))}
              </div>

              <button onClick={() => setQrModal(null)} className="btn-primary py-2.5 text-sm">
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
