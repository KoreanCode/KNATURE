import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import api from '../../api/client';

export default function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/auth/login', form);
      window.dispatchEvent(new Event('auth-changed'));
      navigate(searchParams.get('redirect') || '/');
    } catch {
      setError('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  return (
    <div className="container py-5" style={{ maxWidth: 420 }}>
      <h4 className="text-center fw-bold mb-4">로그인</h4>
      <form onSubmit={submit}>
        <input className="form-control mb-2" placeholder="아이디" maxLength={50} value={form.username}
          onChange={(e) => setForm((prev) => ({ ...prev, username: e.target.value }))} required />
        <input type="password" className="form-control mb-3" placeholder="비밀번호" maxLength={64} value={form.password}
          onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} required />
        {error && <div className="alert alert-danger py-2 small">{error}</div>}
        <button className="btn btn-brand w-100 py-2">로그인</button>
      </form>
      <div className="d-flex justify-content-center gap-3 mt-3 small">
        <Link to="/member/join" className="text-muted">회원가입</Link>
        <span className="text-muted">|</span>
        <Link to="/member/find" className="text-muted">아이디/비밀번호 찾기</Link>
        <span className="text-muted">|</span>
        <Link to="/order/guest" className="text-muted">비회원 주문 조회</Link>
      </div>
    </div>
  );
}
