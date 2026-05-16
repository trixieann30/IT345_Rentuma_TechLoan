import { useState } from 'react'
import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { GoogleOAuthProvider } from '@react-oauth/google'
import Layout           from './shared/components/Layout'
import SplashScreen     from './shared/components/SplashScreen'
import LoginPage        from './features/auth/LoginPage'
import RegisterPage     from './features/auth/RegisterPage'
import VerifyEmailPage     from './features/auth/VerifyEmailPage'
import ResetPasswordPage  from './features/auth/ResetPasswordPage'
import DashboardPage    from './features/dashboard/DashboardPage'
import InventoryPage    from './features/inventory/InventoryPage'
import ProfilePage      from './features/profile/ProfilePage'
import PenaltyPage      from './features/penalty/PenaltyPage'
import MyReservations   from './features/reservation/MyReservations'
import ReservationQueue from './features/reservation/ReservationQueue'
import OverdueTracker   from './features/reservation/OverdueTracker'
import QRScanPage       from './features/reservation/QRScanPage'
import PaymentSuccessPage from './features/payment/PaymentSuccessPage'

function ProtectedLayout() {
  const token = localStorage.getItem('token')
  if (!token) return <Navigate to="/login" replace />
  return <Layout />
}

export default function App() {
  const [splashDone, setSplashDone] = useState(false)

  return (
    <>
      {!splashDone && <SplashScreen onDone={() => setSplashDone(true)} />}
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <Routes>
        <Route path="/"        element={<Navigate to="/login" replace />} />
        <Route path="/login"        element={<LoginPage />} />
        <Route path="/register"     element={<RegisterPage />} />
        <Route path="/verify-email"   element={<VerifyEmailPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        <Route element={<ProtectedLayout />}>
          <Route path="/dashboard"         element={<DashboardPage />} />
          <Route path="/inventory"         element={<InventoryPage />} />
          <Route path="/profile"           element={<ProfilePage />} />
          <Route path="/penalties"         element={<PenaltyPage />} />
          <Route path="/my-reservations"   element={<MyReservations />} />
          <Route path="/reservation-queue" element={<ReservationQueue />} />
          <Route path="/overdue-tracker"   element={<OverdueTracker />} />
          <Route path="/qr-scan"           element={<QRScanPage />} />
          <Route path="/payment/success"   element={<PaymentSuccessPage />} />
        </Route>
      </Routes>
    </GoogleOAuthProvider>
    </>
  )
}
