import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/client';
import MyLayout from './MyLayout';
import { openPostcode } from '../../utils/postcode';

export default function MyInfoPage() {
  const navigate = useNavigate();
  const [verified, setVerified] = useState(false);
  const [password, setPassword] = useState('');
  const [info, setInfo] = useState(null);
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirm: '' });
  const [withdrawPw, setWithdrawPw] = useState('');

  const verify = async (e) => {
    e.preventDefault();
    try {
      await api.post('/mypage/verify-password', { password });
      const res = await api.get('/mypage/info');
      setInfo(res.data);
      setVerified(true);
    } catch (err) {
      alert(err.response?.data?.message || '비밀번호가 일치하지 않습니다.');
    }
  };

  const saveInfo = async () => {
    await api.put('/mypage/info', {
      email: info.email, phone: info.phone || '',
      zipcode: info.zipcode || '', address: info.address || '', addressDetail: info.addressDetail || '',
    });
    alert('회원 정보가 수정되었습니다.');
  };

  const changePw = async () => {
    if (pwForm.newPassword !== pwForm.confirm) { alert('새 비밀번호가 일치하지 않습니다.'); return; }
    try {
      await api.put('/mypage/password', pwForm);
      alert('비밀번호가 변경되었습니다.');
      setPwForm({ currentPassword: '', newPassword: '', confirm: '' });
    } catch (err) {
      alert(err.response?.data?.message || '변경에 실패했습니다.');
    }
  };

  const withdraw = async () => {
    if (!confirm('정말 탈퇴하시겠습니까? 탈퇴 후 복구할 수 없습니다.')) return;
    try {
      const res = await api.post('/mypage/withdraw', { password: withdrawPw });
      alert(res.data.message);
      window.dispatchEvent(new Event('auth-changed'));
      navigate('/');
    } catch (err) {
      alert(err.response?.data?.message || '탈퇴에 실패했습니다.');
    }
  };

  const set = (k) => (e) => setInfo((prev) => ({ ...prev, [k]: e.target.value }));

  return (
    <MyLayout title="회원정보 수정">
      {!verified ? (
        <form onSubmit={verify} style={{ maxWidth: 360 }}>
          <p className="small text-muted">회원 정보 보호를 위해 비밀번호를 다시 입력해주세요.</p>
          <input type="password" className="form-control mb-2" placeholder="비밀번호" value={password}
            onChange={(e) => setPassword(e.target.value)} required />
          <button className="btn btn-brand">확인</button>
        </form>
      ) : (
        <>
          <div className="border rounded p-3 mb-3" style={{ maxWidth: 520 }}>
            <b className="d-block mb-2">기본 정보</b>
            <label className="form-label small mb-0 mt-2">아이디</label>
            <input className="form-control form-control-sm" value={info.username} disabled readOnly />
            <label className="form-label small mb-0 mt-2">이메일</label>
            <input className="form-control form-control-sm" value={info.email || ''} onChange={set('email')} />
            <label className="form-label small mb-0 mt-2">휴대폰</label>
            <input className="form-control form-control-sm" value={info.phone || ''} onChange={set('phone')} />
            <label className="form-label small mb-0 mt-2">주소</label>
            <div className="d-flex gap-2 mb-1">
              <input className="form-control form-control-sm" style={{ maxWidth: 120 }} placeholder="우편번호" value={info.zipcode || ''} readOnly onChange={set('zipcode')} />
              <input className="form-control form-control-sm" placeholder="주소" value={info.address || ''} readOnly onChange={set('address')} />
              <button type="button" className="btn btn-sm btn-outline-brand flex-shrink-0"
                onClick={() => openPostcode(({ zipcode, address }) => setInfo((p) => ({ ...p, zipcode, address })))}>주소 검색</button>
            </div>
            <input className="form-control form-control-sm" placeholder="상세주소" value={info.addressDetail || ''} onChange={set('addressDetail')} />
            <button className="btn btn-sm btn-brand mt-3" onClick={saveInfo}>정보 저장</button>
          </div>

          <div className="border rounded p-3 mb-3" style={{ maxWidth: 520 }}>
            <b className="d-block mb-2">비밀번호 변경</b>
            <input type="password" className="form-control form-control-sm mb-1" placeholder="현재 비밀번호"
              value={pwForm.currentPassword} onChange={(e) => setPwForm((p) => ({ ...p, currentPassword: e.target.value }))} />
            <input type="password" className="form-control form-control-sm mb-1" placeholder="새 비밀번호 (8자 이상)"
              value={pwForm.newPassword} onChange={(e) => setPwForm((p) => ({ ...p, newPassword: e.target.value }))} />
            <input type="password" className="form-control form-control-sm mb-2" placeholder="새 비밀번호 확인"
              value={pwForm.confirm} onChange={(e) => setPwForm((p) => ({ ...p, confirm: e.target.value }))} />
            <button className="btn btn-sm btn-outline-brand" onClick={changePw}>비밀번호 변경</button>
          </div>

          <div className="border border-danger-subtle rounded p-3" style={{ maxWidth: 520 }}>
            <b className="d-block mb-2 text-danger">회원 탈퇴</b>
            <p className="small text-muted">탈퇴 시 로그인이 불가하며, 주문 이력은 법령에 따라 보존됩니다.</p>
            <div className="d-flex gap-2">
              <input type="password" className="form-control form-control-sm" style={{ maxWidth: 200 }}
                placeholder="비밀번호 확인" value={withdrawPw} onChange={(e) => setWithdrawPw(e.target.value)} />
              <button className="btn btn-sm btn-outline-danger" onClick={withdraw}>탈퇴하기</button>
            </div>
          </div>
        </>
      )}
    </MyLayout>
  );
}
