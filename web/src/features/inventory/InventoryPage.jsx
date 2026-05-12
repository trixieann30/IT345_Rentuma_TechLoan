import { useState, useEffect, useRef } from 'react'
import { inventoryService }   from './api'
import { reservationService } from '../reservation/api'
import { authService }        from '../auth/api'

export default function InventoryPage() {
  const [items,    setItems]    = useState([])
  const [categories, setCategories] = useState([])
  const [loading,  setLoading]  = useState(true)
  const [search,   setSearch]   = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')
  const [error,    setError]    = useState('')
  const [currentUser, setCurrentUser] = useState(null)

  const [reserveItem, setReserveItem] = useState(null)
  const [form,        setForm]        = useState({ quantity: 1, purpose: '', returnDate: '' })
  const [formError,   setFormError]   = useState('')
  const [submitting,  setSubmitting]  = useState(false)
  const [successMsg,  setSuccessMsg]  = useState('')

  // Add item
  const [addModal,        setAddModal]        = useState(false)
  const [addForm,         setAddForm]         = useState({ name: '', category: '', description: '', condition: 'Good', quantity: 1, specifications: '' })
  const [addImageFile,    setAddImageFile]    = useState(null)
  const [addImagePreview, setAddImagePreview] = useState('')
  const [addError,        setAddError]        = useState('')
  const [addSaving,       setAddSaving]       = useState(false)
  const addImageRef = useRef(null)

  // Edit item
  const [editModal,        setEditModal]        = useState(false)
  const [editTarget,       setEditTarget]       = useState(null)
  const [editForm,         setEditForm]         = useState({ name: '', category: '', description: '', condition: 'Good', quantity: 1, specifications: '' })
  const [editImageFile,    setEditImageFile]    = useState(null)
  const [editImagePreview, setEditImagePreview] = useState('')
  const [editError,        setEditError]        = useState('')
  const [editSaving,       setEditSaving]       = useState(false)
  const editImageRef = useRef(null)

  // Delete
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting,     setDeleting]     = useState(false)

  // Borrowing guide (shown once for students/faculty)
  const [showGuide, setShowGuide] = useState(false)

  useEffect(() => {
    fetchAll()
    authService.me().then(r => {
      const u = r.data
      setCurrentUser(u)
      if (u.role !== 'CUSTODIAN' && !localStorage.getItem('guide-seen')) {
        setShowGuide(true)
      }
    }).catch(() => {})
  }, [])

  async function fetchAll(skipAutoImageIds = new Set()) {
    setLoading(true)
    try {
      const [itemsRes, catsRes] = await Promise.all([
        inventoryService.getAll(),
        inventoryService.getCategories(),
      ])
      setItems(itemsRes.data)
      setCategories(catsRes.data)
      itemsRes.data
        .filter(item => !item.imageUrl && !item.userProvidedImage && !skipAutoImageIds.has(item.id))
        .forEach(item => autoFetchImage(item.id))
    } catch {
      setError('Failed to load inventory.')
    } finally {
      setLoading(false)
    }
  }

  async function autoFetchImage(itemId) {
    try {
      const res = await inventoryService.autoImage(itemId)
      if (res.data.imageUrl) {
        setItems(prev => prev.map(i => i.id === itemId ? { ...i, imageUrl: res.data.imageUrl } : i))
      }
    } catch {}
  }

  function dismissGuide() {
    localStorage.setItem('guide-seen', '1')
    setShowGuide(false)
  }

  function handleImageChange(e, setFile, setPreview) {
    const file = e.target.files?.[0]
    if (!file) return
    setFile(file)
    const reader = new FileReader()
    reader.onload = ev => setPreview(ev.target.result)
    reader.readAsDataURL(file)
  }

  const filtered = items.filter(item => {
    const matchSearch = !search ||
      item.name?.toLowerCase().includes(search.toLowerCase()) ||
      item.description?.toLowerCase().includes(search.toLowerCase())
    const matchCat = !selectedCategory || item.category === selectedCategory
    return matchSearch && matchCat
  })

  const hasVerifiedCitEmail = currentUser && currentUser.role !== 'CUSTODIAN' && currentUser.emailVerified && (
    currentUser.email?.toLowerCase().endsWith('@cit.edu') ||
    currentUser.institutionalEmail?.toLowerCase().endsWith('@cit.edu')
  )

  const borrowBlocked = currentUser && currentUser.role !== 'CUSTODIAN' && !hasVerifiedCitEmail

  function openReserve(item) {
    if (borrowBlocked) {
      setFormError('You must verify your CIT-U email before reserving equipment. Please complete the verification link sent to your school email.')
      return
    }

    setReserveItem(item)
    setForm({ quantity: 1, purpose: '', returnDate: '' })
    setFormError('')
    setSuccessMsg('')
  }

  function openEdit(item) {
    setEditTarget(item)
    setEditForm({
      name:           item.itemName || item.name || '',
      category:       item.category || '',
      description:    item.description || '',
      condition:      item.condition || 'Good',
      quantity:       item.totalQuantity ?? item.quantity ?? 1,
      specifications: item.specifications || '',
    })
    setEditError('')
    setEditImageFile(null)
    setEditImagePreview(item.imageUrl || '')
    setEditModal(true)
  }

  async function handleReserve() {
    setFormError('')
    if (!form.purpose.trim()) return setFormError('Purpose is required.')
    if (!form.returnDate) return setFormError('Return date is required.')
    if (new Date(form.returnDate) <= new Date()) return setFormError('Return date must be in the future.')
    if (form.quantity < 1 || form.quantity > (reserveItem.availableQuantity || 1))
      return setFormError(`Quantity must be between 1 and ${reserveItem.availableQuantity}.`)

    setSubmitting(true)
    try {
      await reservationService.createReservation({
        inventoryId: reserveItem.id,
        quantity: Number(form.quantity),
        purpose: form.purpose,
        returnDate: form.returnDate,
      })
      setSuccessMsg("Reservation submitted! You'll be notified once it is approved.")
      await fetchAll()
    } catch (e) {
      setFormError(e?.response?.data?.error || 'Failed to submit reservation.')
    } finally {
      setSubmitting(false)
    }
  }

  const isCustodian = currentUser?.role === 'CUSTODIAN'

  async function handleAddItem() {
    setAddError('')
    if (!addForm.name.trim())        return setAddError('Item name is required.')
    if (!addForm.category.trim())    return setAddError('Category is required.')
    if (!addForm.description.trim()) return setAddError('Description is required.')
    if (addForm.quantity < 1)        return setAddError('Quantity must be at least 1.')
    setAddSaving(true)
    try {
      const res = await inventoryService.createItem({
        name:           addForm.name.trim(),
        category:       addForm.category.trim(),
        description:    addForm.description.trim(),
        condition:      addForm.condition,
        quantity:       Number(addForm.quantity),
        specifications: addForm.specifications.trim() || null,
      })
      const newId = res.data?.id
      let imageWarn = ''
      if (addImageFile && newId) {
        try {
          await inventoryService.uploadImage(newId, addImageFile)
        } catch {
          imageWarn = 'Item added, but the image upload failed. Use the edit button to try again.'
        }
      }
      setAddModal(false)
      setAddForm({ name: '', category: '', description: '', condition: 'Good', quantity: 1, specifications: '' })
      setAddImageFile(null)
      setAddImagePreview('')
      await fetchAll(addImageFile && newId ? new Set([newId]) : new Set())
      if (imageWarn) setError(imageWarn)
    } catch (e) {
      setAddError(e?.response?.data?.error?.message || 'Failed to add item.')
    } finally {
      setAddSaving(false)
    }
  }

  async function handleEditItem() {
    setEditError('')
    if (!editForm.name.trim())        return setEditError('Item name is required.')
    if (!editForm.category.trim())    return setEditError('Category is required.')
    if (!editForm.description.trim()) return setEditError('Description is required.')
    if (editForm.quantity < 1)        return setEditError('Quantity must be at least 1.')
    setEditSaving(true)
    try {
      await inventoryService.updateItem(editTarget.id, {
        name:           editForm.name.trim(),
        category:       editForm.category.trim(),
        description:    editForm.description.trim(),
        condition:      editForm.condition,
        quantity:       Number(editForm.quantity),
        specifications: editForm.specifications.trim() || null,
      })
      let editImageWarn = ''
      if (editImageFile) {
        try {
          await inventoryService.uploadImage(editTarget.id, editImageFile)
        } catch {
          editImageWarn = 'Item updated, but the image upload failed. Please try again.'
        }
      }
      setEditModal(false)
      await fetchAll(editImageFile ? new Set([editTarget.id]) : new Set())
      if (editImageWarn) setError(editImageWarn)
    } catch (e) {
      setEditError(e?.response?.data?.error?.message || 'Failed to update item.')
    } finally {
      setEditSaving(false)
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await inventoryService.deleteItem(deleteTarget.id)
      setDeleteTarget(null)
      await fetchAll()
    } catch {
      setDeleteTarget(null)
    } finally {
      setDeleting(false)
    }
  }

  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const minDate = tomorrow.toISOString().split('T')[0]

  return (
    <div className="p-6 space-y-5 max-w-7xl mx-auto">

      {/* Page header */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900">
            {isCustodian ? 'Manage Inventory' : 'Browse Inventory'}
          </h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {isCustodian ? 'Add, edit, and remove lab equipment' : 'Reserve available lab equipment for your needs'}
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {!isCustodian && (
            <button
              onClick={() => setShowGuide(true)}
              title="How to borrow"
              className="w-9 h-9 rounded-xl border border-gray-200 flex items-center justify-center text-gray-500 hover:border-gray-300 hover:bg-gray-50 transition-colors text-sm font-bold"
            >
              ?
            </button>
          )}
          {isCustodian && (
            <button
              onClick={() => {
                setAddForm({ name: '', category: '', description: '', condition: 'Good', quantity: 1, specifications: '' })
                setAddImageFile(null)
                setAddImagePreview('')
                setAddError('')
                setAddModal(true)
              }}
              className="flex items-center gap-2 px-4 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors"
              style={{ background: '#BE1B39' }}
              onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
              onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
              </svg>
              Add Item
            </button>
          )}
        </div>
      </div>

      {/* Search + filter bar */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex flex-wrap gap-3 items-center">
        <div className="relative flex-1 min-w-[200px]">
          <input
            className="input-field pl-10"
            placeholder="Search equipment…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <svg className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <div className="flex gap-2 flex-wrap">
          <button
            onClick={() => setSelectedCategory('')}
            className="px-3.5 py-1.5 rounded-xl text-sm font-semibold border transition-all"
            style={!selectedCategory
              ? { background: '#BE1B39', color: '#fff', borderColor: '#BE1B39' }
              : { background: '#fff', color: '#6B7280', borderColor: '#E5E7EB' }}
          >
            All
          </button>
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className="px-3.5 py-1.5 rounded-xl text-sm font-semibold border transition-all"
              style={selectedCategory === cat
                ? { background: '#BE1B39', color: '#fff', borderColor: '#BE1B39' }
                : { background: '#fff', color: '#6B7280', borderColor: '#E5E7EB' }}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>
      {borrowBlocked && (
        <div className="rounded-2xl border border-red-200 bg-red-50 text-red-700 px-4 py-3 mt-4">
          <p className="font-semibold">CIT-U email verification required</p>
          <p className="text-sm mt-1">
            You need a verified CIT-U email address to reserve equipment. Please verify the institutional email you provided during registration, then try again.
          </p>
        </div>
      )}

      {error && <div className="alert-error">{error}</div>}

      {/* Grid */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="bg-white rounded-2xl border border-gray-100 p-4 animate-pulse">
              <div className="h-36 bg-gray-100 rounded-xl mb-3" />
              <div className="h-4 bg-gray-100 rounded w-3/4 mb-2" />
              <div className="h-3 bg-gray-100 rounded w-1/2" />
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20">
          <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-3" style={{ background: '#FDF2F4' }}>
            <svg className="w-7 h-7" fill="none" stroke="#BE1B39" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
              <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <p className="font-semibold text-gray-700">No items found</p>
          <p className="text-sm text-gray-400 mt-1">Try a different search or category</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map(item => (
            <div key={item.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm flex flex-col overflow-hidden hover:shadow-md transition-shadow">
              <div className="relative w-full h-40 flex items-center justify-center overflow-hidden"
                style={{ background: item.imageUrl ? '#F9FAFB' : getCategoryBg(item.category) }}>
                {item.imageUrl
                  ? <img src={item.imageUrl} alt={item.itemName || item.name} className="w-full h-full object-cover" />
                  : <ItemIllustration category={item.category} />
                }
                {isCustodian && (
                  <div className="absolute top-2 right-2 flex gap-1">
                    <button
                      onClick={() => openEdit(item)}
                      className="w-8 h-8 rounded-lg flex items-center justify-center transition-colors"
                      style={{ background: 'rgba(59,130,246,0.9)', color: '#fff' }}
                      title="Edit item"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                    </button>
                    <button
                      onClick={() => setDeleteTarget(item)}
                      className="w-8 h-8 rounded-lg flex items-center justify-center transition-colors"
                      style={{ background: 'rgba(190,27,57,0.9)', color: '#fff' }}
                      title="Delete item"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                )}
              </div>
              <div className="flex-1 p-4">
                <div className="flex items-start justify-between gap-2 mb-1">
                  <h3 className="font-bold text-gray-800 text-sm leading-tight flex-1">{item.itemName || item.name}</h3>
                  <span className={`shrink-0 text-[11px] font-bold px-2 py-0.5 rounded-full ${
                    item.available ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-500'
                  }`}>
                    {item.available ? `${item.availableQuantity} avail.` : 'Unavail.'}
                  </span>
                </div>
                {item.category && <p className="text-[11px] text-gray-400 mb-1">{item.category}</p>}
                {item.description && <p className="text-xs text-gray-500 line-clamp-2 mb-2">{item.description}</p>}
                <p className="text-xs text-gray-400">Condition: <span className="font-semibold text-gray-600">{item.condition}</span></p>
              </div>
              <div className="px-4 pb-4">
                {!isCustodian && (
                  <button
                    onClick={() => openReserve(item)}
                    disabled={!item.available || item.availableQuantity < 1 || borrowBlocked}
                    className="w-full py-2 text-sm font-semibold rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                    style={{ background: item.available && !borrowBlocked ? '#BE1B39' : '#E5E7EB', color: item.available && !borrowBlocked ? '#fff' : '#9CA3AF' }}
                    onMouseEnter={e => { if (item.available && !borrowBlocked) e.currentTarget.style.background = '#9C1530' }}
                    onMouseLeave={e => { if (item.available && !borrowBlocked) e.currentTarget.style.background = '#BE1B39' }}
                  >
                    {borrowBlocked ? 'Verify CIT-U Email' : item.available ? 'Reserve Now' : 'Unavailable'}
                  </button>
                )}
                {isCustodian && (
                  <div className="text-xs text-gray-400 text-center py-1">
                    Total: <span className="font-semibold text-gray-600">{item.totalQuantity}</span> · Available: <span className="font-semibold text-gray-600">{item.availableQuantity}</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── Borrowing Guide Modal ────────────────────────────────────── */}
      {showGuide && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #F4C430)' }} />
            <div className="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
              <div>
                <h2 className="text-base font-bold text-gray-900">How to Borrow Equipment</h2>
                <p className="text-xs text-gray-500 mt-0.5">TechLoan — CIT-U Lab Equipment System</p>
              </div>
              <button onClick={dismissGuide}
                className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 transition-colors">✕</button>
            </div>
            <div className="px-6 py-5 space-y-5 max-h-[65vh] overflow-y-auto">

              {/* Steps */}
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-gray-400 mb-3">Borrowing Steps</p>
                <div className="space-y-3">
                  {[
                    ['Browse', 'Search the inventory and find the equipment you need.'],
                    ['Reserve', 'Click "Reserve Now" and fill in the quantity, purpose, and return date.'],
                    ['Wait for Approval', 'A custodian will review your request and approve or reject it. You\'ll be notified by email.'],
                    ['Pick Up', 'Once approved, collect the equipment from the designated custodian.'],
                    ['Return On Time', 'Return the item on or before your specified return date.'],
                  ].map(([title, desc], i) => (
                    <div key={i} className="flex gap-3">
                      <div className="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 text-xs font-black text-white"
                        style={{ background: '#BE1B39' }}>
                        {i + 1}
                      </div>
                      <div className="flex-1 pt-0.5">
                        <p className="text-sm font-semibold text-gray-800">{title}</p>
                        <p className="text-xs text-gray-500 mt-0.5">{desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Terms */}
              <div className="rounded-xl p-4 space-y-2" style={{ background: '#FDF2F4', border: '1px solid #FADADF' }}>
                <p className="text-xs font-black uppercase tracking-widest" style={{ color: '#BE1B39' }}>Terms & Conditions</p>
                <ul className="space-y-1.5 text-xs text-gray-600">
                  {[
                    'Equipment must be used for educational or academic purposes only.',
                    'Handle all equipment with care. Report any damage immediately to the custodian.',
                    'Late returns incur 1 penalty point per day overdue. Each penalty point is equivalent to ₱50.00.',
                    'Accumulated penalty points may restrict your borrowing privileges.',
                    'Lost or severely damaged items may require replacement or payment.',
                    'Borrowing for personal, commercial, or off-campus use is not permitted.',
                  ].map((t, i) => (
                    <li key={i} className="flex gap-2">
                      <span className="mt-0.5 w-3 h-3 rounded-full flex-shrink-0 flex items-center justify-center" style={{ background: '#BE1B39' }}>
                        <span className="block w-1 h-1 rounded-full bg-white" />
                      </span>
                      {t}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-gray-100">
              <button onClick={dismissGuide}
                className="w-full py-3 text-sm font-semibold text-white rounded-xl transition-colors"
                style={{ background: '#BE1B39' }}
                onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
                onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}>
                I Understand — Let me Browse
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Add Item Modal ───────────────────────────────────────────── */}
      {addModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #F4C430)' }} />
            <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <h2 className="text-base font-bold text-gray-900">Add Inventory Item</h2>
              <button onClick={() => setAddModal(false)}
                className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 transition-colors">✕</button>
            </div>
            <div className="px-6 py-5 space-y-4 max-h-[70vh] overflow-y-auto">
              {addError && <div className="alert-error text-sm">{addError}</div>}

              {/* Image upload */}
              <div>
                <label className="label">Item Photo <span className="text-gray-400 normal-case font-normal">(optional)</span></label>
                <div
                  onClick={() => addImageRef.current?.click()}
                  className="relative h-36 rounded-xl border-2 border-dashed flex items-center justify-center overflow-hidden cursor-pointer transition-colors"
                  style={{ borderColor: addImagePreview ? '#BE1B39' : '#E5E7EB', background: addImagePreview ? 'transparent' : '#F9FAFB' }}
                  onMouseEnter={e => { if (!addImagePreview) e.currentTarget.style.borderColor = '#BE1B39' }}
                  onMouseLeave={e => { if (!addImagePreview) e.currentTarget.style.borderColor = '#E5E7EB' }}
                >
                  {addImagePreview
                    ? <img src={addImagePreview} alt="Preview" className="w-full h-full object-cover" />
                    : (
                      <div className="text-center">
                        <svg className="w-8 h-8 mx-auto mb-1 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p className="text-xs text-gray-400">Click to upload photo</p>
                      </div>
                    )
                  }
                  {addImagePreview && (
                    <button
                      onClick={e => { e.stopPropagation(); setAddImageFile(null); setAddImagePreview('') }}
                      className="absolute top-2 right-2 w-7 h-7 rounded-lg flex items-center justify-center text-white text-xs"
                      style={{ background: 'rgba(0,0,0,0.6)' }}
                    >✕</button>
                  )}
                </div>
                <input ref={addImageRef} type="file" accept="image/*" className="hidden"
                  onChange={e => handleImageChange(e, setAddImageFile, setAddImagePreview)} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="label">Item Name *</label>
                  <input className="input-field" placeholder="e.g. Dell XPS 15 with RTX 4060"
                    value={addForm.name} onChange={e => setAddForm(f => ({ ...f, name: e.target.value }))} />
                </div>
                <div>
                  <label className="label">Category *</label>
                  <input className="input-field" placeholder="e.g. Laptop, Camera, Projector"
                    value={addForm.category} onChange={e => setAddForm(f => ({ ...f, category: e.target.value }))}
                    list="category-suggestions-add" />
                  <datalist id="category-suggestions-add">
                    {categories.map(c => <option key={c} value={c} />)}
                  </datalist>
                </div>
                <div>
                  <label className="label">Condition *</label>
                  <select className="input-field" value={addForm.condition}
                    onChange={e => setAddForm(f => ({ ...f, condition: e.target.value }))}>
                    {['Excellent', 'Good', 'Fair', 'Poor'].map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
                <div>
                  <label className="label">Total Quantity *</label>
                  <input type="number" min="1" className="input-field"
                    value={addForm.quantity} onChange={e => setAddForm(f => ({ ...f, quantity: e.target.value }))} />
                </div>
                <div className="sm:col-span-2">
                  <label className="label">Description *</label>
                  <textarea className="input-field h-20 resize-none" placeholder="Brief description of the item…"
                    value={addForm.description} onChange={e => setAddForm(f => ({ ...f, description: e.target.value }))} />
                </div>
                <div className="sm:col-span-2">
                  <label className="label">Specifications <span className="text-gray-400 normal-case font-normal">(optional)</span></label>
                  <textarea className="input-field h-16 resize-none" placeholder="Technical specs, model number, etc."
                    value={addForm.specifications} onChange={e => setAddForm(f => ({ ...f, specifications: e.target.value }))} />
                </div>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-gray-100 flex gap-3 justify-end">
              <button onClick={() => setAddModal(false)}
                className="px-5 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors">
                Cancel
              </button>
              <button onClick={handleAddItem} disabled={addSaving}
                className="px-6 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-50"
                style={{ background: '#BE1B39' }}
                onMouseEnter={e => { if (!addSaving) e.currentTarget.style.background = '#9C1530' }}
                onMouseLeave={e => { if (!addSaving) e.currentTarget.style.background = '#BE1B39' }}>
                {addSaving ? 'Adding…' : 'Add Item'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Item Modal ──────────────────────────────────────────── */}
      {editModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #3B82F6, #60A5FA)' }} />
            <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <h2 className="text-base font-bold text-gray-900">Edit Item</h2>
              <button onClick={() => setEditModal(false)}
                className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 transition-colors">✕</button>
            </div>
            <div className="px-6 py-5 space-y-4 max-h-[70vh] overflow-y-auto">
              {editError && <div className="alert-error text-sm">{editError}</div>}

              {/* Image upload */}
              <div>
                <label className="label">Item Photo <span className="text-gray-400 normal-case font-normal">(optional — replaces current)</span></label>
                <div
                  onClick={() => editImageRef.current?.click()}
                  className="relative h-36 rounded-xl border-2 border-dashed flex items-center justify-center overflow-hidden cursor-pointer transition-colors"
                  style={{ borderColor: editImagePreview ? '#3B82F6' : '#E5E7EB', background: editImagePreview ? 'transparent' : '#F9FAFB' }}
                  onMouseEnter={e => { if (!editImagePreview) e.currentTarget.style.borderColor = '#3B82F6' }}
                  onMouseLeave={e => { if (!editImagePreview) e.currentTarget.style.borderColor = '#E5E7EB' }}
                >
                  {editImagePreview
                    ? <img src={editImagePreview} alt="Preview" className="w-full h-full object-cover" />
                    : (
                      <div className="text-center">
                        <svg className="w-8 h-8 mx-auto mb-1 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p className="text-xs text-gray-400">Click to upload new photo</p>
                      </div>
                    )
                  }
                  {editImagePreview && (
                    <button
                      onClick={e => { e.stopPropagation(); setEditImageFile(null); setEditImagePreview('') }}
                      className="absolute top-2 right-2 w-7 h-7 rounded-lg flex items-center justify-center text-white text-xs"
                      style={{ background: 'rgba(0,0,0,0.6)' }}
                    >✕</button>
                  )}
                </div>
                <input ref={editImageRef} type="file" accept="image/*" className="hidden"
                  onChange={e => handleImageChange(e, setEditImageFile, setEditImagePreview)} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="label">Item Name *</label>
                  <input className="input-field"
                    value={editForm.name} onChange={e => setEditForm(f => ({ ...f, name: e.target.value }))} />
                </div>
                <div>
                  <label className="label">Category *</label>
                  <input className="input-field"
                    value={editForm.category} onChange={e => setEditForm(f => ({ ...f, category: e.target.value }))}
                    list="category-suggestions-edit" />
                  <datalist id="category-suggestions-edit">
                    {categories.map(c => <option key={c} value={c} />)}
                  </datalist>
                </div>
                <div>
                  <label className="label">Condition *</label>
                  <select className="input-field" value={editForm.condition}
                    onChange={e => setEditForm(f => ({ ...f, condition: e.target.value }))}>
                    {['Excellent', 'Good', 'Fair', 'Poor'].map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
                <div>
                  <label className="label">Total Quantity *</label>
                  <input type="number" min="1" className="input-field"
                    value={editForm.quantity} onChange={e => setEditForm(f => ({ ...f, quantity: e.target.value }))} />
                </div>
                <div className="sm:col-span-2">
                  <label className="label">Description *</label>
                  <textarea className="input-field h-20 resize-none"
                    value={editForm.description} onChange={e => setEditForm(f => ({ ...f, description: e.target.value }))} />
                </div>
                <div className="sm:col-span-2">
                  <label className="label">Specifications <span className="text-gray-400 normal-case font-normal">(optional)</span></label>
                  <textarea className="input-field h-16 resize-none" placeholder="Technical specs, model number, etc."
                    value={editForm.specifications} onChange={e => setEditForm(f => ({ ...f, specifications: e.target.value }))} />
                </div>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-gray-100 flex gap-3 justify-end">
              <button onClick={() => setEditModal(false)}
                className="px-5 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors">
                Cancel
              </button>
              <button onClick={handleEditItem} disabled={editSaving}
                className="px-6 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-50"
                style={{ background: '#3B82F6' }}
                onMouseEnter={e => { if (!editSaving) e.currentTarget.style.background = '#2563EB' }}
                onMouseLeave={e => { if (!editSaving) e.currentTarget.style.background = '#3B82F6' }}>
                {editSaving ? 'Saving…' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Delete confirmation ──────────────────────────────────────── */}
      {deleteTarget && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden">
            <div className="h-1" style={{ background: '#BE1B39' }} />
            <div className="px-6 py-6 text-center space-y-3">
              <div className="w-12 h-12 rounded-2xl flex items-center justify-center mx-auto" style={{ background: '#FDF2F4' }}>
                <svg className="w-6 h-6" fill="none" stroke="#BE1B39" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </div>
              <div>
                <p className="font-bold text-gray-900">Delete Item?</p>
                <p className="text-sm text-gray-500 mt-1">
                  "<span className="font-semibold">{deleteTarget.itemName || deleteTarget.name}</span>" will be permanently removed.
                </p>
              </div>
            </div>
            <div className="px-6 pb-6 flex gap-3">
              <button onClick={() => setDeleteTarget(null)} disabled={deleting}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors">
                Cancel
              </button>
              <button onClick={handleDelete} disabled={deleting}
                className="flex-1 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-50"
                style={{ background: '#BE1B39' }}>
                {deleting ? 'Deleting…' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Reservation modal ────────────────────────────────────────── */}
      {reserveItem && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
            <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #F4C430)' }} />
            <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <h2 className="text-base font-bold text-gray-900">Reserve Equipment</h2>
              <button onClick={() => setReserveItem(null)}
                className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-gray-100 transition-colors">
                ✕
              </button>
            </div>

            <div className="px-6 py-5 space-y-4">
              <div className="flex gap-3 p-3 rounded-xl" style={{ background: '#F7F5F6' }}>
                {reserveItem.imageUrl
                  ? <img src={reserveItem.imageUrl} alt={reserveItem.name} className="w-14 h-14 rounded-lg object-cover border border-gray-200 flex-shrink-0" />
                  : <div className="w-14 h-14 rounded-lg flex items-center justify-center flex-shrink-0" style={{ background: getCategoryBg(reserveItem.category) }}>
                      <ItemIllustration category={reserveItem.category} size="sm" />
                    </div>
                }
                <div>
                  <p className="font-bold text-gray-800">{reserveItem.name}</p>
                  <p className="text-xs text-gray-400">{reserveItem.category}</p>
                  <p className="text-xs font-semibold text-emerald-600 mt-0.5">{reserveItem.availableQuantity} available</p>
                </div>
              </div>

              {successMsg ? (
                <div className="alert-success">{successMsg}</div>
              ) : (
                <>
                  {formError && <div className="alert-error text-sm">{formError}</div>}
                  <div>
                    <label className="label">Quantity</label>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setForm(f => ({ ...f, quantity: Math.max(1, f.quantity - 1) }))}
                        className="w-9 h-9 rounded-xl border border-gray-200 text-gray-600 hover:bg-gray-100 font-bold text-lg flex items-center justify-center"
                      >−</button>
                      <input type="number" min="1" max={reserveItem.availableQuantity}
                        className="input-field w-20 text-center"
                        value={form.quantity}
                        onChange={e => setForm(f => ({ ...f, quantity: e.target.value }))} />
                      <button
                        onClick={() => setForm(f => ({ ...f, quantity: Math.min(reserveItem.availableQuantity, f.quantity + 1) }))}
                        className="w-9 h-9 rounded-xl border border-gray-200 text-gray-600 hover:bg-gray-100 font-bold text-lg flex items-center justify-center"
                      >+</button>
                    </div>
                  </div>
                  <div>
                    <label className="label">Purpose / Remarks *</label>
                    <textarea
                      className="input-field h-20 resize-none"
                      placeholder="e.g. Lab experiment for IT345 final project…"
                      value={form.purpose}
                      onChange={e => setForm(f => ({ ...f, purpose: e.target.value }))}
                    />
                  </div>
                  <div>
                    <label className="label">Return Date *</label>
                    <input type="date" min={minDate} className="input-field"
                      value={form.returnDate}
                      onChange={e => setForm(f => ({ ...f, returnDate: e.target.value }))} />
                  </div>
                </>
              )}
            </div>

            <div className="px-6 py-4 border-t border-gray-100 flex gap-3 justify-end">
              <button onClick={() => setReserveItem(null)}
                className="px-5 py-2.5 text-sm font-semibold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors">
                {successMsg ? 'Close' : 'Cancel'}
              </button>
              {!successMsg && (
                <button onClick={handleReserve} disabled={submitting}
                  className="px-6 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-50"
                  style={{ background: '#BE1B39' }}
                  onMouseEnter={e => e.currentTarget.style.background = '#9C1530'}
                  onMouseLeave={e => e.currentTarget.style.background = '#BE1B39'}>
                  {submitting ? 'Submitting…' : 'Submit Request'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

/* ── Category illustrations ─────────────────────────────────────────────── */

function getCategoryBg(category = '') {
  const c = category.toLowerCase()
  if (c.includes('laptop') || c.includes('computer') || c.includes('desktop')) return 'linear-gradient(135deg,#FDF2F4,#F2D8DE)'
  if (c.includes('camera') || c.includes('dslr') || c.includes('photo') || c.includes('video')) return 'linear-gradient(135deg,#FFFBEB,#FFF0C0)'
  if (c.includes('projector')) return 'linear-gradient(135deg,#FDF2F4,#F9E6EA)'
  if (c.includes('mic') || c.includes('microphone')) return 'linear-gradient(135deg,#FFF3F3,#FFE4E4)'
  if (c.includes('speaker') || c.includes('audio') || c.includes('sound')) return 'linear-gradient(135deg,#FFFBEB,#FFF3D0)'
  if (c.includes('cable') || c.includes('hdmi') || c.includes('usb') || c.includes('adapter')) return 'linear-gradient(135deg,#FDF2F4,#F9E6EA)'
  if (c.includes('tripod') || c.includes('stand')) return 'linear-gradient(135deg,#FFFBEB,#FFF0C0)'
  if (c.includes('tablet') || c.includes('ipad')) return 'linear-gradient(135deg,#FDF2F4,#F2D8DE)'
  if (c.includes('phone') || c.includes('smartphone') || c.includes('mobile')) return 'linear-gradient(135deg,#FFF3F3,#FFE4E4)'
  return 'linear-gradient(135deg,#FDF2F4,#F2D8DE)'
}

function ItemIllustration({ category = '', size = 'lg' }) {
  const c = category.toLowerCase()
  const cls = size === 'sm' ? 'w-7 h-7' : 'w-16 h-16'
  const p = { className: cls, viewBox: '0 0 24 24', fill: 'none', stroke: '#BE1B39', strokeWidth: '1.5', strokeLinecap: 'round', strokeLinejoin: 'round' }

  if (c.includes('laptop') || c.includes('notebook') || c.includes('computer') || c.includes('desktop'))
    return <svg {...p}><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M0 21h24M9 21l1.5-4h3L15 21M7 8h10M7 11h7"/></svg>

  if (c.includes('camera') || c.includes('dslr') || c.includes('photo') || c.includes('video'))
    return <svg {...p}><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/><circle cx="12" cy="13" r="1.5" fill="#BE1B39"/></svg>

  if (c.includes('projector'))
    return <svg {...p}><rect x="1" y="7" width="18" height="10" rx="2"/><circle cx="13" cy="12" r="3"/><path d="M19 10l4-3M19 14l4 3"/><path d="M5 10h2M5 14h2"/></svg>

  if (c.includes('mic') || c.includes('microphone'))
    return <svg {...p}><rect x="9" y="1" width="6" height="12" rx="3"/><path d="M5 10a7 7 0 0014 0M12 19v4M8 23h8"/></svg>

  if (c.includes('speaker') || c.includes('audio') || c.includes('sound'))
    return <svg {...p}><rect x="4" y="2" width="12" height="20" rx="2"/><circle cx="10" cy="13" r="3"/><circle cx="10" cy="6" r="1.5"/></svg>

  if (c.includes('cable') || c.includes('hdmi') || c.includes('usb') || c.includes('adapter') || c.includes('connector'))
    return <svg {...p}><rect x="2" y="5" width="6" height="6" rx="1"/><rect x="16" y="13" width="6" height="6" rx="1"/><path d="M8 8h8M8 16h8M12 8v8"/></svg>

  if (c.includes('tripod') || c.includes('stand'))
    return <svg {...p}><rect x="8" y="1" width="8" height="5" rx="1"/><path d="M12 6v5M12 11L5 22M12 11l7 11M12 11v11"/></svg>

  if (c.includes('tablet') || c.includes('ipad'))
    return <svg {...p}><rect x="4" y="1" width="16" height="22" rx="2"/><path d="M12 18h.01"/></svg>

  if (c.includes('phone') || c.includes('smartphone') || c.includes('mobile'))
    return <svg {...p}><rect x="5" y="2" width="14" height="20" rx="2"/><path d="M12 18h.01M9 5h6"/></svg>

  return <svg {...p}><path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
}
