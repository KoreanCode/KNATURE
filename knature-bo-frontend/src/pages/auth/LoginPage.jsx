import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/client';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/auth/login', { username, password });
      // 공장관리자는 대시보드 권한이 없으므로 발주 관리로 진입
      navigate(res.data.role === 'FACTORY_ADMIN' ? '/purchase-orders' : '/dashboard');
    } catch {
      setError('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2>KNATURE</h2>
        <p className="sub">관리자 로그인</p>
        {error && <div className="alert alert-danger py-2">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">아이디</label>
            <input type="text" className="form-control" value={username} maxLength={50} onChange={(e) => setUsername(e.target.value)} required autoFocus />
          </div>
          <div className="mb-3">
            <label className="form-label">비밀번호</label>
            <input type="password" className="form-control" value={password} maxLength={64} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary w-100">로그인</button>
        </form>
      </div>
    </div>
  );
}
