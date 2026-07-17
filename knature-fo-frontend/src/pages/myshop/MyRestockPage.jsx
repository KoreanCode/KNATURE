import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/client';
import MyLayout from './MyLayout';

export default function MyRestockPage() {
  const navigate = useNavigate();
  const [alerts, setAlerts] = useState([]);

  const load = () => api.get('/restock-alerts').then((res) => setAlerts(res.data)).catch(() => setAlerts([]));
  useEffect(() => { load(); }, []);

  const cancel = async (id) => {
    if (!confirm('재입고 알림 신청을 취소하시겠습니까?')) return;
    try {
      const res = await api.delete(`/restock-alerts/${id}`);
      alert(res.data.message);
      load();
    } catch (err) {
      alert(err.response?.data?.message || '취소에 실패했습니다.');
    }
  };

  return (
    <MyLayout title="재입고 알림">
      <table className="table table-sm align-middle">
        <thead className="table-light">
          <tr><th>상품명</th><th>신청일</th><th>상태</th><th></th></tr>
        </thead>
        <tbody>
          {alerts.map((a) => (
            <tr key={a.id}>
              <td>
                <span style={{ cursor: 'pointer' }} className="fw-semibold"
                  onClick={() => navigate(`/products/${a.product?.id}`)}>{a.product?.name}</span>
              </td>
              <td className="small text-muted">{a.createdAt?.slice(0, 10)}</td>
              <td>
                {a.notified
                  ? <span className="badge bg-success">재입고 알림 {a.notifiedAt?.replace('T', ' ').slice(5, 16)}</span>
                  : <span className="badge bg-secondary">대기중</span>}
              </td>
              <td className="text-end">
                <button className="btn btn-sm btn-outline-secondary" onClick={() => cancel(a.id)}>취소</button>
              </td>
            </tr>
          ))}
          {alerts.length === 0 && (
            <tr><td colSpan={4} className="text-center text-muted py-4">재입고 알림 신청 내역이 없습니다.</td></tr>
          )}
        </tbody>
      </table>
      <small className="text-muted">품절 상품 상세 페이지에서 재입고 알림을 신청하면 재입고 시 알려드립니다.</small>
    </MyLayout>
  );
}
