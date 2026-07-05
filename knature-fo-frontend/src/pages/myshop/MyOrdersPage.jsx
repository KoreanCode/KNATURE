import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/client';
import MyLayout from './MyLayout';

const fmt = (n) => Number(n).toLocaleString();
const STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', EXCHANGE_COMPLETED: '교환완료', RETURN_REQUESTED: '반품요청', RETURN_COMPLETED: '반품완료', REFUND_COMPLETED: '환불완료' };

const PERIODS = [[1, '1개월'], [3, '3개월'], [6, '6개월'], [null, '전체']];

export default function MyOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [months, setMonths] = useState(3); // 기본 최근 3개월

  useEffect(() => {
    const params = months ? { months } : {};
    api.get('/orders', { params }).then((res) => setOrders(res.data)).catch(() => {});
  }, [months]);

  return (
    <MyLayout title="주문 조회">
      <div className="d-flex gap-2 mb-3">
        {PERIODS.map(([m, label]) => (
          <button key={label} className={`btn btn-sm ${months === m ? 'btn-brand' : 'btn-outline-secondary'}`}
            onClick={() => setMonths(m)}>{label}</button>
        ))}
      </div>
      {orders.length === 0 ? (
        <div className="text-center text-muted py-5">해당 기간의 주문 내역이 없습니다.</div>
      ) : (
        orders.map((o) => (
          <Link key={o.id} to={`/myshop/orders/${o.id}`} className="d-block border rounded p-3 mb-2">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="small text-muted">{o.createdAt?.slice(0, 10)} · {o.orderNumber}</div>
                <div className="fw-semibold mt-1">
                  {(o.items || [])[0]?.productName}
                  {(o.items || []).length > 1 && ` 외 ${o.items.length - 1}건`}
                </div>
                <div className="small">{fmt(o.paymentAmount)}원</div>
              </div>
              <div className="text-end">
                <span className="badge bg-brand-light text-brand border">{STATUS_LABELS[o.status]}</span>
                {o.trackingNumber && <div className="small text-muted mt-1">{o.courierCompany} {o.trackingNumber}</div>}
              </div>
            </div>
          </Link>
        ))
      )}
    </MyLayout>
  );
}
