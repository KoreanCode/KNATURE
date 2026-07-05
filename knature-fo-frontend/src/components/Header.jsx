import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/client';
import { cartCount } from '../utils/cart';

export default function Header() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [count, setCount] = useState(cartCount());
  const [keyword, setKeyword] = useState('');

  useEffect(() => {
    api.get('/auth/me').then((res) => setUser(res.data)).catch(() => setUser(null));
    const onChange = () => setCount(cartCount());
    window.addEventListener('cart-changed', onChange);
    window.addEventListener('auth-changed', refreshUser);
    return () => {
      window.removeEventListener('cart-changed', onChange);
      window.removeEventListener('auth-changed', refreshUser);
    };
  }, []);

  const refreshUser = () => {
    api.get('/auth/me').then((res) => setUser(res.data)).catch(() => setUser(null));
  };

  const logout = async () => {
    await api.post('/auth/logout');
    setUser(null);
    navigate('/');
  };

  const search = (e) => {
    e.preventDefault();
    if (keyword.trim()) navigate(`/products?keyword=${encodeURIComponent(keyword.trim())}`);
  };

  return (
    <header className="fo-header">
      <div className="container py-3 d-flex align-items-center justify-content-between flex-wrap gap-2">
        <Link to="/" className="fo-logo">KNATURE</Link>

        <nav className="fo-gnb d-none d-md-flex">
          <Link to="/products">PRODUCT</Link>
          <Link to="/page/guide">GUIDE</Link>
          <Link to="/page/agreement">POLICY</Link>
        </nav>

        <div className="d-flex align-items-center gap-3">
          <form onSubmit={search} className="d-none d-md-flex">
            <input className="form-control form-control-sm" style={{ width: 160 }} placeholder="상품 검색"
              value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          </form>

          {user ? (
            <>
              <Link to="/myshop" className="small text-muted"><i className="bi bi-person"></i> {user.name}님</Link>
              <button className="btn btn-link btn-sm text-muted p-0" onClick={logout}>로그아웃</button>
            </>
          ) : (
            <>
              <Link to="/member/login" className="small text-muted">로그인</Link>
              <Link to="/member/join" className="small text-muted">회원가입</Link>
            </>
          )}

          <Link to="/cart" className="position-relative fs-5 text-dark">
            <i className="bi bi-bag"></i>
            {count > 0 && <span className="cart-badge">{count}</span>}
          </Link>
        </div>
      </div>
    </header>
  );
}
