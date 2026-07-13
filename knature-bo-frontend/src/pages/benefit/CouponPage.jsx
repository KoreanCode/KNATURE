import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const fmt = (n) => Number(n).toLocaleString();
const GRADES = ['NEW', 'RUBY', 'SILVER', 'GOLD', 'DIAMOND', 'PLATINUM'];
const EMPTY = { name: '', code: '', discountType: 'FIXED', amount: '', maxDiscount: '', minOrderAmount: '', validUntil: '', birthdayCoupon: false };

export default function CouponPage() {
  const [coupons, setCoupons] = useState([]);
  const [form, setForm] = useState(null);
  const [issueFor, setIssueFor] = useState(null); // {couponId, name, target, memberId, grade}

  const load = () => api.get('/coupons').then((res) => setCoupons(res.data));
  useEffect(() => { load(); }, []);

  const set = (k) => (e) => setForm((p) => ({ ...p, [k]: e.target.value }));

  const save = () => {
    api.post('/coupons', { ...form, birthdayCoupon: form.birthdayCoupon ? 'true' : 'false' })
      .then(() => { alert('쿠폰이 생성되었습니다.'); setForm(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '생성에 실패했습니다.'));
  };

  const submitIssue = () => {
    api.post(`/coupons/${issueFor.couponId}/issue`, {
      target: issueFor.target, memberId: issueFor.memberId || '', grade: issueFor.grade || '',
    })
      .then((res) => { alert(res.data.message); setIssueFor(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '발급에 실패했습니다.'));
  };

  const discountLabel = (c) => c.discountType === 'PERCENT'
    ? `${c.amount}%${c.maxDiscount ? ` (최대 ${fmt(c.maxDiscount)}원)` : ''}`
    : `${fmt(c.amount)}원`;

  return (
    <>
      <TopBar title="쿠폰 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-end mb-2">
          <button className="btn btn-sm btn-primary" onClick={() => setForm({ ...EMPTY })}>+ 쿠폰 생성</button>
        </div>
        <table className="table table-hover">
          <thead className="table-light">
            <tr><th>쿠폰명</th><th>코드</th><th>할인</th><th className="text-end">최소주문</th><th>유효기간</th><th className="text-center">발급/사용</th><th className="text-center">관리</th></tr>
          </thead>
          <tbody>
            {coupons.map(({ coupon: c, issuedCount, usedCount }) => (
              <tr key={c.id}>
                <td>{c.name}{c.birthdayCoupon === true && <span className="badge bg-info ms-1">생일</span>}</td>
                <td>{c.code ? <code>{c.code}</code> : '-'}</td>
                <td>{discountLabel(c)}</td>
                <td className="text-end">{c.minOrderAmount > 0 ? fmt(c.minOrderAmount) + '원' : '-'}</td>
                <td>{c.validUntil || '무기한'}</td>
                <td className="text-center">{issuedCount} / <span className="text-primary">{usedCount}</span></td>
                <td className="text-center">
                  <button className="btn btn-sm btn-outline-primary py-0"
                    onClick={() => setIssueFor({ couponId: c.id, name: c.name, target: 'ALL', memberId: '', grade: 'NEW' })}>발급</button>
                </td>
              </tr>
            ))}
            {coupons.length === 0 && <tr><td colSpan={7} className="text-center text-muted py-4">등록된 쿠폰이 없습니다.</td></tr>}
          </tbody>
        </table>
        <small className="text-muted">코드 <code>WELCOME</code> 쿠폰은 회원가입 시 자동 발급됩니다.</small>
      </div>

      {form && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setForm(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">쿠폰 생성</h6>
                <button type="button" className="btn-close" onClick={() => setForm(null)} /></div>
              <div className="modal-body">
                <label className="form-label small mb-0">쿠폰명 *</label>
                <input className="form-control form-control-sm mb-2" value={form.name} maxLength={100} onChange={set('name')} />
                <label className="form-label small mb-0">코드 (자동발급용, 선택)</label>
                <input className="form-control form-control-sm mb-2" placeholder="예: BIRTHDAY" value={form.code} maxLength={50} onChange={set('code')} />
                <div className="d-flex gap-2 mb-2">
                  <div className="flex-fill">
                    <label className="form-label small mb-0">할인 유형 *</label>
                    <select className="form-select form-select-sm" value={form.discountType} onChange={set('discountType')}>
                      <option value="FIXED">정액 (원)</option>
                      <option value="PERCENT">정률 (%)</option>
                    </select>
                  </div>
                  <div className="flex-fill">
                    <label className="form-label small mb-0">{form.discountType === 'PERCENT' ? '할인율(%) *' : '할인액(원) *'}</label>
                    <input type="number" min="0" max={form.discountType === 'PERCENT' ? 100 : 99999999} className="form-control form-control-sm" value={form.amount} onChange={set('amount')} />
                  </div>
                </div>
                {form.discountType === 'PERCENT' && (
                  <>
                    <label className="form-label small mb-0">최대 할인액 (원, 선택)</label>
                    <input type="number" min="0" max={99999999} className="form-control form-control-sm mb-2" value={form.maxDiscount} onChange={set('maxDiscount')} />
                  </>
                )}
                <label className="form-label small mb-0">최소 주문금액 (원)</label>
                <input type="number" min="0" max={99999999} className="form-control form-control-sm mb-2" value={form.minOrderAmount} onChange={set('minOrderAmount')} />
                <label className="form-label small mb-0">유효기간 (까지, 비우면 무기한)</label>
                <input type="date" className="form-control form-control-sm" value={form.validUntil} onChange={set('validUntil')} />
                <div className="form-check mt-2">
                  <input className="form-check-input" type="checkbox" id="birthdayCoupon" checked={!!form.birthdayCoupon}
                    onChange={(e) => { const checked = e.target.checked; setForm((p) => ({ ...p, birthdayCoupon: checked })); }} />
                  <label className="form-check-label small" htmlFor="birthdayCoupon">생일 쿠폰 (회원 생일에 자동 발급)</label>
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setForm(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={save}>생성</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {issueFor && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setIssueFor(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">쿠폰 발급 — {issueFor.name}</h6>
                <button type="button" className="btn-close" onClick={() => setIssueFor(null)} /></div>
              <div className="modal-body">
                <label className="form-label small mb-0">발급 대상</label>
                <select className="form-select form-select-sm mb-2" value={issueFor.target}
                  onChange={(e) => setIssueFor({ ...issueFor, target: e.target.value })}>
                  <option value="ALL">전체 회원</option>
                  <option value="GRADE">특정 등급</option>
                  <option value="MEMBER">특정 회원</option>
                </select>
                {issueFor.target === 'GRADE' && (
                  <select className="form-select form-select-sm" value={issueFor.grade}
                    onChange={(e) => setIssueFor({ ...issueFor, grade: e.target.value })}>
                    {GRADES.map((g) => <option key={g} value={g}>{g}</option>)}
                  </select>
                )}
                {issueFor.target === 'MEMBER' && (
                  <input type="number" min="0" className="form-control form-control-sm" placeholder="회원 ID (고객관리에서 확인)"
                    value={issueFor.memberId} onChange={(e) => setIssueFor({ ...issueFor, memberId: e.target.value })} />
                )}
                <small className="text-muted d-block mt-2">이미 보유한 회원은 중복 발급되지 않습니다.</small>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setIssueFor(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={submitIssue}>발급하기</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
