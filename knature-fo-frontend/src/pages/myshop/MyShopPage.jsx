import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/client';
import MyLayout from './MyLayout';

export default function MyShopPage() {
  const [summary, setSummary] = useState({});

  useEffect(() => {
    api.get('/orders/summary').then((res) => setSummary(res.data)).catch(() => {});
  }, []);

  const entries = Object.entries(summary);
  const flow = entries.slice(0, 4);   // 입금전~배송완료
  const cs = entries.slice(4);        // 취소/교환/반품

  return (
    <MyLayout title="주문 현황">
      <div className="row g-2 text-center mb-4">
        {flow.map(([label, count], i) => (
          <div key={label} className="col-3">
            <Link to="/myshop/orders" className="d-block border rounded py-3">
              <div className="fs-4 fw-bold text-brand">{count}</div>
              <div className="small text-muted">{label}{i < flow.length - 1 && <i className="bi bi-chevron-right ms-1"></i>}</div>
            </Link>
          </div>
        ))}
      </div>
      <div className="d-flex gap-3 border rounded p-3">
        {cs.map(([label, count]) => (
          <div key={label} className="small">
            <span className="text-muted">{label}</span> <b className={count > 0 ? 'text-danger' : ''}>{count}</b>
          </div>
        ))}
      </div>
      <p className="small text-muted mt-3">최근 주문의 상태별 건수입니다. 상세 내역은 [주문 조회]에서 확인하세요.</p>
    </MyLayout>
  );
}
