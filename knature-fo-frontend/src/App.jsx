import { Routes, Route, Navigate } from 'react-router-dom'
import Header from './components/Header'
import Footer from './components/Footer'
import MainPage from './pages/MainPage'
import LoginPage from './pages/member/LoginPage'
import JoinPage from './pages/member/JoinPage'
import FindAccountPage from './pages/member/FindAccountPage'
import ProductListPage from './pages/product/ProductListPage'
import ProductDetailPage from './pages/product/ProductDetailPage'
import CartPage from './pages/order/CartPage'
import OrderFormPage from './pages/order/OrderFormPage'
import OrderResultPage from './pages/order/OrderResultPage'
import GuestOrderPage from './pages/order/GuestOrderPage'
import MyShopPage from './pages/myshop/MyShopPage'
import MyOrdersPage from './pages/myshop/MyOrdersPage'
import MyOrderDetailPage from './pages/myshop/MyOrderDetailPage'
import MyInfoPage from './pages/myshop/MyInfoPage'
import MyAddressPage from './pages/myshop/MyAddressPage'
import MyMileagePage from './pages/myshop/MyMileagePage'
import MyDepositPage from './pages/myshop/MyDepositPage'
import MyWishlistPage from './pages/myshop/MyWishlistPage'
import MyCouponPage from './pages/myshop/MyCouponPage'
import MyInquiryPage from './pages/myshop/MyInquiryPage'
import CommunityPage from './pages/community/CommunityPage'
import StaticPage from './pages/static/StaticPage'

export default function App() {
  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="flex-grow-1">
        <Routes>
          <Route path="/" element={<MainPage />} />
          <Route path="/member/login" element={<LoginPage />} />
          <Route path="/member/join" element={<JoinPage />} />
          <Route path="/member/find" element={<FindAccountPage />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/order" element={<OrderFormPage />} />
          <Route path="/order/result" element={<OrderResultPage />} />
          <Route path="/order/guest" element={<GuestOrderPage />} />
          <Route path="/myshop" element={<MyShopPage />} />
          <Route path="/myshop/orders" element={<MyOrdersPage />} />
          <Route path="/myshop/orders/:id" element={<MyOrderDetailPage />} />
          <Route path="/myshop/info" element={<MyInfoPage />} />
          <Route path="/myshop/address" element={<MyAddressPage />} />
          <Route path="/myshop/mileage" element={<MyMileagePage />} />
          <Route path="/myshop/deposit" element={<MyDepositPage />} />
          <Route path="/myshop/wishlist" element={<MyWishlistPage />} />
          <Route path="/myshop/coupon" element={<MyCouponPage />} />
          <Route path="/myshop/inquiry" element={<MyInquiryPage />} />
          <Route path="/community" element={<CommunityPage />} />
          <Route path="/page/:slug" element={<StaticPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}
