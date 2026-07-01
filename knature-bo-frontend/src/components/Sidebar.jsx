import { NavLink } from 'react-router-dom';

const menus = [
  { path: '/dashboard', icon: 'bi-speedometer2', label: '대시보드' },
  { path: '/orders', icon: 'bi-cart-check', label: '주문 관리' },
  { path: '/products', icon: 'bi-box-seam', label: '상품 관리' },
  { path: '/members', icon: 'bi-people', label: '고객 관리' },
];

export default function Sidebar() {
  return (
    <nav className="sidebar">
      <div className="logo">KNATURE Admin</div>
      <ul className="nav flex-column mt-2" style={{ listStyle: 'none', padding: 0 }}>
        {menus.map((m) => (
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
