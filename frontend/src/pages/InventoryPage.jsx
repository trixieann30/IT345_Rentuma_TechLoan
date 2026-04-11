import { useState, useEffect } from 'react'
import { inventoryService, reservationService, authService } from "../services/api";
export default function InventoryPage() {
  const [items, setItems] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')
  const [error, setError] = useState('')

  // Reserve modal
  const [reserveItem, setReserveItem] = useState(null)
  const [form, setForm] = useState({ quantity: 1, purpose: '', returnDate: '' })
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [successMsg, setSuccessMsg] = useState('')

  // Current user for eligibility check
  const [currentUser, setCurrentUser] = useState(null)

  useEffect(() => {
    fetchAll()
    authService.me().then(r => setCurrentUser(r.data)).catch(() => {})
  }, [])

  const fetchAll = async () => {
    setLoading(true)
    try {
      const [itemsRes, catsRes] = await Promise.all([
        inventoryService.getAll(),
        inventoryService.getCategories(),
      ])
      setItems(itemsRes.data)
      setCategories(catsRes.data)
    } catch {
      setError('Failed to load inventory.')
    } finally {
      setLoading(false)
    }
  }

  // Filter items
  const filtered = items.filter(item => {
    const matchSearch = !search ||
      item.name?.toLowerCase().includes(search.toLowerCase()) ||
      item.description?.toLowerCase().includes(search.toLowerCase())
    const matchCat = !selectedCategory || item.category === selectedCategory
    return matchSearch && matchCat
  })

  // Open reserve modal
  const openReserve = (item) => {
    setReserveItem(item)
    setForm({ quantity: 1, purpose: '', returnDate: '' })
    setFormError('')
    setSuccessMsg('')
  }

  // Submit reservation
  const handleReserve = async () => {
    setFormError('')
    if (!form.purpose.trim()) return setFormError('Purpose is required.')
    if (!form.returnDate) return setFormError('Return date is required.')
    if (new Date(form.returnDate) <= new Date()) return setFormError('Return date must be in the future.')
    if (form.quantity < 1 || form.quantity > (reserveItem.availableQuantity || 1)) {
      return setFormError(`Quantity must be between 1 and ${reserveItem.availableQuantity}.`)
    }

    setSubmitting(true)
    try {
      await reservationService.createReservation({
        inventoryId: reserveItem.id,
        quantity: Number(form.quantity),
        purpose: form.purpose,
        returnDate: form.returnDate,
      })
      setSuccessMsg('Reservation submitted! You will be notified once it is approved.')
      await fetchAll()
    } catch (e) {
      setFormError(e?.response?.data?.error || 'Failed to submit reservation.')
    } finally {
      setSubmitting(false)
    }
  }

  // Min date for return date picker (tomorrow)
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const minDate = tomorrow.toISOString().split('T')[0]

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <h1 className="text-2xl font-bold text-primary">Browse Inventory</h1>
        <p className="text-sm text-gray-500 mt-0.5">
          Browse and reserve available lab equipment
        </p>
      </div>

      {/* Search + Filter */}
      <div className="px-6 py-4 flex flex-wrap gap-3 items-center">
        <input
          className="input-field w-72"
          placeholder="Search equipment..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <div className="flex gap-2 flex-wrap">
          <button
            onClick={() => setSelectedCategory('')}
            className={`px-3 py-1.5 rounded-full text-sm font-semibold border transition-colors ${
              !selectedCategory ? 'bg-primary text-white border-primary' : 'bg-white text-gray-600 border-gray-200 hover:border-primary'
            }`}
          >
            All
          </button>
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 rounded-full text-sm font-semibold border transition-colors ${
                selectedCategory === cat ? 'bg-primary text-white border-primary' : 'bg-white text-gray-600 border-gray-200 hover:border-primary'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="mx-6 mb-4 alert-error"><span>⚠</span> {error}</div>
      )}

      {/* Grid */}
      <div className="px-6 pb-10">
        {loading ? (
          <div className="text-center py-20 text-gray-400">Loading inventory...</div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20 text-gray-400">No items found.</div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filtered.map(item => (
              <div key={item.id} className="card flex flex-col">
                {/* Image */}
                <div className="w-full h-40 rounded-xl overflow-hidden bg-gray-100 mb-3 flex items-center justify-center">
                  {item.imageUrl ? (
                    <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-5xl">📦</span>
                  )}
                </div>

                {/* Info */}
                <div className="flex-1">
                  <div className="flex items-start justify-between gap-2 mb-1">
                    <h3 className="font-bold text-gray-800 text-sm leading-tight">{item.name}</h3>
                    <span className={`shrink-0 px-2 py-0.5 rounded-full text-xs font-semibold ${
                      item.available ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-500'
                    }`}>
                      {item.available ? `Avail. ${item.availableQuantity}` : 'Unavail.'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mb-1">{item.category}</p>
                  {item.description && (
                    <p className="text-xs text-gray-500 line-clamp-2 mb-2">{item.description}</p>
                  )}
                  <p className="text-xs text-gray-400">Condition: <span className="font-medium text-gray-600">{item.condition}</span></p>
                </div>

                {/* Reserve button */}
                <button
                  onClick={() => openReserve(item)}
                  disabled={!item.available || item.availableQuantity < 1}
                  className="mt-3 btn-primary py-2 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {item.available ? 'Reserve' : 'Unavailable'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── Reserve Modal ── */}
      {reserveItem && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-lg font-bold text-gray-800">Reservation Form</h2>
              <button onClick={() => setReserveItem(null)} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
            </div>

            <div className="px-6 py-5 space-y-4">
              {/* Item summary */}
              <div className="flex gap-3 p-3 bg-gray-50 rounded-xl">
                {reserveItem.imageUrl ? (
                  <img src={reserveItem.imageUrl} alt={reserveItem.name}
                    className="w-14 h-14 rounded-lg object-cover border border-gray-200" />
                ) : (
                  <div className="w-14 h-14 rounded-lg bg-gray-200 flex items-center justify-center text-2xl">📦</div>
                )}
                <div>
                  <p className="font-bold text-gray-800">{reserveItem.name}</p>
                  <p className="text-xs text-gray-400">{reserveItem.category}</p>
                  <p className="text-xs text-green-600 font-semibold mt-0.5">
                    {reserveItem.availableQuantity} available
                  </p>
                </div>
              </div>

              {successMsg ? (
                <div className="alert-success">
                  <span>✓</span> {successMsg}
                </div>
              ) : (
                <>
                  {formError && <div className="alert-error text-sm">⚠ {formError}</div>}

                  {/* Quantity */}
                  <div>
                    <label className="label">Quantity</label>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setForm(f => ({ ...f, quantity: Math.max(1, f.quantity - 1) }))}
                        className="w-8 h-8 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-100 font-bold"
                      >−</button>
                      <input
                        type="number"
                        min="1"
                        max={reserveItem.availableQuantity}
                        className="input-field w-20 text-center"
                        value={form.quantity}
                        onChange={e => setForm(f => ({ ...f, quantity: e.target.value }))}
                      />
                      <button
                        onClick={() => setForm(f => ({ ...f, quantity: Math.min(reserveItem.availableQuantity, f.quantity + 1) }))}
                        className="w-8 h-8 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-100 font-bold"
                      >+</button>
                    </div>
                  </div>

                  {/* Purpose */}
                  <div>
                    <label className="label">Purpose / Remarks *</label>
                    <textarea
                      className="input-field h-20 resize-none"
                      placeholder="e.g. Lab experiment for IT345 final project..."
                      value={form.purpose}
                      onChange={e => setForm(f => ({ ...f, purpose: e.target.value }))}
                    />
                  </div>

                  {/* Return date */}
                  <div>
                    <label className="label">Return Date *</label>
                    <input
                      type="date"
                      min={minDate}
                      className="input-field"
                      value={form.returnDate}
                      onChange={e => setForm(f => ({ ...f, returnDate: e.target.value }))}
                    />
                  </div>
                </>
              )}
            </div>

            <div className="px-6 py-4 border-t border-gray-200 flex gap-3 justify-end">
              <button
                onClick={() => setReserveItem(null)}
                className="px-5 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                {successMsg ? 'Close' : 'Cancel'}
              </button>
              {!successMsg && (
                <button
                  onClick={handleReserve}
                  disabled={submitting}
                  className="btn-primary w-auto px-6 py-2.5 text-sm"
                >
                  {submitting ? 'Submitting...' : 'Submit Request'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}