import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import ProductListPage from './pages/product/ProductListPage'
import ProductFormPage from './pages/product/ProductFormPage'
import CategoryManagePage from './pages/product/CategoryManagePage'
import OrderListPage from './pages/order/OrderListPage'
import OrderDetailPage from './pages/order/OrderDetailPage'
import MemberListPage from './pages/member/MemberListPage'
import MemberDetailPage from './pages/member/MemberDetailPage'
import StockListPage from './pages/stock/StockListPage'
import SettingsPage from './pages/setting/SettingsPage'
import MileagePage from './pages/benefit/MileagePage'
import CouponPage from './pages/benefit/CouponPage'
import BoardPage from './pages/board/BoardPage'
import CsPage from './pages/board/CsPage'
import FactoryPage from './pages/scm/FactoryPage'
import PurchaseOrderPage from './pages/scm/PurchaseOrderPage'
import FactoryStockPage from './pages/scm/FactoryStockPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/new" element={<ProductFormPage />} />
        <Route path="/products/:id/edit" element={<ProductFormPage />} />
        <Route path="/products/categories-manage" element={<CategoryManagePage />} />
        <Route path="/orders" element={<OrderListPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/members" element={<MemberListPage />} />
        <Route path="/members/:id" element={<MemberDetailPage />} />
        <Route path="/stocks" element={<StockListPage />} />
        <Route path="/mileages" element={<MileagePage />} />
        <Route path="/coupons" element={<CouponPage />} />
        <Route path="/boards" element={<BoardPage />} />
        <Route path="/cs" element={<CsPage />} />
        <Route path="/factories" element={<FactoryPage />} />
        <Route path="/purchase-orders" element={<PurchaseOrderPage />} />
        <Route path="/factory-stocks" element={<FactoryStockPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
