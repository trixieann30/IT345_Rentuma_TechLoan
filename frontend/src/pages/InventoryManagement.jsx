import { useState, useEffect, useRef } from 'react'
import { inventoryService } from '../../services/api'

const CATEGORIES = ['Laptop', 'Tablet', 'Camera', 'Drone', 'Monitor', 'Keyboard',
  'Mouse', 'Projector', 'Microphone', 'Headphones', 'Speaker', 'Storage',
  'Arduino', 'Sensors', 'Network Tools', 'Other']
const CONDITIONS = ['Excellent', 'Good', 'Fair', 'Poor']

const emptyForm = {
  name: '', description: '', category: '', condition: 'Good',
  quantity: 1, specifications: '',
}

export default function InventoryManagement() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [filterCategory, setFilterCategory] = useState('')

  // Modal state
  const [modalOpen, setModalOpen] = useState(false)
  const [editItem, setEditItem] = useState(null) // null = create mode
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  // Image upload state
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')
  const [uploadingImage, setUploadingImage] = useState(false)
  const fileInputRef = useRef()

  // Delete confirm
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => { fetchItems() }, [])

  const fetchItems = async () => {
    setLoading(true)
    try {
      const res = await inventoryService.getAll()
      setItems(res.data)
    } catch {
      setError('Failed to load inventory.')
    } finally {
      setLoading(false)
    }
  }

  // ── Filtering ──────────────────────────────────────
  const filtered = items.filter(item => {
    const matchSearch = !search ||
      item.name?.toLowerCase().includes(search.toLowerCase()) ||
      item.description?.toLowerCase().includes(search.toLowerCase())
    const matchCat = !filterCategory || item.category === filterCategory
    return matchSearch && matchCat
  })

  // ── Open modal ─────────────────────────────────────
  const openCreate = () => {
    setEditItem(null)
    setForm(emptyForm)
    setImageFile(null)
    setImagePreview('')
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (item) => {
    setEditItem(item)
    setForm({
      name: item.name || '',
      description: item.description || '',
      category: item.category || '',
      condition: item.condition || 'Good',
      quantity: item.availableQuantity ?? item.quantity ?? 1,
      specifications: item.specifications || '',
    })
    setImageFile(null)
    setImagePreview(item.imageUrl || '')
    setFormError('')
    setModalOpen(true)
  }

  // ── Image picker ───────────────────────────────────
  const handleImageChange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    if (file.size > 5 * 1024 * 1024) {
      setFormError('Image must be under 5MB.')
      return
    }
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      setFormError('Only JPG and PNG images are allowed.')
      return
    }
    setImageFile(file)
    setImagePreview(URL.createObjectURL(file))
    setFormError('')
  }

  // ── Save (create or update) ────────────────────────
  const handleSave = async () => {
    setFormError('')
    if (!form.name.trim()) return setFormError('Item name is required.')
    if (!form.category) return setFormError('Category is required.')
    if (!form.condition) return setFormError('Condition is required.')
    if (!form.quantity || form.quantity < 1) return setFormError('Quantity must be at least 1.')

    setSaving(true)
    try {
      let savedId = editItem?.id

      if (editItem) {
        await inventoryService.updateItem(editItem.id, {
          name: form.name,
          description: form.description,
          category: form.category,
          condition: form.condition,
          quantity: Number(form.quantity),
          specifications: form.specifications,
        })
      } else {
        const res = await inventoryService.createItem({
          name: form.name,
          description: form.description,
          category: form.category,
          condition: form.condition,
          quantity: Number(form.quantity),
          specifications: form.specifications,
        })
        savedId = res.data.id
      }

      // Upload image if a new file was selected
      if (imageFile && savedId) {
        setUploadingImage(true)
        try {
          await inventoryService.uploadImage(savedId, imageFile)
        } catch {
          setFormError('Item saved but image upload failed. You can re-upload from Edit.')
          setSaving(false)
          setUploadingImage(false)
          fetchItems()
          setModalOpen(false)
          return
        }
        setUploadingImage(false)
      }

      await fetchItems()
      setModalOpen(false)
    } catch (e) {
      setFormError(e?.response?.data?.error || 'Failed to save item.')
    } finally {
      setSaving(false)
    }
  }

  // ── Delete ─────────────────────────────────────────
  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await inventoryService.deleteItem(deleteTarget.id)
      await fetchItems()
      setDeleteTarget(null)
    } catch (e) {
      setError(e?.response?.data?.error || 'Failed to delete item.')
      setDeleteTarget(null)
    } finally {
      setDeleting(false)
    }
  }

  // ── Render ─────────────────────────────────────────
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-primary">Inventory Management</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {items.length} items total · {items.filter(i => i.available).length} available
          </p>
        </div>
        <button onClick={openCreate} className="btn-primary w-auto px-5 py-2.5 flex items-center gap-2">
          <span className="text-lg leading-none">+</span> Add Item
        </button>
      </div>

      {/* Filters */}
      <div className="px-6 py-4 flex flex-wrap gap-3">
        <input
          className="input-field w-64"
          placeholder="Search items..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select
          className="input-field w-48"
          value={filterCategory}
          onChange={e => setFilterCategory(e.target.value)}
        >
          <option value="">All Categories</option>
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        {(search || filterCategory) && (
          <button
            className="text-sm text-gray-500 underline"
            onClick={() => { setSearch(''); setFilterCategory('') }}
          >Clear filters</button>
        )}
      </div>

      {/* Error banner */}
      {error && (
        <div className="mx-6 mb-4 alert-error">
          <span>⚠</span> {error}
          <button className="ml-auto text-xs underline" onClick={() => setError('')}>Dismiss</button>
        </div>
      )}

      {/* Table */}
      <div className="px-6 pb-10">
        {loading ? (
          <div className="text-center py-20 text-gray-400">Loading inventory...</div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            {search || filterCategory ? 'No items match your filters.' : 'No inventory items yet. Add one!'}
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Image</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Name</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Category</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Condition</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Available</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Total</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Status</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map(item => (
                  <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-3">
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt={item.name}
                          className="w-12 h-12 object-cover rounded-lg border border-gray-200"
                        />
                      ) : (
                        <div className="w-12 h-12 rounded-lg bg-gray-100 flex items-center justify-center text-gray-300 text-xl">
                          📦
                        </div>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <div className="font-semibold text-gray-800">{item.name}</div>
                      {item.description && (
                        <div className="text-xs text-gray-400 mt-0.5 max-w-xs truncate">{item.description}</div>
                      )}
                    </td>
                    <td className="px-4 py-3 text-gray-600">{item.category}</td>
                    <td className="px-4 py-3">
                      <ConditionBadge condition={item.condition} />
                    </td>
                    <td className="px-4 py-3 font-semibold text-gray-800">{item.availableQuantity ?? 0}</td>
                    <td className="px-4 py-3 text-gray-600">{item.totalQuantity ?? item.quantity ?? '-'}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        item.available ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'
                      }`}>
                        {item.available ? 'Available' : 'Unavailable'}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <button
                          onClick={() => openEdit(item)}
                          className="px-3 py-1.5 text-xs font-semibold bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => setDeleteTarget(item)}
                          className="px-3 py-1.5 text-xs font-semibold bg-red-50 text-red-600 border border-red-200 rounded-lg hover:bg-red-100 transition-colors"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Add/Edit Modal ── */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-lg font-bold text-gray-800">
                {editItem ? 'Edit Item' : 'Add New Item'}
              </h2>
              <button onClick={() => setModalOpen(false)} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
            </div>

            <div className="px-6 py-5 space-y-4">
              {formError && (
                <div className="alert-error text-sm">⚠ {formError}</div>
              )}

              {/* Image upload */}
              <div>
                <label className="label">Equipment Image</label>
                <div
                  className="border-2 border-dashed border-gray-300 rounded-xl p-4 text-center cursor-pointer hover:border-primary transition-colors"
                  onClick={() => fileInputRef.current?.click()}
                >
                  {imagePreview ? (
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="w-full h-40 object-contain rounded-lg mx-auto"
                    />
                  ) : (
                    <div className="py-6">
                      <div className="text-4xl mb-2">📷</div>
                      <p className="text-sm text-gray-500">Click to upload image</p>
                      <p className="text-xs text-gray-400 mt-1">JPG, PNG · Max 5MB</p>
                    </div>
                  )}
                </div>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/jpeg,image/png"
                  className="hidden"
                  onChange={handleImageChange}
                />
                {imagePreview && (
                  <button
                    className="text-xs text-red-500 mt-1 underline"
                    onClick={() => { setImageFile(null); setImagePreview('') }}
                  >
                    Remove image
                  </button>
                )}
              </div>

              {/* Name */}
              <div>
                <label className="label">Item Name *</label>
                <input
                  className="input-field"
                  placeholder="e.g. Arduino Uno R3"
                  value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                />
              </div>

              {/* Description */}
              <div>
                <label className="label">Description</label>
                <textarea
                  className="input-field h-20 resize-none"
                  placeholder="Brief description of the equipment..."
                  value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                />
              </div>

              {/* Category + Condition */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="label">Category *</label>
                  <select
                    className="input-field"
                    value={form.category}
                    onChange={e => setForm(f => ({ ...f, category: e.target.value }))}
                  >
                    <option value="">Select category</option>
                    {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div>
                  <label className="label">Condition *</label>
                  <select
                    className="input-field"
                    value={form.condition}
                    onChange={e => setForm(f => ({ ...f, condition: e.target.value }))}
                  >
                    {CONDITIONS.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
              </div>

              {/* Quantity */}
              <div>
                <label className="label">Quantity *</label>
                <input
                  type="number"
                  min="1"
                  className="input-field w-32"
                  value={form.quantity}
                  onChange={e => setForm(f => ({ ...f, quantity: e.target.value }))}
                />
              </div>

              {/* Specifications */}
              <div>
                <label className="label">Specifications</label>
                <textarea
                  className="input-field h-20 resize-none"
                  placeholder="Technical specs, model number, etc..."
                  value={form.specifications}
                  onChange={e => setForm(f => ({ ...f, specifications: e.target.value }))}
                />
              </div>
            </div>

            <div className="px-6 py-4 border-t border-gray-200 flex gap-3 justify-end">
              <button
                onClick={() => setModalOpen(false)}
                className="px-5 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                disabled={saving || uploadingImage}
                className="btn-primary w-auto px-6 py-2.5 text-sm"
              >
                {uploadingImage ? 'Uploading image...' : saving ? 'Saving...' : editItem ? 'Save Changes' : 'Add Item'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Delete Confirm Modal ── */}
      {deleteTarget && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6">
            <div className="text-4xl text-center mb-3">🗑️</div>
            <h2 className="text-lg font-bold text-gray-800 text-center">Delete Item?</h2>
            <p className="text-sm text-gray-500 text-center mt-2">
              Are you sure you want to delete <strong>{deleteTarget.name}</strong>? This cannot be undone.
            </p>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setDeleteTarget(null)}
                className="flex-1 px-4 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="flex-1 px-4 py-2.5 text-sm font-semibold text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors disabled:opacity-60"
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function ConditionBadge({ condition }) {
  const colors = {
    Excellent: 'bg-green-100 text-green-700',
    Good: 'bg-blue-100 text-blue-700',
    Fair: 'bg-yellow-100 text-yellow-700',
    Poor: 'bg-red-100 text-red-600',
  }
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${colors[condition] || 'bg-gray-100 text-gray-600'}`}>
      {condition}
    </span>
  )
}