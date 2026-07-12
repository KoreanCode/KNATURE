import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
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
  const [addresses, setAddresses] = useState([]);
  const [shopInfo, setShopInfo] = useState({});
  const [form, setForm] = useState({ receiverName: '', receiverPhone: '', zipcode: '', address: '', addressDetail: '', deliveryMemo: '' });
  const [payMethod, setPayMethod] = useState('BANK_TRANSFER');
  const [submitting, setSubmitting] = useState(false);
  // 적립금/쿠폰 (2차)
  const [mileageBalance, setMileageBalance] = useState(0);
  const [useMileage, setUseMileage] = useState(0);
  const [myCoupons, setMyCoupons] = useState([]);
  const [couponId, setCouponId] = useState('');

  useEffect(() => {
    if (items.length === 0) { navigate('/cart'); return; }
    api.get('/auth/me')
      .then((res) => setMe(res.data))
      .catch(() => navigate('/member/login?redirect=/cart')); // 주문은 로그인 필요
    api.get('/addresses').then((res) => {
      setAddresses(res.data);
      const def = res.data.find((a) => a.isDefault);
      if (def) applyAddress(def);
    }).catch(() => {});
    api.get('/shop-info').then((res) => setShopInfo(res.data)).catch(() => {});
    api.get('/mypage/mileage').then((res) => setMileageBalance(res.data.balance)).catch(() => {});
    api.get('/mypage/coupons').then((res) => setMyCoupons(res.data.filter((mc) => !mc.used))).catch(() => {});
  }, []);

  const applyAddress = (a) => setForm((f) => ({
    ...f, receiverName: a.receiverName, receiverPhone: a.receiverPhone,
    zipcode: a.zipcode, address: a.address, addressDetail: a.addressDetail || '',
  }));

  const set = (k) => (e) => setForm((prev) => ({ ...prev, [k]: e.target.value }));

  const totalAmount = items.reduce((sum, it) => sum + it.unitPrice * it.quantity, 0);
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
  const payable = totalAmount + deliveryFee - couponDiscount;
  const mileageApplied = Math.min(useMileage || 0, mileageBalance, payable);
  const finalAmount = payable - mileageApplied;

  const setMileageInput = (v) => {
    const n = Math.max(0, parseInt(v) || 0);
    setUseMileage(Math.min(n, mileageBalance, payable));
  };

  const submit = async () => {
    if (!form.receiverName || !form.address) { alert('배송지 정보를 입력해주세요.'); return; }
    setSubmitting(true);
    try {
      const res = await api.post('/orders', {
        items: items.map((it) => ({ productId: it.productId, optionId: it.optionId, quantity: it.quantity })),
        paymentMethod: payMethod,
        useMileage: mileageApplied,
        memberCouponId: selectedCoupon ? selectedCoupon.id : null,
        ...form,
      });
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

  if (!me) return <div className="container py-5 text-center">로딩중...</div>;

  return (
    <div className="container py-4" style={{ maxWidth: 900 }}>
      <h4 className="fw-bold text-center mb-4">주문 / 결제</h4>

      {/* 주문 상품 */}
      <div className="border rounded p-3 mb-3">
        <b className="d-block mb-2">주문 상품 ({items.length})</b>
        {items.map((it, i) => (
          <div key={i} className="d-flex justify-content-between small py-1 border-top">
            <span>{it.name}{it.optionName ? ` (${it.optionName})` : ''} × {it.quantity}</span>
            <span className="fw-semibold">{fmt(it.unitPrice * it.quantity)}원</span>
          </div>
        ))}
      </div>

      {/* 주문자 정보 (회원정보 자동입력) */}
      <div className="border rounded p-3 mb-3">
        <b className="d-block mb-2">주문자 정보</b>
        <div className="small text-muted">{me.name} · {me.email}</div>
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

      {/* 할인 — 쿠폰 / 적립금 */}
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
        </div>
      </div>

      {/* 결제 금액 */}
      <div className="bg-brand-light rounded p-3 mb-3">
        <div className="d-flex justify-content-between small"><span>상품금액</span><span>{fmt(totalAmount)}원</span></div>
        <div className="d-flex justify-content-between small"><span>배송비</span><span>{deliveryFee === 0 ? '무료' : fmt(deliveryFee) + '원'}</span></div>
        {couponDiscount > 0 && <div className="d-flex justify-content-between small text-danger"><span>쿠폰 할인</span><span>-{fmt(couponDiscount)}원</span></div>}
        {mileageApplied > 0 && <div className="d-flex justify-content-between small text-danger"><span>적립금 사용</span><span>-{fmt(mileageApplied)}P</span></div>}
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
