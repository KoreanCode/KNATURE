import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../api/client';
import MyLayout from './MyLayout';

const fmt = (n) => Number(n).toLocaleString();
const STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', EXCHANGE_COMPLETED: '교환완료', RETURN_REQUESTED: '반품요청', RETURN_COMPLETED: '반품완료', REFUND_COMPLETED: '환불완료' };
const PAY_LABELS = { BANK_TRANSFER: '무통장입금', CREDIT_CARD: '신용카드', KAKAO_PAY: '카카오페이', NAVER_PAY: '네이버페이' };
const FLOW = ['PENDING_PAYMENT', 'PREPARING', 'SHIPPING', 'DELIVERED'];

export default function MyOrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [cs, setCs] = useState(null); // {type, reason}

  const load = () => api.get(`/orders/${id}`).then((res) => setOrder(res.data)).catch(() => navigate('/myshop/orders'));
  useEffect(() => { load(); }, [id]);

  if (!order) return <MyLayout title="주문 상세"><div className="py-5 text-center">로딩중...</div></MyLayout>;

  const flowIdx = FLOW.indexOf(order.status);
  const canCancel = ['PENDING_PAYMENT', 'PREPARING'].includes(order.status);
  const canExchangeReturn = order.status === 'DELIVERED';

  const submitCs = async () => {
    if (!cs.reason || !cs.reason.trim()) { alert('사유를 입력해주세요.'); return; }
    try {
      const res = await api.post(`/orders/${id}/cs`, cs);
      alert(res.data.message);
      setCs(null);
      load();
    } catch (err) {
      alert(err.response?.data?.message || '신청에 실패했습니다.');
    }
  };

  return (
    <MyLayout title={`주문 상세 — ${order.orderNumber}`}>
      {/* 상태 플로우 */}
      {flowIdx >= 0 ? (
        <div className="d-flex justify-content-between text-center border rounded p-3 mb-3">
          {FLOW.map((s, i) => (
            <div key={s} className={`flex-fill small ${i === flowIdx ? 'fw-bold text-brand' : 'text-muted'}`}>
              {STATUS_LABELS[s]}{i < FLOW.length - 1 && <i className="bi bi-chevron-right ms-2"></i>}
            </div>
          ))}
        </div>
      ) : (
        <div className="alert alert-secondary py-2 small">현재 상태: <b>{STATUS_LABELS[order.status]}</b></div>
      )}

      {/* 주문 상품 */}
      <div className="border rounded p-3 mb-3">
        <b className="d-block mb-2">주문 상품</b>
        {(order.items || []).map((it) => (
          <div key={it.id} className="d-flex justify-content-between small py-1 border-top">
            <span>{it.productName}{it.optionName ? ` (${it.optionName})` : ''} × {it.quantity}</span>
            <span className="fw-semibold">{fmt(it.totalPrice)}원</span>
          </div>
        ))}
        <div className="d-flex justify-content-between pt-2 mt-1 border-top fw-bold">
          <span>결제금액 ({PAY_LABELS[order.paymentMethod]})</span>
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
              href={`https://search.naver.com/search.naver?query=${encodeURIComponent(order.courierCompany + ' ' + order.trackingNumber)}`}
              target="_blank" rel="noreferrer">배송 추적</a>
          </div>
        )}
      </div>

      {/* CS 신청 */}
      <div className="d-flex gap-2">
        {canCancel && <button className="btn btn-sm btn-outline-danger" onClick={() => setCs({ type: 'cancel', reason: '' })}>주문 취소 신청</button>}
        {canExchangeReturn && (
          <>
            <button className="btn btn-sm btn-outline-brand" onClick={() => setCs({ type: 'exchange', reason: '' })}>교환 신청</button>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setCs({ type: 'return', reason: '' })}>반품 신청</button>
          </>
        )}
      </div>

      {cs && (
        <div className="border rounded p-3 mt-3">
          <b>{cs.type === 'cancel' ? '취소' : cs.type === 'exchange' ? '교환' : '반품'} 신청 사유</b>
          <textarea className="form-control form-control-sm mt-2" rows={2} maxLength={2000} value={cs.reason}
            onChange={(e) => setCs({ ...cs, reason: e.target.value })} placeholder="사유를 입력해주세요" />
          <div className="d-flex gap-2 mt-2">
            <button className="btn btn-sm btn-brand" onClick={submitCs}>신청하기</button>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setCs(null)}>닫기</button>
          </div>
        </div>
      )}
    </MyLayout>
  );
}
