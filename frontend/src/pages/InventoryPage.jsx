import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { inventoryService, reservationService, authService } from '../services/api'

export default function InventoryPage() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [items, setItems] = useState([])
  const [filteredItems, setFilteredItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [categories, setCategories] = useState([])
  const [selectedCategory, setSelectedCategory] = useState('all')
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedItem, setSelectedItem] = useState(null)
  const [showRequestModal, setShowRequestModal] = useState(false)
  const [requestForm, setRequestForm] = useState({ purpose: '', quantity: 1, returnDate: '' })
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) setUser(JSON.parse(stored))

    fetchCurrentUser()
    fetchInventoryItems()
    fetchCategories()
  }, [navigate])

  useEffect(() => {
    filterItems()
  }, [items, selectedCategory, searchQuery])

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

  async function fetchInventoryItems() {
    try {
      const res = await inventoryService.getAvailable()
      setItems(res.data)
    } catch (err) {
      console.error('Failed to fetch inventory:', err)
    } finally {
      setLoading(false)
    }
  }

  async function fetchCategories() {
    try {
      const res = await inventoryService.getCategories()
      setCategories(res.data)
    } catch (err) {
      console.error('Failed to fetch categories:', err)
    }
  }

  function filterItems() {
    let filtered = items

    // Filter by category
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(item => item.category === selectedCategory)
    }

    // Filter by search query
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase()
      filtered = filtered.filter(item =>
        item.itemName.toLowerCase().includes(query) ||
        item.description?.toLowerCase().includes(query) ||
        item.itemCode.toLowerCase().includes(query)
      )
    }

    setFilteredItems(filtered)
  }

  async function handleCreateRequest(e) {
    e.preventDefault()
    if (!selectedItem || !requestForm.returnDate) {
      alert('Please fill in all required fields')
      return
    }

    setSubmitting(true)
    try {
      await reservationService.createReservation({
        inventoryId: selectedItem.id,
        quantity: parseInt(requestForm.quantity) || 1,
        purpose: requestForm.purpose || 'General use',
        returnDate: requestForm.returnDate,
      })
      setRequestForm({ purpose: '', quantity: 1, returnDate: '' })
      setShowRequestModal(false)
      setSelectedItem(null)
      fetchCurrentUser()
      navigate('/dashboard', { state: { message: 'Item requested successfully!' } })
    } catch (err) {
      console.error('Failed to create request:', err)
      alert('Failed to request item. Please try again.')
    } finally {
      setSubmitting(false)
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
        <p className="text-gray-600 text-sm">Loading inventory...</p>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-gray-50 to-gray-100">

      {/* Header Navigation */}
      <nav className="bg-gradient-to-r from-primary to-primary-light shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <button
                onClick={() => navigate('/dashboard')}
                className="flex items-center gap-3 hover:opacity-90 transition-opacity"
              >
                <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
                  <span className="text-primary font-bold text-lg">T</span>
                </div>
                <h1 className="text-white text-2xl font-bold">TechLoan</h1>
              </button>
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

        {/* Page Header */}
        <div className="mb-10">
          <h2 className="text-4xl font-bold text-gray-900">
            Inventory Browser
          </h2>
          <p className="text-gray-600 mt-2">
            Browse and request available tech items
          </p>
        </div>

        {/* Search and Filter Section */}
        <div className="card-elevated mb-8">
          <div className="space-y-4">
            {/* Search Bar */}
            <div>
              <input
                type="text"
                placeholder="Search items by name, code, or description..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            {/* Category Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Filter by Category
              </label>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => setSelectedCategory('all')}
                  className={`px-4 py-2 rounded-lg font-medium transition-all ${
                    selectedCategory === 'all'
                      ? 'bg-primary text-white'
                      : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                  }`}
                >
                  All Items
                </button>
                {categories.map(category => (
                  <button
                    key={category}
                    onClick={() => setSelectedCategory(category)}
                    className={`px-4 py-2 rounded-lg font-medium transition-all ${
                      selectedCategory === category
                        ? 'bg-primary text-white'
                        : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                    }`}
                  >
                    {category}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Results Info */}
        <div className="mb-6">
          <p className="text-gray-600 text-sm">
            Showing <span className="font-semibold">{filteredItems.length}</span> item(s)
          </p>
        </div>

        {/* Inventory Grid */}
        {loading ? (
          <div className="text-center py-16">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-gray-200 border-t-primary mx-auto mb-2" />
            <p className="text-gray-600 text-sm">Loading inventory...</p>
          </div>
        ) : filteredItems.length === 0 ? (
          <div className="text-center py-16 card-elevated">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
              </svg>
            </div>
            <p className="text-gray-600 font-medium mb-2">No items found</p>
            <p className="text-gray-500 text-sm">Try adjusting your search or category filter</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredItems.map(item => (
              <div key={item.id} className="card-elevated hover:shadow-xl transition-shadow">
                <div className="mb-4">
                  <div className="bg-gradient-to-br from-primary to-primary-light rounded-lg h-40 flex items-center justify-center mb-4">
                    {item.imageUrl ? (
                      <img src={item.imageUrl} alt={item.itemName} className="w-full h-full object-cover rounded-lg" />
                    ) : (
                      <svg className="w-16 h-16 text-white opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                    )}
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-1">{item.itemName}</h3>
                  <p className="text-sm text-gray-600 mb-2">Code: {item.itemCode}</p>
                  <div className="flex gap-2 mb-3">
                    <span className="inline-block bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs font-medium">
                      {item.category}
                    </span>
                    {item.condition && (
                      <span className="inline-block bg-green-100 text-green-800 px-2 py-1 rounded text-xs font-medium">
                        {item.condition}
                      </span>
                    )}
                  </div>
                </div>

                <p className="text-gray-700 text-sm mb-4 line-clamp-2">{item.description}</p>

                <div className="mb-4">
                  <p className="text-xs text-gray-600 mb-1">Available Quantity</p>
                  <div className="flex items-center gap-2">
                    <div className="flex-1 bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-500 h-2 rounded-full"
                        style={{
                          width: item.totalQuantity > 0
                            ? `${(item.availableQuantity / item.totalQuantity) * 100}%`
                            : '0%'
                        }}
                      />
                    </div>
                    <span className="text-sm font-semibold text-gray-900">
                      {item.availableQuantity}/{item.totalQuantity}
                    </span>
                  </div>
                </div>

                <button
                  onClick={() => {
                    setSelectedItem(item)
                    setShowRequestModal(true)
                  }}
                  disabled={!item.available || item.availableQuantity <= 0}
                  className={`w-full py-2 rounded-lg font-semibold text-sm transition-all ${
                    item.available && item.availableQuantity > 0
                      ? 'btn-primary hover:shadow-lg'
                      : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  }`}
                >
                  {item.available && item.availableQuantity > 0 ? 'Request Item' : 'Unavailable'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Request Modal */}
      {showRequestModal && selectedItem && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full shadow-xl">
            <div className="border-b border-gray-200 p-6">
              <h2 className="text-lg font-bold text-gray-900">
                Request: {selectedItem.itemName}
              </h2>
              <p className="text-gray-600 text-sm mt-1">
                Code: {selectedItem.itemCode}
              </p>
            </div>

            <form onSubmit={handleCreateRequest} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Quantity <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  min="1"
                  max={selectedItem.availableQuantity}
                  value={requestForm.quantity}
                  onChange={(e) => setRequestForm({ ...requestForm, quantity: parseInt(e.target.value) || 1 })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <p className="text-xs text-gray-500 mt-1">Available: {selectedItem.availableQuantity}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Purpose (optional)
                </label>
                <textarea
                  value={requestForm.purpose}
                  onChange={(e) => setRequestForm({ ...requestForm, purpose: e.target.value })}
                  placeholder="Why do you need this item? (e.g., Project research, assignment, event)"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary text-sm"
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Return Date <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  value={requestForm.returnDate}
                  onChange={(e) => setRequestForm({ ...requestForm, returnDate: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                <p className="text-xs text-blue-800">
                  <strong>Note:</strong> Your request will be submitted for approval. You'll be notified once it's approved.
                </p>
              </div>

              <div className="flex gap-3 pt-4 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => {
                    setShowRequestModal(false)
                    setSelectedItem(null)
                    setRequestForm({ purpose: '', quantity: 1, returnDate: '' })
                  }}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting || !requestForm.returnDate}
                  className="flex-1 btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {submitting ? 'Requesting...' : 'Request Item'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
