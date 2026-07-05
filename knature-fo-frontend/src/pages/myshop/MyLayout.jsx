import { useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import api from '../../api/client';

/** 마이페이지 공통 레이아웃 — 로그인 가드 + 사이드 메뉴 */
export default function MyLayout({ title, children }) {
  const navigate = useNavigate();
  const [me, setMe] = useState(null);

  useEffect(() => {
    api.get('/auth/me')
      .then((res) => setMe(res.data))
      .catch(() => navigate('/member/login?redirect=' + encodeURIComponent(location.pathname)));
  }, []);

  if (!me) return <div className="container py-5 text-center">로딩중...</div>;

  return (
    <div className="container py-4">
      <h4 className="fw-bold mb-4">마이쇼핑 <small className="text-muted fs-6">{me.name}님 ({me.gradeLabel})</small></h4>
      <div className="row g-4">
        <div className="col-md-3">
          <nav className="my-nav">
            <NavLink to="/myshop" end>마이쇼핑 홈</NavLink>
            <NavLink to="/myshop/orders">주문 조회</NavLink>
            <NavLink to="/cart">장바구니</NavLink>
            <NavLink to="/myshop/address">배송지 관리</NavLink>
            <NavLink to="/myshop/info">회원정보 수정</NavLink>
          </nav>
        </div>
        <div className="col-md-9">
          {title && <h5 className="fw-bold mb-3">{title}</h5>}
          {children}
        </div>
      </div>
    </div>
  );
}
