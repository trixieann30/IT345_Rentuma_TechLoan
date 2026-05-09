import { useRef, useState, useEffect, useCallback } from 'react'
import jsQR from 'jsqr'
import { reservationService } from './api'

export default function QRScanPage() {
  const fileInputRef = useRef(null)
  const canvasRef    = useRef(null)
  const videoRef     = useRef(null)
  const streamRef    = useRef(null)
  const rafRef       = useRef(null)

  const [mode, setMode]               = useState('idle') // 'idle' | 'camera' | 'upload'
  const [cameraError, setCameraError] = useState('')
  const [scanning, setScanning]       = useState(false)

  const [preview, setPreview]         = useState(null)
  const [decoding, setDecoding]       = useState(false)
  const [decodeError, setDecodeError] = useState('')
  const [reservation, setReservation] = useState(null)
  const [fetchError, setFetchError]   = useState('')
  const [actionLoading, setActionLoading] = useState('')
  const [actionDone, setActionDone]   = useState('')

  // ── camera frame loop ────────────────────────────────────────────────────────
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
    if (result) {
      stopCamera()
      processQrContent(result.data)
    } else {
      rafRef.current = requestAnimationFrame(scanFrame)
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  function stopCamera() {
    if (rafRef.current) cancelAnimationFrame(rafRef.current)
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop())
      streamRef.current = null
    }
    setScanning(false)
  }

  async function startCamera() {
    clearResults()
    setCameraError('')
    setMode('camera')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      })
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        videoRef.current.play()
      }
      setScanning(true)
      rafRef.current = requestAnimationFrame(scanFrame)
    } catch (err) {
      setCameraError(
        err.name === 'NotAllowedError'
          ? 'Camera permission denied. Please allow camera access and try again.'
          : `Camera error: ${err.message}`
      )
      setMode('idle')
    }
  }

  // clean up camera when leaving the page
  useEffect(() => () => stopCamera(), [])

  // ── file upload ───────────────────────────────────────────────────────────────
  function handleFileChange(e) {
    const file = e.target.files?.[0]
    if (!file) return
    if (!file.type.startsWith('image/')) {
      setDecodeError('Only image files are supported (PNG, JPG, WebP). PDFs are not accepted.')
      if (fileInputRef.current) fileInputRef.current.value = ''
      return
    }
    clearResults()
    setMode('upload')
    setDecoding(true)

    const reader = new FileReader()
    reader.onload = (evt) => {
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
        if (!result) {
          setDecodeError('Could not detect a QR code in this image. Try a clearer or higher-resolution photo.')
          return
        }
        processQrContent(result.data)
      }
      img.onerror = () => {
        setDecoding(false)
        setDecodeError('Could not load the image. Try a different file.')
      }
      img.src = dataUrl
    }
    reader.onerror = () => {
      setDecoding(false)
      setDecodeError('Could not read the file.')
    }
    reader.readAsDataURL(file)
  }

  // ── shared logic ──────────────────────────────────────────────────────────────
  async function processQrContent(content) {
    if (!content.startsWith('TECHLOAN-RESERVATION-')) {
      setDecodeError(`Not a TechLoan QR code.\nDecoded: "${content}"`)
      return
    }
    const id = parseInt(content.replace('TECHLOAN-RESERVATION-', ''), 10)
    if (isNaN(id)) {
      setDecodeError('Invalid reservation ID in QR code.')
      return
    }
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
      setReservation(prev => ({ ...prev, status: 'APPROVED' }))
      setActionDone('Reservation approved successfully.')
    } catch (err) {
      setFetchError(err?.response?.data?.error || 'Failed to approve.')
    } finally {
      setActionLoading('')
    }
  }

  async function handleReject() {
    if (!reservation) return
    const reason = window.prompt('Reason for rejection (optional):') ?? ''
    setActionLoading('reject')
    try {
      await reservationService.rejectReservation(reservation.id, reason)
      setReservation(prev => ({ ...prev, status: 'REJECTED' }))
      setActionDone('Reservation rejected.')
    } catch (err) {
      setFetchError(err?.response?.data?.error || 'Failed to reject.')
    } finally {
      setActionLoading('')
    }
  }

  function clearResults() {
    setPreview(null)
    setDecodeError('')
    setFetchError('')
    setReservation(null)
    setActionDone('')
    setActionLoading('')
  }

  function reset() {
    stopCamera()
    clearResults()
    setMode('idle')
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const statusColors = {
    PENDING:  'bg-amber-100 text-amber-800',
    APPROVED: 'bg-green-100 text-green-700',
    REJECTED: 'bg-red-100 text-red-600',
    RETURNED: 'bg-blue-100 text-blue-700',
    OVERDUE:  'bg-orange-100 text-orange-700',
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <h1 className="text-2xl font-bold text-primary">QR Code Scanner</h1>
        <p className="text-sm text-gray-500 mt-0.5">
          Scan a student's reservation QR code using your camera or by uploading an image
        </p>
      </div>

      {/* off-screen canvas used for both file decode and camera frames */}
      <canvas ref={canvasRef} aria-hidden="true" style={{ position: 'absolute', top: -9999, left: -9999 }} />

      <div className="max-w-2xl mx-auto px-6 py-8 space-y-6">

        {/* ── mode selector (idle state) ─────────────────────────────────── */}
        {mode === 'idle' && (
          <div className="grid grid-cols-2 gap-4">
            {/* Camera option */}
            <button
              onClick={startCamera}
              className="border-2 border-dashed border-gray-300 rounded-2xl p-8 text-center
                         hover:border-primary hover:bg-primary/5 transition-colors group"
            >
              <div className="w-12 h-12 bg-gray-100 group-hover:bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3 transition-colors">
                <svg className="w-6 h-6 text-gray-400 group-hover:text-primary transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                    d="M15 10l4.553-2.069A1 1 0 0121 8.882v6.235a1 1 0 01-1.447.894L15 14M3 8a2 2 0 012-2h8a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8z" />
                </svg>
              </div>
              <p className="font-semibold text-gray-700 group-hover:text-primary transition-colors">Use Camera</p>
              <p className="text-xs text-gray-400 mt-1">Point at a QR code to scan</p>
            </button>

            {/* Upload option */}
            <button
              onClick={() => fileInputRef.current?.click()}
              className="border-2 border-dashed border-gray-300 rounded-2xl p-8 text-center
                         hover:border-primary hover:bg-primary/5 transition-colors group"
            >
              <div className="w-12 h-12 bg-gray-100 group-hover:bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3 transition-colors">
                <svg className="w-6 h-6 text-gray-400 group-hover:text-primary transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <p className="font-semibold text-gray-700 group-hover:text-primary transition-colors">Upload Image</p>
              <p className="text-xs text-gray-400 mt-1">PNG, JPG, WebP, or screenshot</p>
            </button>
          </div>
        )}

        <input
          ref={fileInputRef}
          type="file"
          accept="image/png,image/jpeg,image/webp,image/gif,image/bmp"
          className="hidden"
          onChange={handleFileChange}
        />

        {/* ── camera view ───────────────────────────────────────────────────── */}
        {mode === 'camera' && (
          <div className="bg-white rounded-2xl shadow overflow-hidden">
            <div className="relative">
              <video
                ref={videoRef}
                className="w-full rounded-t-2xl"
                autoPlay
                playsInline
                muted
              />
              {scanning && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                  {/* scanning crosshair */}
                  <div className="w-48 h-48 border-4 border-white/70 rounded-xl shadow-lg" />
                </div>
              )}
            </div>
            <div className="px-4 py-3 flex items-center justify-between">
              <p className="text-sm text-gray-500">
                {scanning ? 'Scanning… point at a QR code' : 'Starting camera…'}
              </p>
              <button onClick={reset} className="text-xs text-red-500 hover:underline font-medium">
                Stop camera
              </button>
            </div>
          </div>
        )}

        {/* ── upload preview ────────────────────────────────────────────────── */}
        {mode === 'upload' && (
          <div className="bg-white rounded-2xl shadow p-4 flex flex-col items-center gap-3">
            {preview
              ? <img src={preview} alt="Uploaded QR code" className="max-h-56 rounded-lg object-contain border border-gray-100" />
              : <div className="w-full h-32 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400 text-sm">Loading…</div>
            }
            {decoding && (
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <div className="animate-spin rounded-full h-4 w-4 border-2 border-gray-200 border-t-primary" />
                Scanning QR code…
              </div>
            )}
            <button onClick={reset} className="text-xs text-gray-400 hover:text-red-500 transition-colors underline">
              Clear / try another image
            </button>
          </div>
        )}

        {/* ── camera error ──────────────────────────────────────────────────── */}
        {cameraError && (
          <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">
            {cameraError}
            <button onClick={reset} className="ml-2 underline text-xs">Dismiss</button>
          </div>
        )}

        {/* ── decode / fetch errors ─────────────────────────────────────────── */}
        {decodeError && (
          <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700 whitespace-pre-line">
            {decodeError}
            <button onClick={reset} className="ml-2 underline text-xs">Try again</button>
          </div>
        )}
        {fetchError && (
          <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">
            {fetchError}
          </div>
        )}

        {/* ── success banner ────────────────────────────────────────────────── */}
        {actionDone && (
          <div className="bg-green-50 border border-green-200 rounded-xl p-4 text-sm text-green-700 font-medium flex items-center justify-between">
            {actionDone}
            <button onClick={reset} className="text-xs text-green-600 underline ml-4">Scan another</button>
          </div>
        )}

        {/* ── reservation card ──────────────────────────────────────────────── */}
        {reservation && (
          <div className="bg-white rounded-2xl shadow p-6 space-y-4">
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-lg font-bold text-gray-900">{reservation.itemName}</h2>
                {reservation.itemDescription && (
                  <p className="text-sm text-gray-500 mt-0.5">{reservation.itemDescription}</p>
                )}
              </div>
              <span className={`px-3 py-1 rounded-full text-xs font-bold ${statusColors[reservation.status] || 'bg-gray-100 text-gray-600'}`}>
                {reservation.status}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-0.5">Borrower</p>
                <p className="font-medium text-gray-800">{reservation.userEmail || '—'}</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-0.5">Quantity</p>
                <p className="font-medium text-gray-800">{reservation.quantity}</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-0.5">Return Date</p>
                <p className="font-medium text-gray-800">{reservation.returnDate || reservation.dueDate || '—'}</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-0.5">Purpose</p>
                <p className="font-medium text-gray-800 truncate">{reservation.purpose || '—'}</p>
              </div>
            </div>

            {reservation.status === 'PENDING' && !actionDone && (
              <div className="flex gap-3 pt-2">
                <button
                  onClick={handleApprove}
                  disabled={!!actionLoading}
                  className="flex-1 py-2.5 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-60 transition-colors"
                >
                  {actionLoading === 'approve' ? 'Approving…' : '✓ Approve'}
                </button>
                <button
                  onClick={handleReject}
                  disabled={!!actionLoading}
                  className="flex-1 py-2.5 bg-red-50 text-red-600 border border-red-200 text-sm font-semibold rounded-lg hover:bg-red-100 disabled:opacity-60 transition-colors"
                >
                  {actionLoading === 'reject' ? 'Rejecting…' : '✕ Reject'}
                </button>
              </div>
            )}

            {reservation.status !== 'PENDING' && !actionDone && (
              <p className="text-xs text-gray-400 text-center pt-1">
                This reservation is already <strong>{reservation.status}</strong> — no actions available.
              </p>
            )}

            {!actionDone && (
              <button onClick={reset} className="w-full text-xs text-gray-400 hover:text-gray-600 underline pt-1">
                Scan another QR code
              </button>
            )}
          </div>
        )}

      </div>
    </div>
  )
}
