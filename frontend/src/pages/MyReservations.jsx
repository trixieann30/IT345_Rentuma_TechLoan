import { useState, useEffect } from 'react'
import { reservationService } from '../../services/api'

const STATUS_FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'RETURNED']

export default function MyReservations() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [error, setError] = useState('')
  const [slipLoading, setSlipLoading] = useState({})
  const [qrModal, setQrModal] = useState(null) // holds reservation data with qrCodeUrl

  useEffect(() => { fetchReservations() }, [statusFilter])

  const fetchReservations = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await reservationService.getReservations(
        statusFilter === 'ALL' ? null : statusFilter
      )
      setReservations(res.data)
    } catch {
      setError('Failed to load reservations.')
    } finally {
      setLoading(false)
    }
  }

  const handleDownloadSlip = async (r) => {
    setSlipLoading(prev => ({ ...prev, [r.id]: true }))
    try {
      const res = await reservationService.downloadSlip(r.id)
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `borrowing-slip-${r.id}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch {
      setError('Failed to download borrowing slip.')
    } finally {
      setSlipLoading(prev => ({ ...prev, [r.id]: false }))
    }
  }

  const handleViewQR = async (r) => {
    try {
      const res = await reservationService.getQR(r.id)
      setQrModal({ ...r, ...res.data })
    } catch {
      setError('Failed to load QR code.')
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <h1 className="text-2xl font-bold text-primary">My Reservations</h1>
        <p className="text-sm text-gray-500 mt-0.5">Track your borrowing requests</p>
      </div>

      {/* Status tabs */}
      <div className="px-6 pt-4 flex gap-2 flex-wrap">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`px-4 py-2 rounded-full text-sm font-semibold transition-colors ${
              statusFilter === s
                ? 'bg-primary text-white'
                : 'bg-white text-gray-600 border border-gray-200 hover:border-primary hover:text-primary'
            }`}
          >
            {s}
          </button>
        ))}
      </div>

      {error && (
        <div className="mx-6 mt-4 alert-error">
          <span>⚠</span> {error}
          <button className="ml-auto text-xs underline" onClick={() => setError('')}>Dismiss</button>
        </div>
      )}

      {/* Cards */}
      <div className="px-6 py-4 pb-10 space-y-3">
        {loading ? (
          <div className="text-center py-20 text-gray-400">Loading...</div>
        ) : reservations.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            No {statusFilter !== 'ALL' ? statusFilter.toLowerCase() : ''} reservations found.
          </div>
        ) : (
          reservations.map(r => (
            <div key={r.id} className="bg-white rounded-2xl shadow p-5">
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="font-bold text-gray-800">{r.itemName}</h3>
                    <StatusBadge status={r.status} />
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-x-6 gap-y-1 text-sm mt-2">
                    <div>
                      <span className="text-gray-400 text-xs">Quantity</span>
                      <p className="font-semibold text-gray-700">{r.quantity}</p>
                    </div>
                    <div>
                      <span className="text-gray-400 text-xs">Return Date</span>
                      <p className="font-semibold text-gray-700">{r.returnDate || r.dueDate || '—'}</p>
                    </div>
                    <div>
                      <span className="text-gray-400 text-xs">Submitted</span>
                      <p className="font-semibold text-gray-700">
                        {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}
                      </p>
                    </div>
                    <div>
                      <span className="text-gray-400 text-xs">Purpose</span>
                      <p className="font-semibold text-gray-700 truncate max-w-[160px]">{r.purpose || '—'}</p>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex flex-col gap-2 items-end">
                  {r.status === 'APPROVED' && (
                    <>
                      <button
                        onClick={() => handleViewQR(r)}
                        className="px-4 py-2 text-xs font-semibold bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                      >
                        View QR Code
                      </button>
                      <button
                        onClick={() => handleDownloadSlip(r)}
                        disabled={slipLoading[r.id]}
                        className="px-4 py-2 text-xs font-semibold bg-gray-100 text-gray-700 border border-gray-200 rounded-lg hover:bg-gray-200 transition-colors disabled:opacity-60"
                      >
                        {slipLoading[r.id] ? 'Downloading...' : '↓ Download Slip'}
                      </button>
                    </>
                  )}
                </div>
              </div>

              {r.status === 'REJECTED' && r.rejectReason && (
                <div className="mt-3 p-3 bg-red-50 rounded-xl text-sm text-red-600">
                  <span className="font-semibold">Rejection reason: </span>{r.rejectReason}
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* ── QR Modal ── */}
      {qrModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 text-center">
            <div className="text-green-600 font-bold text-lg mb-1">✓ Reservation Approved!</div>
            <p className="text-sm text-gray-500 mb-4">Show this QR code to the custodian</p>

            {qrModal.qrCodeUrl ? (
              <img
                src={qrModal.qrCodeUrl}
                alt="QR Code"
                className="w-48 h-48 mx-auto rounded-xl border border-gray-200 mb-4"
              />
            ) : (
              <div className="w-48 h-48 mx-auto bg-gray-100 rounded-xl flex items-center justify-center text-gray-400 mb-4">
                QR not available
              </div>
            )}

            <div className="text-left space-y-1.5 text-sm bg-gray-50 rounded-xl p-4 mb-4">
              {[
                ['Item', qrModal.itemName],
                ['Quantity', qrModal.quantity],
                ['Return Date', qrModal.returnDate || qrModal.dueDate],
              ].map(([label, value]) => (
                <div key={label} className="flex justify-between">
                  <span className="text-gray-400">{label}</span>
                  <span className="font-semibold text-gray-700">{value || '—'}</span>
                </div>
              ))}
            </div>

            <button
              onClick={() => setQrModal(null)}
              className="btn-primary py-2.5 text-sm"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function StatusBadge({ status }) {
  const map = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    APPROVED: 'bg-green-100 text-green-700',
    REJECTED: 'bg-red-100 text-red-600',
    RETURNED: 'bg-blue-100 text-blue-700',
    OVERDUE: 'bg-orange-100 text-orange-700',
  }
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${map[status] || 'bg-gray-100 text-gray-600'}`}>
      {status}
    </span>
  )
}