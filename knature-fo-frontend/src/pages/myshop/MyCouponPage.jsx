import { useEffect, useState } from 'react';
import api from '../../api/client';
import MyLayout from './MyLayout';

const fmt = (n) => Number(n).toLocaleString();

export default function MyCouponPage() {
  const [coupons, setCoupons] = useState([]);

  useEffect(() => {
    api.get('/mypage/coupons').then((res) => setCoupons(res.data)).catch(() => {});
  }, []);

  const usable = coupons.filter((mc) => !mc.used);
  const usedList = coupons.filter((mc) => mc.used);

  const discountLabel = (c) => c.discountType === 'PERCENT'
    ? `${c.amount}% 할인${c.maxDiscount ? ` (최대 ${fmt(c.maxDiscount)}원)` : ''}`
    : `${fmt(c.amount)}원 할인`;

  const CouponCard = ({ mc, dim }) => (
    <div className={`border rounded p-3 mb-2 d-flex justify-content-between align-items-center ${dim ? 'opacity-50' : ''}`}>
      <div>
        <div className="fw-bold">{mc.coupon.name}</div>
        <div className="text-brand fw-semibold">{discountLabel(mc.coupon)}</div>
        <div className="small text-muted">
          {mc.coupon.minOrderAmount > 0 && `${fmt(mc.coupon.minOrderAmount)}원 이상 주문 시 · `}
          {mc.coupon.validUntil ? `~${mc.coupon.validUntil}` : '무기한'}
        </div>
      </div>
      {dim
        ? <span className="badge bg-secondary">사용완료</span>
        : <span className="badge bg-brand-light text-brand border">사용가능</span>}
    </div>
  );

  return (
    <MyLayout title="쿠폰 내역">
      <div className="bg-brand-light rounded p-3 mb-3">
        사용 가능한 쿠폰 <b className="text-brand">{usable.length}</b>장
      </div>
      {usable.map((mc) => <CouponCard key={mc.id} mc={mc} />)}
      {usedList.length > 0 && <h6 className="mt-4 mb-2 text-muted">사용한 쿠폰</h6>}
      {usedList.map((mc) => <CouponCard key={mc.id} mc={mc} dim />)}
      {coupons.length === 0 && <div className="text-center text-muted py-5">보유한 쿠폰이 없습니다.</div>}
      <small className="text-muted d-block mt-3">쿠폰은 주문서에서 선택해 적용할 수 있습니다. (주문당 1장)</small>
    </MyLayout>
  );
}
