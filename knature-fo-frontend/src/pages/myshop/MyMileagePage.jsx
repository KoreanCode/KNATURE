import { useEffect, useState } from 'react';
import api from '../../api/client';
import MyLayout from './MyLayout';

const fmt = (n) => Number(n).toLocaleString();
const TYPE_LABELS = { JOIN: '가입적립', PURCHASE: '구매적립', USE: '사용', ADMIN: '관리자', REFUND: '취소환급', REVIEW: '리뷰적립' };

export default function MyMileagePage() {
  const [data, setData] = useState({ balance: 0, pending: 0, history: [] });

  useEffect(() => {
    api.get('/mypage/mileage').then((res) => setData(res.data)).catch(() => {});
  }, []);

  return (
    <MyLayout title="적립금 내역">
      <div className="bg-brand-light rounded p-4 mb-3 d-flex justify-content-between align-items-center">
        <span>사용 가능 적립금</span>
        <div className="text-end">
          <span className="fs-4 fw-bold text-brand">{fmt(data.balance)}P</span>
          {data.pending > 0 && (
            <div className="small text-muted">사용 대기 {fmt(data.pending)}P (배송완료 후 20일 뒤 사용 가능)</div>
          )}
        </div>
      </div>
      <table className="table table-sm">
        <thead className="table-light">
          <tr><th>일시</th><th>구분</th><th className="text-end">변동</th><th className="text-end">잔액</th><th>내용</th></tr>
        </thead>
        <tbody>
          {data.history.map((h) => (
            <tr key={h.id}>
              <td className="small">{h.createdAt?.replace('T', ' ').slice(0, 16)}</td>
              <td><span className="badge bg-brand-light text-brand border">{TYPE_LABELS[h.mileageType]}</span></td>
              <td className={`text-end fw-bold ${h.amount > 0 ? 'text-brand' : 'text-danger'}`}>
                {h.amount > 0 ? '+' : ''}{fmt(h.amount)}P
                {h.pending === true && (
                  <div className="fw-normal">
                    <span className="badge bg-warning text-dark">적립예정</span>
                    {h.availableAt && <small className="text-muted ms-1">({h.availableAt.slice(5, 10)}부터)</small>}
                  </div>
                )}
              </td>
              <td className="text-end">{fmt(h.balanceAfter)}P</td>
              <td className="small text-muted">{h.reason}</td>
            </tr>
          ))}
          {data.history.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-4">적립금 내역이 없습니다.</td></tr>}
        </tbody>
      </table>
      <small className="text-muted">적립금은 주문 시 결제금액에서 차감 사용할 수 있습니다. 배송완료 시 등급별 적립율만큼 자동 적립됩니다.</small>
    </MyLayout>
  );
}
