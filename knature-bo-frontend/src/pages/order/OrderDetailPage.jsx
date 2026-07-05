import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', EXCHANGE_COMPLETED: '교환완료', RETURN_REQUESTED: '반품요청', RETURN_COMPLETED: '반품완료', REFUND_COMPLETED: '환불완료' };
const PAY_LABELS = { CREDIT_CARD: '신용카드', BANK_TRANSFER: '무통장입금', KAKAO_PAY: '카카오페이', NAVER_PAY: '네이버페이' };

export default function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [status, setStatus] = useState('');
  const [memo, setMemo] = useState('');
  const [error, setError] = useState(null);

  const load = () => {
    api.get(`/orders/${id}`)
      .then((res) => { setOrder(res.data); setStatus(res.data.status); setMemo(res.data.adminMemo || ''); })
      .catch(() => setError('주문 정보를 불러오지 못했습니다.'));
  };
  useEffect(load, [id]);

  const fmt = (n) => (n != null ? Number(n).toLocaleString() : '-');

  const saveStatus = () => {
    api.patch(`/orders/${id}/status`, { status }).then(() => { alert('주문 상태가 변경되었습니다.'); load(); });
  };
  const saveMemo = () => {
    api.patch(`/orders/${id}/memo`, { memo }).then(() => alert('메모가 저장되었습니다.'));
  };

  if (error) {
    return (<><TopBar title="주문 상세" /><div className="content-card"><p className="text-danger">{error}</p>
      <button className="btn btn-sm btn-secondary" onClick={() => navigate('/orders')}>목록으로</button></div></>);
  }
  if (!order) return (<><TopBar title="주문 상세" /><div className="content-card">로딩중...</div></>);

  return (
    <>
      <TopBar title="주문 상세" />

      <div className="content-card mb-3">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h6 className="mb-0">주문 정보 <span className="text-muted">{order.orderNumber}</span></h6>
          <button className="btn btn-sm btn-outline-secondary" onClick={() => navigate('/orders')}>← 목록으로</button>
        </div>
        <table className="table table-bordered mb-0">
          <tbody>
            <tr>
              <th className="table-light" style={{ width: 120 }}>주문번호</th><td>{order.orderNumber}</td>
              <th className="table-light" style={{ width: 120 }}>주문일</th><td>{order.createdAt?.replace('T', ' ').slice(0, 16)}</td>
            </tr>
            <tr>
              <th className="table-light">주문자</th><td>{order.ordererName}</td>
              <th className="table-light">연락처</th><td>{order.ordererPhone || '-'}</td>
            </tr>
            <tr>
              <th className="table-light">결제수단</th><td>{PAY_LABELS[order.paymentMethod] || order.paymentMethod}</td>
              <th className="table-light">결제금액</th><td><strong>{fmt(order.paymentAmount)}원</strong> (상품 {fmt(order.totalAmount)} + 배송 {fmt(order.deliveryFee)})</td>
            </tr>
            <tr>
              <th className="table-light">수령인</th><td>{order.receiverName} / {order.receiverPhone || '-'}</td>
              <th className="table-light">배송지</th>
              <td>{order.receiverZipcode ? `[${order.receiverZipcode}] ` : ''}{order.receiverAddress || '-'} {order.receiverAddressDetail || ''}</td>
            </tr>
            <tr>
              <th className="table-light">배송메모</th><td colSpan={3}>{order.deliveryMemo || '-'}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="content-card mb-3">
        <h6 className="mb-3">주문 상품</h6>
        <table className="table">
          <thead className="table-light">
            <tr><th>상품명</th><th>옵션</th><th className="text-center">수량</th><th className="text-end">단가</th><th className="text-end">합계</th></tr>
          </thead>
          <tbody>
            {(order.items || []).map((it) => (
              <tr key={it.id}>
                <td>{it.productName}</td>
                <td>{it.optionName || '-'}</td>
                <td className="text-center">{it.quantity}</td>
                <td className="text-end">{fmt(it.price)}원</td>
                <td className="text-end">{fmt(it.totalPrice)}원</td>
              </tr>
            ))}
            {(!order.items || order.items.length === 0) && (
              <tr><td colSpan={5} className="text-center text-muted py-3">주문 상품 정보가 없습니다.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="content-card">
        <h6 className="mb-3">주문 처리</h6>
        <div className="d-flex gap-2 align-items-center mb-3">
          <label className="mb-0" style={{ width: 90 }}>상태 변경</label>
          <select className="form-select form-select-sm" style={{ width: 180 }} value={status} onChange={(e) => setStatus(e.target.value)}>
            {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <button className="btn btn-sm btn-primary" onClick={saveStatus}>상태 저장</button>
        </div>
        <div className="d-flex gap-2 align-items-start">
          <label className="mb-0" style={{ width: 90, paddingTop: 6 }}>관리자 메모</label>
          <textarea className="form-control form-control-sm" rows={3} style={{ maxWidth: 500 }} value={memo} onChange={(e) => setMemo(e.target.value)} />
          <button className="btn btn-sm btn-outline-primary" onClick={saveMemo}>메모 저장</button>
        </div>
      </div>
    </>
  );
}
