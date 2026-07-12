import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import api from '../api/client';

// roles: 메뉴 접근 가능 역할 (미지정 = SUPER_ADMIN 전용)
const MENUS = [
  { path: '/dashboard', icon: 'bi-speedometer2', label: '대시보드' },
  { path: '/orders', icon: 'bi-cart-check', label: '주문 관리' },
  { path: '/products', icon: 'bi-box-seam', label: '상품 관리' },
  { path: '/members', icon: 'bi-people', label: '고객 관리' },
  { path: '/stocks', icon: 'bi-clipboard-data', label: '재고 관리' },
  { path: '/factories', icon: 'bi-buildings', label: '공장 관리' },
  { path: '/purchase-orders', icon: 'bi-truck', label: '발주 관리', roles: ['SUPER_ADMIN', 'FACTORY_ADMIN'] },
  { path: '/factory-stocks', icon: 'bi-box2', label: '공장 재고', roles: ['SUPER_ADMIN', 'FACTORY_ADMIN'] },
  { path: '/mileages', icon: 'bi-coin', label: '적립금 관리' },
  { path: '/coupons', icon: 'bi-ticket-perforated', label: '쿠폰 관리' },
  { path: '/boards', icon: 'bi-megaphone', label: '게시판 관리' },
  { path: '/cs', icon: 'bi-chat-left-text', label: '문의/후기' },
  { path: '/settings', icon: 'bi-gear', label: '설정' },
];

export default function Sidebar() {
  const [role, setRole] = useState('SUPER_ADMIN');

  useEffect(() => {
    api.get('/auth/me').then((res) => setRole(res.data.role)).catch(() => {});
  }, []);

  const visible = MENUS.filter((m) => (m.roles || ['SUPER_ADMIN']).includes(role));

  return (
    <nav className="sidebar">
      <div className="logo">KNATURE Admin</div>
      <ul className="nav flex-column mt-2" style={{ listStyle: 'none', padding: 0 }}>
        {visible.map((m) => (
          <li key={m.path}>
            <NavLink to={m.path} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
              <i className={`bi ${m.icon}`}></i> {m.label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
