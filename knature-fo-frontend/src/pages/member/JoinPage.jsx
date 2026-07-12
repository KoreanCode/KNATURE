import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/client';
import { openPostcode } from '../../utils/postcode';

// 약관 동의 → 정보 입력 → 가입 완료 (3단계)
export default function JoinPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [agree, setAgree] = useState({ terms: false, privacy: false });
  const [form, setForm] = useState({ username: '', password: '', passwordConfirm: '', name: '', email: '', phone: '', zipcode: '', address: '', addressDetail: '' });
  const [error, setError] = useState('');
  const [doneName, setDoneName] = useState('');

  // 함수형 업데이트 — 자동완성 등 연속 입력 시 stale closure로 값이 소실되는 문제 방지
  const set = (k) => (e) => setForm((prev) => ({ ...prev, [k]: e.target.value }));

  const goStep2 = () => {
    if (!agree.terms || !agree.privacy) { setError('필수 약관에 모두 동의해주세요.'); return; }
    setError(''); setStep(2);
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.passwordConfirm) { setError('비밀번호가 일치하지 않습니다.'); return; }
    try {
      const res = await api.post('/auth/signup', form);
      setDoneName(res.data.name);
      setStep(3);
    } catch (err) {
      setError(err.response?.data?.message || '가입에 실패했습니다.');
    }
  };

  return (
    <div className="container py-5" style={{ maxWidth: 520 }}>
      <h4 className="text-center fw-bold mb-2">회원가입</h4>
      <div className="text-center small text-muted mb-4">
        {[1, 2, 3].map((s) => (
          <span key={s} className={step === s ? 'text-brand fw-bold' : ''}>
            {s === 1 ? '① 약관동의' : s === 2 ? '② 정보입력' : '③ 가입완료'}{s < 3 && ' > '}
          </span>
        ))}
      </div>

      {step === 1 && (
        <div>
          <div className="border rounded p-3 mb-2" style={{ maxHeight: 150, overflowY: 'auto' }}>
            <b>이용약관 (필수)</b>
            <p className="small text-muted mb-0">제1조(목적) 본 약관은 KNATURE(아람티앤씨)가 운영하는 쇼핑몰에서 제공하는 서비스의 이용조건 및 절차, 회사와 회원 간의 권리·의무 및 책임사항을 규정함을 목적으로 합니다. ...</p>
          </div>
          <div className="form-check mb-3">
            <input type="checkbox" className="form-check-input" id="terms" checked={agree.terms}
              onChange={(e) => setAgree({ ...agree, terms: e.target.checked })} />
            <label className="form-check-label small" htmlFor="terms">이용약관에 동의합니다. (필수)</label>
          </div>
          <div className="border rounded p-3 mb-2" style={{ maxHeight: 150, overflowY: 'auto' }}>
            <b>개인정보 수집·이용 동의 (필수)</b>
            <p className="small text-muted mb-0">수집 항목: 아이디, 비밀번호, 이름, 이메일, 휴대폰번호, 주소 / 수집 목적: 회원 관리, 주문·배송 / 보유 기간: 회원 탈퇴 시까지 ...</p>
          </div>
          <div className="form-check mb-3">
            <input type="checkbox" className="form-check-input" id="privacy" checked={agree.privacy}
              onChange={(e) => setAgree({ ...agree, privacy: e.target.checked })} />
            <label className="form-check-label small" htmlFor="privacy">개인정보 수집·이용에 동의합니다. (필수)</label>
          </div>
          {error && <div className="alert alert-danger py-2 small">{error}</div>}
          <button className="btn btn-brand w-100 py-2" onClick={goStep2}>동의하고 다음</button>
        </div>
      )}

      {step === 2 && (
        <form onSubmit={submit}>
          <input className="form-control mb-2" placeholder="아이디 (4~20자) *" maxLength={20} value={form.username} onChange={set('username')} required />
          <input type="password" className="form-control mb-2" placeholder="비밀번호 (8자 이상) *" maxLength={64} value={form.password} onChange={set('password')} required />
          <input type="password" className="form-control mb-2" placeholder="비밀번호 확인 *" maxLength={64} value={form.passwordConfirm} onChange={set('passwordConfirm')} required />
          <input className="form-control mb-2" placeholder="이름 *" maxLength={50} value={form.name} onChange={set('name')} required />
          <input type="email" className="form-control mb-2" placeholder="이메일 *" maxLength={100} value={form.email} onChange={set('email')} required />
          <input className="form-control mb-2" placeholder="휴대폰 (예: 010-1234-5678)" maxLength={20} value={form.phone} onChange={set('phone')} />
          <div className="d-flex gap-2 mb-2">
            <input className="form-control" style={{ maxWidth: 140 }} placeholder="우편번호" value={form.zipcode} readOnly onChange={set('zipcode')} />
            <input className="form-control" placeholder="주소" value={form.address} readOnly onChange={set('address')} />
            <button type="button" className="btn btn-outline-brand flex-shrink-0"
              onClick={() => openPostcode(({ zipcode, address }) => setForm((p) => ({ ...p, zipcode, address })))}>주소 검색</button>
          </div>
          <input className="form-control mb-3" placeholder="상세주소" maxLength={100} value={form.addressDetail} onChange={set('addressDetail')} />
          {error && <div className="alert alert-danger py-2 small">{error}</div>}
          <button className="btn btn-brand w-100 py-2">가입하기</button>
        </form>
      )}

      {step === 3 && (
        <div className="text-center py-4">
          <i className="bi bi-check-circle text-brand" style={{ fontSize: '3rem' }}></i>
          <h5 className="mt-3">{doneName}님, 가입을 환영합니다!</h5>
          <p className="text-muted small">로그인 후 KNATURE의 모든 서비스를 이용하실 수 있습니다.</p>
          <button className="btn btn-brand px-5" onClick={() => navigate('/member/login')}>로그인 하러가기</button>
        </div>
      )}

      {step < 3 && (
        <div className="text-center mt-3 small">
          <span className="text-muted">이미 회원이신가요? </span><Link to="/member/login" className="text-brand">로그인</Link>
        </div>
      )}
    </div>
  );
}
