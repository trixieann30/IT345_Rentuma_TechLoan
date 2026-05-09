import { Routes, Route, Navigate } from 'react-router-dom'
import { GoogleOAuthProvider } from '@react-oauth/google'
import LoginPage          from './features/auth/LoginPage'
import RegisterPage       from './features/auth/RegisterPage'
import DashboardPage      from './features/dashboard/DashboardPage'
import InventoryPage      from './features/inventory/InventoryPage'
import ProfilePage        from './features/profile/ProfilePage'
import PenaltyPage        from './features/penalty/PenaltyPage'
import MyReservations     from './features/reservation/MyReservations'
import ReservationQueue   from './features/reservation/ReservationQueue'
import OverdueTracker     from './features/reservation/OverdueTracker'
import QRScanPage         from './features/reservation/QRScanPage'

// Simple auth guard
function PrivateRoute({ children }) {
  const token = localStorage.getItem('token')
  return token ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <Routes>
        <Route path="/"           element={<Navigate to="/login" replace />} />
        <Route path="/login"      element={<LoginPage />} />
        <Route path="/register"   element={<RegisterPage />} />
        <Route path="/dashboard"  element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        } />
        <Route path="/inventory"  element={
          <PrivateRoute>
            <InventoryPage />
          </PrivateRoute>
        } />
        <Route path="/profile"    element={
          <PrivateRoute>
            <ProfilePage />
          </PrivateRoute>
        } />
        <Route path="/penalties"  element={
          <PrivateRoute>
            <PenaltyPage />
          </PrivateRoute>
        } />
        <Route path="/my-reservations" element={
          <PrivateRoute>
            <MyReservations />
          </PrivateRoute>
        } />
        <Route path="/reservation-queue" element={
          <PrivateRoute>
            <ReservationQueue />
          </PrivateRoute>
        } />
        <Route path="/overdue-tracker" element={
          <PrivateRoute>
            <OverdueTracker />
          </PrivateRoute>
        } />
        <Route path="/qr-scan" element={
          <PrivateRoute>
            <QRScanPage />
          </PrivateRoute>
        } />
      </Routes>
    </GoogleOAuthProvider>
  )
}
