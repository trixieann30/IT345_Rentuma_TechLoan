import { useEffect, useState } from 'react'

export default function SplashScreen({ onDone }) {
  const [fading, setFading] = useState(false)

  useEffect(() => {
    const t1 = setTimeout(() => setFading(true), 1400)
    const t2 = setTimeout(() => onDone(), 1900)
    return () => { clearTimeout(t1); clearTimeout(t2) }
  }, [onDone])

  return (
    <div
      className="fixed inset-0 z-[9999] flex flex-col items-center justify-center transition-opacity duration-500"
      style={{
        backgroundColor: '#120709',
        backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.06) 1px, transparent 1px), linear-gradient(150deg, #BE1B39 0%, #8C1229 45%, #120709 100%)',
        backgroundSize: '24px 24px, 100% 100%',
        opacity: fading ? 0 : 1,
      }}
    >
      {/* Decorative circles */}
      <div className="absolute -top-20 -right-20 w-80 h-80 rounded-full pointer-events-none"
        style={{ background: 'rgba(244,196,48,0.10)' }} />
      <div className="absolute bottom-10 -left-16 w-64 h-64 rounded-full pointer-events-none"
        style={{ background: 'rgba(224,96,96,0.07)' }} />

      {/* Logo */}
      <div className="relative flex flex-col items-center gap-4 animate-[fadeIn_0.6s_ease_forwards]">
        <div className="w-28 h-28 rounded-3xl overflow-hidden shadow-2xl bg-white flex items-center justify-center">
          <img src="/techloan_logo.png" alt="TechLoan" className="w-full h-full object-contain p-1" />
        </div>

        <div className="text-center">
          <p className="text-white font-black text-5xl tracking-tight leading-none">TechLoan</p>
          <p className="text-sm mt-2 tracking-[0.15em] uppercase" style={{ color: 'rgba(255,255,255,0.45)' }}>
            Lab Equipment Management
          </p>
        </div>

        <div className="w-10 h-0.5 rounded-full mt-1" style={{ background: '#F4C430' }} />

        <p className="text-xs mt-1" style={{ color: 'rgba(255,255,255,0.3)' }}>
          Cebu Institute of Technology – University
        </p>
      </div>

      {/* Loading dots */}
      <div className="absolute bottom-12 flex gap-2">
        {[0, 1, 2].map(i => (
          <div
            key={i}
            className="w-1.5 h-1.5 rounded-full"
            style={{
              background: 'rgba(244,196,48,0.7)',
              animation: `pulse 1.2s ease-in-out ${i * 0.2}s infinite`,
            }}
          />
        ))}
      </div>

      <style>{`
        @keyframes fadeIn { from { opacity:0; transform:translateY(16px); } to { opacity:1; transform:translateY(0); } }
        @keyframes pulse { 0%,80%,100% { opacity:0.2; transform:scale(0.8); } 40% { opacity:1; transform:scale(1); } }
      `}</style>
    </div>
  )
}
