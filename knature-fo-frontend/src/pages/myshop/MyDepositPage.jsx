import { useEffect, useState } from 'react';
import api from '../../api/client';
import MyLayout from './MyLayout';

const fmt = (n) => Number(n).toLocaleString();
const TYPE_LABELS = { ADMIN: '관리자', USE: '사용', REFUND: '취소환급' };

export default function MyDepositPage() {
  const [data, setData] = useState({ balance: 0, history: [] });

  useEffect(() => {
    api.get('/mypage/deposit').then((res) => setData(res.data)).catch(() => {});
  }, []);

  return (
    <MyLayout title="예치금 내역">
      <div className="bg-brand-light rounded p-4 mb-3 d-flex justify-content-between align-items-center">
        <span>사용 가능 예치금</span>
        <span className="fs-4 fw-bold text-brand">{fmt(data.balance)}원</span>
      </div>
      <table className="table table-sm">
        <thead className="table-light">
          <tr><th>일시</th><th>구분</th><th className="text-end">변동</th><th className="text-end">잔액</th><th>내용</th></tr>
        </thead>
        <tbody>
          {data.history.map((h) => (
            <tr key={h.id}>
              <td className="small">{h.createdAt?.replace('T', ' ').slice(0, 16)}</td>
              <td><span className="badge bg-brand-light text-brand border">{TYPE_LABELS[h.depositType] || h.depositType}</span></td>
              <td className={`text-end fw-bold ${h.amount > 0 ? 'text-brand' : 'text-danger'}`}>{h.amount > 0 ? '+' : ''}{fmt(h.amount)}원</td>
              <td className="text-end">{fmt(h.balanceAfter)}원</td>
              <td className="small text-muted">{h.reason}</td>
            </tr>
          ))}
          {data.history.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-4">예치금 내역이 없습니다.</td></tr>}
        </tbody>
      </table>
      <small className="text-muted">예치금은 주문 취소·환불 시 환급되며, 주문 시 결제금액에서 차감 사용할 수 있습니다.</small>
    </MyLayout>
  );
}
