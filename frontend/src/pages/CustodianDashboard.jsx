import { useState, useEffect } from 'react'
import { reservationService, inventoryService } from '../../services/api'
import { Link } from 'react-router-dom'

export default function CustodianDashboard() {
  const [pending, setPending] = useState([])
  const [overdue, setOverdue] = useState([])
  const [inventoryCount, setInventoryCount] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      reservationService.getReservations('PENDING'),
      reservationService.getReservations('OVERDUE'),
      inventoryService.getAll(),
    ]).then(([pendRes, overdueRes, invRes]) => {
      setPending(pendRes.data)
      setOverdue(overdueRes.data)
      setInventoryCount(invRes.data.length)
    }).catch(() => {}).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex items-center justify-center min-h-screen text-gray-400">Loading...</div>

  return (
    <div className="min-h-screen bg-gray-50 pb-10">
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <p className="text-sm text-gray-500">Custodian Panel</p>
        <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
      </div>

      <div className="px-6 py-5 space-y-5">
        {/* Stats */}
        <div className="grid grid-cols-3 gap-4">
          {[
            { label: 'Pending Requests', value: pending.length, color: 'text-yellow-600' },
            { label: 'Overdue Items', value: overdue.length, color: 'text-red-600' },
            { label: 'Total Inventory', value: inventoryCount, color: 'text-primary' },
          ].map(({ label, value, color }) => (
            <div key={label} className="bg-white rounded-2xl shadow p-4 text-center">
              <p className={`text-3xl font-bold ${color}`}>{value}</p>
              <p className="text-xs text-gray-500 mt-1">{label}</p>
            </div>
          ))}
        </div>

        {/* Quick links */}
        <div className="grid grid-cols-3 gap-3">
          <Link to="/custodian/reservations" className="btn-primary py-3 text-center text-sm font-semibold rounded-xl">
            Reservation Queue
          </Link>
          <Link to="/custodian/inventory" className="py-3 text-center text-sm font-semibold rounded-xl bg-white border border-gray-200 text-gray-700 hover:border-primary hover:text-primary transition-colors">
            Manage Inventory
          </Link>
          <Link to="/custodian/overdue" className="py-3 text-center text-sm font-semibold rounded-xl bg-white border border-gray-200 text-red-600 border-red-200 hover:bg-red-50 transition-colors">
            Overdue Tracker
          </Link>
        </div>

        {/* Pending reservations preview */}
        {pending.length > 0 && (
          <div className="bg-white rounded-2xl shadow p-5">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-bold text-gray-800">Pending Requests</h2>
              <Link to="/custodian/reservations" className="text-sm text-primary underline">View all</Link>
            </div>
            <div className="space-y-2">
              {pending.slice(0, 5).map(r => (
                <div key={r.id} className="flex items-center justify-between py-2 border-b border-gray-50 last:border-0">
                  <div>
                    <p className="font-medium text-gray-700 text-sm">{r.borrowerName || r.userEmail}</p>
                    <p className="text-xs text-gray-400">{r.itemName} · Qty {r.quantity}</p>
                  </div>
                  <span className="text-xs text-gray-400">{r.returnDate || '—'}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}