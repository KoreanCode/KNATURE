import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import api from '../../api/client';
import { removeItems } from '../../utils/cart';
import { openPostcode } from '../../utils/postcode';

const fmt = (n) => Number(n).toLocaleString();
const PAY_METHODS = [
  ['BANK_TRANSFER', '무통장입금'], ['CREDIT_CARD', '신용카드'], ['KAKAO_PAY', '카카오페이'], ['NAVER_PAY', '네이버페이'],
];

export default function OrderFormPage() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const items = state?.items || [];

  const [me, setMe] = useState(null);
  const [authChecked, setAuthChecked] = useState(false); // 비로그인(비회원 주문) 여부 판별 완료
  const [addresses, setAddresses] = useState([]);
  const [shopInfo, setShopInfo] = useState({});
  const [form, setForm] = useState({ receiverName: '', receiverPhone: '', zipcode: '', address: '', addressDetail: '', deliveryMemo: '' });
  const [guest, setGuest] = useState({ guestName: '', guestEmail: '', guestPhone: '', guestPassword: '' });
  const [payMethod, setPayMethod] = useState('BANK_TRANSFER');
  const [submitting, setSubmitting] = useState(false);
  // 적립금/쿠폰/예치금 (2차)
  const [mileageBalance, setMileageBalance] = useState(0);
  const [useMileage, setUseMileage] = useState(0);
  const [depositBalance, setDepositBalance] = useState(0);
  const [useDeposit, setUseDeposit] = useState(0);
  const [myCoupons, setMyCoupons] = useState([]);
  const [couponId, setCouponId] = useState('');

  useEffect(() => {
    if (items.length === 0) { navigate('/cart'); return; }
    api.get('/auth/me')
      .then((res) => {
        setMe(res.data);
        // 회원 전용 데이터
        api.get('/addresses').then((r) => {
          setAddresses(r.data);
          const def = r.data.find((a) => a.isDefault);
          if (def) applyAddress(def);
        }).catch(() => {});
        api.get('/mypage/mileage').then((r) => setMileageBalance(r.data.balance)).catch(() => {});
        api.get('/mypage/deposit').then((r) => setDepositBalance(r.data.balance)).catch(() => {});
        api.get('/mypage/coupons').then((r) => setMyCoupons(r.data.filter((mc) => !mc.used))).catch(() => {});
      })
      .catch(() => setMe(null)) // 비로그인 → 비회원 주문 모드
      .finally(() => setAuthChecked(true));
    api.get('/shop-info').then((res) => setShopInfo(res.data)).catch(() => {});
  }, []);

  const applyAddress = (a) => setForm((f) => ({
    ...f, receiverName: a.receiverName, receiverPhone: a.receiverPhone,
    zipcode: a.zipcode, address: a.address, addressDetail: a.addressDetail || '',
  }));

  const set = (k) => (e) => setForm((prev) => ({ ...prev, [k]: e.target.value }));
  const setGuestField = (k) => (e) => setGuest((prev) => ({ ...prev, [k]: e.target.value }));

  // 등급 추가 할인 — 서버 공식과 동일: 항목 단가(기본가+옵션추가금)에 할인 적용 후 원 단위 내림 × 수량
  const gradeRate = me ? (parseInt(shopInfo['gradeDiscount.' + me.grade]) || 0) : 0;
  const effectiveUnitPrice = (it) => (gradeRate > 0 ? Math.floor(it.unitPrice * (100 - gradeRate) / 100) : it.unitPrice);
  const totalAmount = items.reduce((sum, it) => sum + effectiveUnitPrice(it) * it.quantity, 0);
  const baseFee = parseInt(shopInfo['delivery.baseFee'] || '0') || 0;
  const freeThreshold = parseInt(shopInfo['delivery.freeThreshold'] || '0') || 0;
  const deliveryFee = freeThreshold > 0 && totalAmount >= freeThreshold ? 0 : baseFee;

  // 쿠폰 할인 미리보기 (서버에서 최종 재검증)
  const selectedCoupon = myCoupons.find((mc) => String(mc.id) === couponId);
  const couponDiscount = (() => {
    if (!selectedCoupon) return 0;
    const c = selectedCoupon.coupon;
    if (totalAmount < c.minOrderAmount) return 0;
    let d = c.discountType === 'PERCENT' ? Math.floor(totalAmount * c.amount / 100) : c.amount;
    if (c.maxDiscount && d > c.maxDiscount) d = c.maxDiscount;
    return Math.min(d, totalAmount);
  })();
  // 차감 순서: 쿠폰 → 적립금 → 예치금
  const payable = totalAmount + deliveryFee - couponDiscount;
  const mileageApplied = me ? Math.min(useMileage || 0, mileageBalance, payable) : 0;
  const afterMileage = payable - mileageApplied;
  const depositApplied = me ? Math.min(useDeposit || 0, depositBalance, afterMileage) : 0;
  const finalAmount = afterMileage - depositApplied;

  const setMileageInput = (v) => {
    const n = Math.max(0, parseInt(v) || 0);
    setUseMileage(Math.min(n, mileageBalance, payable));
  };

  const setDepositInput = (v) => {
    const n = Math.max(0, parseInt(v) || 0);
    setUseDeposit(Math.min(n, depositBalance, afterMileage));
  };

  const submit = async () => {
    if (!form.receiverName || !form.address) { alert('배송지 정보를 입력해주세요.'); return; }
    if (!me) {
      if (!guest.guestName.trim()) { alert('주문자 이름을 입력해주세요.'); return; }
      if (!guest.guestPassword || guest.guestPassword.length < 4) { alert('주문 조회 비밀번호를 4자 이상 입력해주세요.'); return; }
    }
    setSubmitting(true);
    try {
      const body = {
        items: items.map((it) => ({ productId: it.productId, optionId: it.optionId, quantity: it.quantity })),
        paymentMethod: payMethod,
        ...form,
      };
      if (me) {
        body.useMileage = mileageApplied;
        body.useDeposit = depositApplied;
        body.memberCouponId = selectedCoupon ? selectedCoupon.id : null;
      } else {
        body.useMileage = 0;
        body.useDeposit = 0;
        body.guestName = guest.guestName;
        body.guestEmail = guest.guestEmail;
        body.guestPhone = guest.guestPhone;
        body.guestPassword = guest.guestPassword;
      }
      const res = await api.post('/orders', body);
      // 주문된 상품은 장바구니에서 제거
      removeItems(items.map((it) => ({ productId: it.productId, optionId: it.optionId })));
      navigate('/order/result', {
        state: {
          ...res.data,
          receiver: form,
          bank: shopInfo['shop.bank'] || '주문 완료 후 고객센터로 문의해주세요',
        },
      });
    } catch (err) {
      alert(err.response?.data?.message || '주문에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!authChecked) return <div className="container py-5 text-center">로딩중...</div>;

  return (
    <div className="container py-4" style={{ maxWidth: 900 }}>
      <h4 className="fw-bold text-center mb-4">주문 / 결제</h4>

      {/* 주문 상품 */}
      <div className="border rounded p-3 mb-3">
        <b className="d-block mb-2">주문 상품 ({items.length})</b>
        {gradeRate > 0 && <div className="small text-brand mb-1">등급 추가 할인 -{gradeRate}% 적용</div>}
        {items.map((it, i) => (
          <div key={i} className="d-flex justify-content-between small py-1 border-top">
            <span>{it.name}{it.optionName ? ` (${it.optionName})` : ''} × {it.quantity}</span>
            <span className="fw-semibold">{fmt(effectiveUnitPrice(it) * it.quantity)}원</span>
          </div>
        ))}
      </div>

      {/* 주문자 정보 — 회원: 자동입력 / 비회원: 직접입력 */}
      <div className="border rounded p-3 mb-3">
        <b className="d-block mb-2">주문자 정보{!me && <small className="text-muted fw-normal ms-2">비회원 주문</small>}</b>
        {me ? (
          <div className="small text-muted">{me.name} · {me.email}</div>
        ) : (
          <div className="row g-2">
            <div className="col-md-4"><input className="form-control form-control-sm" placeholder="이름 *" maxLength={50} value={guest.guestName} onChange={setGuestField('guestName')} /></div>
            <div className="col-md-4"><input type="email" className="form-control form-control-sm" placeholder="이메일" maxLength={100} value={guest.guestEmail} onChange={setGuestField('guestEmail')} /></div>
            <div className="col-md-4"><input className="form-control form-control-sm" placeholder="휴대폰" maxLength={20} value={guest.guestPhone} onChange={setGuestField('guestPhone')} /></div>
            <div className="col-md-6"><input type="password" className="form-control form-control-sm" placeholder="주문 조회 비밀번호 * (4자 이상)" maxLength={64} value={guest.guestPassword} onChange={setGuestField('guestPassword')} /></div>
            <div className="col-12"><small className="text-muted">비회원 주문 조회 시 주문번호와 함께 사용됩니다. 꼭 기억해주세요.</small></div>
          </div>
        )}
      </div>

      {/* 배송지 */}
      <div className="border rounded p-3 mb-3">
        <div className="d-flex justify-content-between align-items-center mb-2">
          <b>배송지 정보</b>
          {addresses.length > 0 && (
            <select className="form-select form-select-sm" style={{ width: 200 }}
              onChange={(e) => { const a = addresses.find((x) => String(x.id) === e.target.value); if (a) applyAddress(a); }}>
              <option value="">배송지 선택</option>
              {addresses.map((a) => <option key={a.id} value={a.id}>{a.alias}{a.isDefault ? ' (기본)' : ''}</option>)}
            </select>
          )}
        </div>
        <div className="row g-2">
          <div className="col-md-6"><input className="form-control form-control-sm" placeholder="수령인 *" maxLength={50} value={form.receiverName} onChange={set('receiverName')} /></div>
          <div className="col-md-6"><input className="form-control form-control-sm" placeholder="연락처 *" maxLength={20} value={form.receiverPhone} onChange={set('receiverPhone')} /></div>
          <div className="col-md-3">
            <div className="d-flex gap-1">
              <input className="form-control form-control-sm" placeholder="우편번호" value={form.zipcode} readOnly onChange={set('zipcode')} />
              <button type="button" className="btn btn-sm btn-outline-brand flex-shrink-0"
                onClick={() => openPostcode(({ zipcode, address }) => setForm((p) => ({ ...p, zipcode, address })))}>검색</button>
            </div>
          </div>
          <div className="col-md-9"><input className="form-control form-control-sm" placeholder="주소 *" value={form.address} readOnly onChange={set('address')} /></div>
          <div className="col-12"><input className="form-control form-control-sm" placeholder="상세주소" maxLength={100} value={form.addressDetail} onChange={set('addressDetail')} /></div>
          <div className="col-12"><input className="form-control form-control-sm" placeholder="배송메모 (예: 문 앞에 놓아주세요)" maxLength={200} value={form.deliveryMemo} onChange={set('deliveryMemo')} /></div>
        </div>
      </div>

      {/* 결제 수단 */}
      <div className="border rounded p-3 mb-3">
        <b className="d-block mb-2">결제 수단</b>
        <div className="d-flex flex-wrap gap-2">
          {PAY_METHODS.map(([k, v]) => (
            <button key={k} className={`btn btn-sm ${payMethod === k ? 'btn-brand' : 'btn-outline-secondary'}`}
              onClick={() => setPayMethod(k)}>{v}</button>
          ))}
        </div>
        {payMethod === 'BANK_TRANSFER'
          ? <div className="small text-muted mt-2">입금 계좌: 우리은행 (주문 완료 후 안내) · 7일 내 미입금 시 자동 취소됩니다.</div>
          : <div className="small text-muted mt-2">테스트 환경에서는 결제창 없이 결제 완료로 처리됩니다. (PG 연동 예정)</div>}
      </div>

      {/* 할인 — 쿠폰 / 적립금 / 예치금 (회원 전용) */}
      {me ? (
        <div className="border rounded p-3 mb-3">
          <b className="d-block mb-2">할인 적용</b>
          <div className="row g-2 align-items-center">
            <div className="col-md-6">
              <label className="form-label small mb-0">쿠폰 (보유 {myCoupons.length}장)</label>
              <select className="form-select form-select-sm" value={couponId} onChange={(e) => setCouponId(e.target.value)}>
                <option value="">쿠폰 선택 안 함</option>
                {myCoupons.map((mc) => (
                  <option key={mc.id} value={mc.id} disabled={totalAmount < mc.coupon.minOrderAmount}>
                    {mc.coupon.name}
                    {totalAmount < mc.coupon.minOrderAmount ? ` (${fmt(mc.coupon.minOrderAmount)}원 이상)` : ''}
                  </option>
                ))}
              </select>
              {couponDiscount > 0 && <small className="text-brand">-{fmt(couponDiscount)}원 할인 적용</small>}
            </div>
            <div className="col-md-6">
              <label className="form-label small mb-0">적립금 (보유 {fmt(mileageBalance)}P)</label>
              <div className="d-flex gap-1">
                <input type="number" min="0" max="99999999" className="form-control form-control-sm" value={useMileage}
                  onChange={(e) => setMileageInput(e.target.value)} />
                <button type="button" className="btn btn-sm btn-outline-brand flex-shrink-0"
                  onClick={() => setMileageInput(mileageBalance)}>전액 사용</button>
              </div>
              {mileageApplied > 0 && <small className="text-brand">-{fmt(mileageApplied)}P 사용</small>}
            </div>
            <div className="col-md-6">
              <label className="form-label small mb-0">예치금 (보유 {fmt(depositBalance)}원)</label>
              <div className="d-flex gap-1">
                <input type="number" min="0" max="99999999" className="form-control form-control-sm" value={useDeposit}
                  onChange={(e) => setDepositInput(e.target.value)} />
                <button type="button" className="btn btn-sm btn-outline-brand flex-shrink-0"
                  onClick={() => setDepositInput(depositBalance)}>전액 사용</button>
              </div>
              {depositApplied > 0 && <small className="text-brand">-{fmt(depositApplied)}원 사용</small>}
            </div>
          </div>
        </div>
      ) : (
        <div className="border rounded p-3 mb-3 d-flex justify-content-between align-items-center">
          <span className="small text-muted">쿠폰·적립금은 회원 전용입니다.</span>
          <Link to="/member/login?redirect=/cart" className="btn btn-sm btn-outline-brand">로그인</Link>
        </div>
      )}

      {/* 결제 금액 */}
      <div className="bg-brand-light rounded p-3 mb-3">
        <div className="d-flex justify-content-between small"><span>상품금액</span><span>{fmt(totalAmount)}원</span></div>
        <div className="d-flex justify-content-between small"><span>배송비</span><span>{deliveryFee === 0 ? '무료' : fmt(deliveryFee) + '원'}</span></div>
        {couponDiscount > 0 && <div className="d-flex justify-content-between small text-danger"><span>쿠폰 할인</span><span>-{fmt(couponDiscount)}원</span></div>}
        {mileageApplied > 0 && <div className="d-flex justify-content-between small text-danger"><span>적립금 사용</span><span>-{fmt(mileageApplied)}P</span></div>}
        {depositApplied > 0 && <div className="d-flex justify-content-between small text-danger"><span>예치금 사용</span><span>-{fmt(depositApplied)}원</span></div>}
        <hr className="my-2" />
        <div className="d-flex justify-content-between fw-bold fs-5">
          <span>최종 결제금액</span><span className="text-brand">{fmt(finalAmount)}원</span>
        </div>
      </div>

      <button className="btn btn-brand w-100 py-2 fs-5" onClick={submit} disabled={submitting}>
        {submitting ? '주문 처리 중...' : `${fmt(finalAmount)}원 결제하기`}
      </button>
    </div>
  );
}
