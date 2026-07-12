import { useState } from 'react';
import api from '../../api/client';

export default function FindAccountPage() {
  const [tab, setTab] = useState('id');
  const [idForm, setIdForm] = useState({ name: '', email: '' });
  const [pwForm, setPwForm] = useState({ username: '', email: '' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const findId = async (e) => {
    e.preventDefault(); setError(''); setResult(null);
    try {
      const res = await api.post('/auth/find-id', idForm);
      setResult({ type: 'id', value: res.data.username });
    } catch (err) {
      setError(err.response?.data?.message || '일치하는 회원 정보가 없습니다.');
    }
  };

  const resetPw = async (e) => {
    e.preventDefault(); setError(''); setResult(null);
    try {
      const res = await api.post('/auth/reset-password', pwForm);
      setResult({ type: 'pw', value: res.data.tempPassword });
    } catch (err) {
      setError(err.response?.data?.message || '일치하는 회원 정보가 없습니다.');
    }
  };

  return (
    <div className="container py-5" style={{ maxWidth: 420 }}>
      <h4 className="text-center fw-bold mb-4">아이디 / 비밀번호 찾기</h4>
      <ul className="nav nav-tabs mb-3">
        <li className="nav-item"><button className={`nav-link ${tab === 'id' ? 'active' : ''}`} onClick={() => { setTab('id'); setResult(null); setError(''); }}>아이디 찾기</button></li>
        <li className="nav-item"><button className={`nav-link ${tab === 'pw' ? 'active' : ''}`} onClick={() => { setTab('pw'); setResult(null); setError(''); }}>비밀번호 찾기</button></li>
      </ul>

      {tab === 'id' ? (
        <form onSubmit={findId}>
          <input className="form-control mb-2" placeholder="이름" maxLength={50} value={idForm.name}
            onChange={(e) => setIdForm((p) => ({ ...p, name: e.target.value }))} required />
          <input type="email" className="form-control mb-3" placeholder="이메일" maxLength={100} value={idForm.email}
            onChange={(e) => setIdForm((p) => ({ ...p, email: e.target.value }))} required />
          <button className="btn btn-brand w-100">아이디 찾기</button>
        </form>
      ) : (
        <form onSubmit={resetPw}>
          <input className="form-control mb-2" placeholder="아이디" maxLength={50} value={pwForm.username}
            onChange={(e) => setPwForm((p) => ({ ...p, username: e.target.value }))} required />
          <input type="email" className="form-control mb-3" placeholder="이메일" maxLength={100} value={pwForm.email}
            onChange={(e) => setPwForm((p) => ({ ...p, email: e.target.value }))} required />
          <button className="btn btn-brand w-100">임시 비밀번호 발급</button>
        </form>
      )}

      {error && <div className="alert alert-danger py-2 small mt-3">{error}</div>}
      {result && (
        <div className="alert alert-success mt-3">
          {result.type === 'id'
            ? <>회원님의 아이디는 <b>{result.value}</b> 입니다.</>
            : <>임시 비밀번호: <b>{result.value}</b><br /><small>로그인 후 마이페이지에서 반드시 비밀번호를 변경해주세요.</small></>}
        </div>
      )}
    </div>
  );
}
