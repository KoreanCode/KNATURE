import { useState } from 'react';
import api from '../../api/client';

const fmt = (n) => Number(n).toLocaleString();
const STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', EXCHANGE_COMPLETED: '교환완료', RETURN_REQUESTED: '반품요청', RETURN_COMPLETED: '반품완료', REFUND_COMPLETED: '환불완료' };

/** 비회원 주문 조회 — 주문번호 + 주문 비밀번호로 조회 */
export default function GuestOrderPage() {
  const [form, setForm] = useState({ orderNumber: '', password: '' });
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setForm((prev) => ({ ...prev, [k]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await api.post('/orders/guest/lookup', form);
      setOrder(res.data);
    } catch (err) {
      setOrder(null);
      setError(err.response?.data?.message || '주문 정보를 찾을 수 없습니다. 주문번호와 비밀번호를 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-5" style={{ maxWidth: 640 }}>
      <h4 className="text-center fw-bold mb-4">비회원 주문 조회</h4>

      <form onSubmit={submit} className="border rounded p-4 mb-4">
        <input className="form-control mb-2" placeholder="주문번호" maxLength={30} value={form.orderNumber}
          onChange={set('orderNumber')} required />
        <input type="password" className="form-control mb-3" placeholder="주문 비밀번호" maxLength={64} value={form.password}
          onChange={set('password')} required />
        {error && <div className="alert alert-danger py-2 small">{error}</div>}
        <button className="btn btn-brand w-100 py-2" disabled={loading}>{loading ? '조회 중...' : '주문 조회'}</button>
        <div className="small text-muted mt-2">주문 시 입력하신 주문 조회 비밀번호로 조회하실 수 있습니다.</div>
      </form>

      {order && (
        <div>
          {/* 주문번호 / 상태 */}
          <div className="border rounded p-3 mb-3 d-flex justify-content-between align-items-center">
            <div>
              <div className="small text-muted">주문번호</div>
              <b>{order.orderNumber}</b>
            </div>
            <span className="badge bg-brand-light text-brand border fs-6">{STATUS_LABELS[order.status] || order.status}</span>
          </div>

          {/* 주문 상품 */}
          <div className="border rounded p-3 mb-3">
            <b className="d-block mb-2">주문 상품</b>
            {(order.items || []).map((it, i) => (
              <div key={it.id ?? i} className="d-flex justify-content-between small py-1 border-top">
                <span>{it.productName}{it.optionName ? ` (${it.optionName})` : ''} × {it.quantity}</span>
                <span className="fw-semibold">{fmt(it.totalPrice)}원</span>
              </div>
            ))}
            <div className="d-flex justify-content-between pt-2 mt-1 border-top fw-bold">
              <span>결제금액</span>
              <span className="text-brand">{fmt(order.paymentAmount)}원</span>
            </div>
          </div>

          {/* 배송 정보 */}
          <div className="border rounded p-3 mb-3 small">
            <b className="d-block mb-2">배송 정보</b>
            <div>{order.receiverName} · {order.receiverPhone}</div>
            <div>{order.receiverZipcode && `[${order.receiverZipcode}] `}{order.receiverAddress} {order.receiverAddressDetail}</div>
            {order.deliveryMemo && <div className="text-muted">메모: {order.deliveryMemo}</div>}
            {order.trackingNumber && (
              <div className="mt-2">
                송장: {order.courierCompany} {order.trackingNumber}
                <a className="btn btn-sm btn-outline-brand ms-2 py-0"
                  href={`https://search.naver.com/search.naver?query=${encodeURIComponent((order.courierCompany || '') + ' ' + order.trackingNumber)}`}
                  target="_blank" rel="noreferrer">배송 추적</a>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
