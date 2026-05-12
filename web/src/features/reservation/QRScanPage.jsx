import { useRef, useState, useEffect, useCallback } from 'react'
import jsQR from 'jsqr'
import { reservationService } from './api'

const STATUS_BADGE = {
  PENDING:  'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  RETURNED: 'badge-returned',
  OVERDUE:  'badge-overdue',
}

export default function QRScanPage() {
  const fileInputRef = useRef(null)
  const canvasRef    = useRef(null)
  const videoRef     = useRef(null)
  const streamRef    = useRef(null)
  const rafRef       = useRef(null)

  const [mode,        setMode]        = useState('idle')
  const [cameraError, setCameraError] = useState('')
  const [scanning,    setScanning]    = useState(false)
  const [preview,     setPreview]     = useState(null)
  const [decoding,    setDecoding]    = useState(false)
  const [decodeError, setDecodeError] = useState('')
  const [reservation, setReservation] = useState(null)
  const [fetchError,  setFetchError]  = useState('')
  const [actionLoading, setActionLoading] = useState('')
  const [actionDone,  setActionDone]  = useState('')

  const scanFrame = useCallback(() => {
    const video  = videoRef.current
    const canvas = canvasRef.current
    if (!video || !canvas || video.readyState !== video.HAVE_ENOUGH_DATA) {
      rafRef.current = requestAnimationFrame(scanFrame)
      return
    }
    canvas.width  = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0)
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const result = jsQR(imageData.data, imageData.width, imageData.height)
    if (result) { stopCamera(); processQrContent(result.data) }
    else rafRef.current = requestAnimationFrame(scanFrame)
  }, [])

  function stopCamera() {
    if (rafRef.current) cancelAnimationFrame(rafRef.current)
    if (streamRef.current) { streamRef.current.getTracks().forEach(t => t.stop()); streamRef.current = null }
    setScanning(false)
  }

  async function startCamera() {
    clearResults()
    setCameraError('')
    setMode('camera')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
      streamRef.current = stream
      if (videoRef.current) { videoRef.current.srcObject = stream; videoRef.current.play() }
      setScanning(true)
      rafRef.current = requestAnimationFrame(scanFrame)
    } catch (err) {
      setCameraError(err.name === 'NotAllowedError' ? 'Camera permission denied.' : `Camera error: ${err.message}`)
      setMode('idle')
    }
  }

  useEffect(() => () => stopCamera(), [])

  function handleFileChange(e) {
    const file = e.target.files?.[0]
    if (!file) return
    if (!file.type.startsWith('image/')) {
      setDecodeError('Only image files are supported (PNG, JPG, WebP).')
      if (fileInputRef.current) fileInputRef.current.value = ''
      return
    }
    clearResults()
    setMode('upload')
    setDecoding(true)
    const reader = new FileReader()
    reader.onload = evt => {
      const dataUrl = evt.target.result
      setPreview(dataUrl)
      const img = new Image()
      img.onload = () => {
        const canvas = canvasRef.current
        if (!canvas) return
        canvas.width  = img.naturalWidth
        canvas.height = img.naturalHeight
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0)
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
        const result = jsQR(imageData.data, imageData.width, imageData.height)
        setDecoding(false)
        if (!result) { setDecodeError('Could not detect a QR code. Try a clearer photo.'); return }
        processQrContent(result.data)
      }
      img.onerror = () => { setDecoding(false); setDecodeError('Could not load the image.') }
      img.src = dataUrl
    }
    reader.onerror = () => { setDecoding(false); setDecodeError('Could not read the file.') }
    reader.readAsDataURL(file)
  }

  async function processQrContent(content) {
    if (!content.startsWith('TECHLOAN-RESERVATION-')) {
      setDecodeError(`Not a TechLoan QR code.\nDecoded: "${content}"`)
      return
    }
    const id = parseInt(content.replace('TECHLOAN-RESERVATION-', ''), 10)
    if (isNaN(id)) { setDecodeError('Invalid reservation ID in QR code.'); return }
    try {
      const res = await reservationService.getReservation(id)
      setReservation(res.data)
    } catch (err) {
      setFetchError(err?.response?.data?.message || `Reservation #${id} not found.`)
    }
  }

  async function handleApprove() {
    if (!reservation) return
    setActionLoading('approve')
    try {
      await reservationService.approveReservation(reservation.id)
      setReservation(p => ({ ...p, status: 'APPROVED' }))
      setActionDone('Reservation approved successfully.')
    } catch (err) {
      setFetchError(err?.response?.data?.error || 'Failed to approve.')
    } finally { setActionLoading('') }
  }

  async function handleReject() {
    if (!reservation) return
    const reason = window.prompt('Reason for rejection (optional):') ?? ''
    setActionLoading('reject')
    try {
      await reservationService.rejectReservation(reservation.id, reason)
      setReservation(p => ({ ...p, status: 'REJECTED' }))
      setActionDone('Reservation rejected.')
    } catch (err) {
      setFetchError(err?.response?.data?.error || 'Failed to reject.')
    } finally { setActionLoading('') }
  }

  function clearResults() {
    setPreview(null); setDecodeError(''); setFetchError(''); setReservation(null); setActionDone(''); setActionLoading('')
  }

  function reset() {
    stopCamera(); clearResults(); setMode('idle')
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  return (
    <div className="p-6 space-y-5 max-w-2xl mx-auto">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">QR Code Scanner</h1>
        <p className="text-sm text-gray-500 mt-0.5">Scan student reservation QR codes to approve or process</p>
      </div>

      <canvas ref={canvasRef} aria-hidden="true" style={{ position: 'absolute', top: -9999, left: -9999 }} />

      {/* Mode selector */}
      {mode === 'idle' && (
        <div className="grid grid-cols-2 gap-4">
          <button
            onClick={startCamera}
            className="bg-white rounded-2xl border-2 border-dashed border-gray-200 p-8 text-center hover:border-primary transition-all group"
          >
            <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-3 transition-all group-hover:scale-110"
              style={{ background: '#FDF2F4' }}>
              <svg className="w-7 h-7" fill="none" stroke="#BE1B39" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                  d="M15 10l4.553-2.069A1 1 0 0121 8.882v6.235a1 1 0 01-1.447.894L15 14M3 8a2 2 0 012-2h8a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8z" />
              </svg>
            </div>
            <p className="font-bold text-gray-800">Use Camera</p>
            <p className="text-xs text-gray-400 mt-0.5">Point at a QR code to scan</p>
          </button>

          <button
            onClick={() => fileInputRef.current?.click()}
            className="bg-white rounded-2xl border-2 border-dashed border-gray-200 p-8 text-center hover:border-primary transition-all group"
          >
            <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-3 transition-all group-hover:scale-110"
              style={{ background: '#FDF2F4' }}>
              <svg className="w-7 h-7" fill="none" stroke="#BE1B39" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="font-bold text-gray-800">Upload Image</p>
            <p className="text-xs text-gray-400 mt-0.5">PNG, JPG, WebP or screenshot</p>
          </button>
        </div>
      )}

      <input ref={fileInputRef} type="file" accept="image/png,image/jpeg,image/webp,image/gif,image/bmp"
        className="hidden" onChange={handleFileChange} />

      {/* Camera view */}
      {mode === 'camera' && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="relative">
            <video ref={videoRef} className="w-full" autoPlay playsInline muted />
            {scanning && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="w-52 h-52 rounded-2xl" style={{ border: '3px solid rgba(244,196,48,0.8)', boxShadow: '0 0 0 9999px rgba(0,0,0,0.4)' }} />
              </div>
            )}
          </div>
          <div className="px-5 py-4 flex items-center justify-between border-t border-gray-100">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full animate-pulse" style={{ background: '#BE1B39' }} />
              <p className="text-sm text-gray-600 font-medium">
                {scanning ? 'Scanning… point at a QR code' : 'Starting camera…'}
              </p>
            </div>
            <button onClick={reset} className="text-xs font-semibold hover:underline" style={{ color: '#BE1B39' }}>
              Stop
            </button>
          </div>
        </div>
      )}

      {/* Upload preview */}
      {mode === 'upload' && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 flex flex-col items-center gap-3">
          {preview
            ? <img src={preview} alt="Uploaded QR code" className="max-h-56 rounded-xl object-contain border border-gray-100" />
            : <div className="w-full h-32 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 text-sm">Loading…</div>
          }
          {decoding && (
            <div className="flex items-center gap-2 text-sm text-gray-500">
              <div className="w-4 h-4 rounded-full border-2 border-gray-200 animate-spin" style={{ borderTopColor: '#BE1B39' }} />
              Scanning QR code…
            </div>
          )}
          <button onClick={reset} className="text-xs text-gray-400 hover:underline">Clear / try another</button>
        </div>
      )}

      {/* Errors */}
      {cameraError && (
        <div className="alert-error">{cameraError}
          <button onClick={reset} className="ml-auto text-xs underline">Dismiss</button>
        </div>
      )}
      {decodeError && (
        <div className="alert-error whitespace-pre-line">{decodeError}
          <button onClick={reset} className="ml-auto text-xs underline">Try again</button>
        </div>
      )}
      {fetchError && <div className="alert-error">{fetchError}</div>}

      {/* Success */}
      {actionDone && (
        <div className="alert-success justify-between">
          <span className="font-medium">{actionDone}</span>
          <button onClick={reset} className="text-xs underline ml-4">Scan another</button>
        </div>
      )}

      {/* Reservation card */}
      {reservation && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-md overflow-hidden">
          <div className="h-1" style={{ background: 'linear-gradient(90deg, #BE1B39, #F4C430)' }} />
          <div className="p-5 space-y-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h2 className="text-lg font-bold text-gray-900">{reservation.itemName}</h2>
                {reservation.itemDescription && (
                  <p className="text-sm text-gray-500 mt-0.5">{reservation.itemDescription}</p>
                )}
              </div>
              <span className={STATUS_BADGE[reservation.status] || 'badge-pending'}>{reservation.status}</span>
            </div>

            <div className="grid grid-cols-2 gap-3">
              {[
                ['Borrower',    reservation.userEmail || '—'],
                ['Quantity',    reservation.quantity],
                ['Return Date', reservation.returnDate || reservation.dueDate || '—'],
                ['Purpose',     reservation.purpose || '—'],
              ].map(([label, value]) => (
                <div key={label} className="rounded-xl p-3" style={{ background: '#F7F5F6' }}>
                  <p className="text-xs text-gray-400 font-medium mb-0.5">{label}</p>
                  <p className="font-semibold text-gray-800 text-sm truncate">{value}</p>
                </div>
              ))}
            </div>

            {reservation.status === 'PENDING' && !actionDone && (
              <div className="flex gap-3">
                <button
                  onClick={handleApprove}
                  disabled={!!actionLoading}
                  className="flex-1 py-2.5 text-sm font-semibold text-white rounded-xl transition-colors disabled:opacity-60"
                  style={{ background: '#10B981' }}
                  onMouseEnter={e => e.currentTarget.style.background = '#059669'}
                  onMouseLeave={e => e.currentTarget.style.background = '#10B981'}
                >
                  {actionLoading === 'approve' ? 'Approving…' : '✓ Approve'}
                </button>
                <button
                  onClick={handleReject}
                  disabled={!!actionLoading}
                  className="flex-1 py-2.5 text-sm font-semibold rounded-xl border transition-colors disabled:opacity-60"
                  style={{ color: '#BE1B39', background: '#FDF2F4', borderColor: '#FADADF' }}
                  onMouseEnter={e => e.currentTarget.style.background = '#FADADF'}
                  onMouseLeave={e => e.currentTarget.style.background = '#FDF2F4'}
                >
                  {actionLoading === 'reject' ? 'Rejecting…' : '✕ Reject'}
                </button>
              </div>
            )}

            {reservation.status !== 'PENDING' && !actionDone && (
              <p className="text-xs text-gray-400 text-center">
                This reservation is already <strong>{reservation.status}</strong> — no actions available.
              </p>
            )}

            {!actionDone && (
              <button onClick={reset} className="w-full text-xs text-gray-400 hover:text-gray-600 underline pt-1">
                Scan another QR code
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
