import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import ProductListPage from './pages/product/ProductListPage'
import ProductFormPage from './pages/product/ProductFormPage'
import OrderListPage from './pages/order/OrderListPage'
import OrderDetailPage from './pages/order/OrderDetailPage'
import MemberListPage from './pages/member/MemberListPage'
import MemberDetailPage from './pages/member/MemberDetailPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/new" element={<ProductFormPage />} />
        <Route path="/products/:id/edit" element={<ProductFormPage />} />
        <Route path="/orders" element={<OrderListPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/members" element={<MemberListPage />} />
        <Route path="/members/:id" element={<MemberDetailPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
