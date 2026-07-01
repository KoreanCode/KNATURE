import api from '../api/client';
import { useNavigate } from 'react-router-dom';

export default function TopBar({ title, user }) {
  const navigate = useNavigate();

  const handleLogout = async () => {
    await api.post('/auth/logout');
    navigate('/login');
  };

  return (
    <div className="top-bar">
      <h5 style={{ marginBottom: 0 }}>{title}</h5>
      <div>
        <span style={{ marginRight: 12 }}>{user?.name || '관리자'}님</span>
        <button className="btn btn-outline-secondary btn-sm" onClick={handleLogout}>로그아웃</button>
      </div>
    </div>
  );
}
