import { Routes, Route } from 'react-router-dom'
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
import MyShopPage from './pages/myshop/MyShopPage'
import MyOrdersPage from './pages/myshop/MyOrdersPage'
import MyOrderDetailPage from './pages/myshop/MyOrderDetailPage'
import MyInfoPage from './pages/myshop/MyInfoPage'
import MyAddressPage from './pages/myshop/MyAddressPage'
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
          <Route path="/myshop" element={<MyShopPage />} />
          <Route path="/myshop/orders" element={<MyOrdersPage />} />
          <Route path="/myshop/orders/:id" element={<MyOrderDetailPage />} />
          <Route path="/myshop/info" element={<MyInfoPage />} />
          <Route path="/myshop/address" element={<MyAddressPage />} />
          <Route path="/page/:slug" element={<StaticPage />} />
          <Route path="*" element={<MainPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}
