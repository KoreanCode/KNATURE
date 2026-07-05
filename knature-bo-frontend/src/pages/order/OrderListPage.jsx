import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import Pagination from '../../components/Pagination';

const STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', RETURN_REQUESTED: '반품요청', REFUND_COMPLETED: '환불완료' };
const PAY_LABELS = { CREDIT_CARD: '신용카드', BANK_TRANSFER: '무통장입금', KAKAO_PAY: '카카오페이', NAVER_PAY: '네이버페이' };

export default function OrderListPage() {
  const [orders, setOrders] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const navigate = useNavigate();

  const page = parseInt(searchParams.get('page') || '0');
  const status = searchParams.get('status') || '';

  useEffect(() => {
    const params = { page, size: 20 };
    if (keyword) params.keyword = keyword;
    if (status) params.status = status;
    api.get('/orders', { params }).then((res) => setOrders(res.data));
  }, [page, status, searchParams]);

  const handleSearch = (e) => { e.preventDefault(); setSearchParams({ keyword, status, page: 0 }); };
  const fmt = (n) => Number(n).toLocaleString();

  return (
    <>
      <TopBar title="주문 관리" />
      <div className="content-card">
        <form className="d-flex gap-2 mb-3" onSubmit={handleSearch}>
          <input type="text" className="form-control form-control-sm" placeholder="주문번호/주문자명" value={keyword} onChange={(e) => setKeyword(e.target.value)} style={{ width: 220 }} />
          <select className="form-select form-select-sm" style={{ width: 140 }} value={status} onChange={(e) => setSearchParams({ keyword, status: e.target.value, page: 0 })}>
            <option value="">전체 상태</option>
            {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <button className="btn btn-sm btn-outline-primary">검색</button>
        </form>

        <table className="table table-hover">
          <thead className="table-light">
            <tr><th style={{ width: 60 }}>번호</th><th>주문번호</th><th>주문자</th><th className="text-end">결제금액</th><th className="text-center">결제수단</th><th className="text-center">상태</th><th className="text-center">주문일</th></tr>
          </thead>
          <tbody>
            {orders.content.map((o, i) => (
              <tr key={o.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/orders/${o.id}`)}>
                <td>{orders.number * 20 + i + 1}</td><td>{o.orderNumber}</td><td>{o.ordererName}</td>
                <td className="text-end">{fmt(o.paymentAmount)}원</td>
                <td className="text-center">{PAY_LABELS[o.paymentMethod]}</td>
                <td className="text-center"><span className="badge bg-info">{STATUS_LABELS[o.status]}</span></td>
                <td className="text-center">{o.createdAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {orders.totalElements === 0 && <tr><td colSpan={7} className="text-center text-muted py-4">주문 내역이 없습니다.</td></tr>}
          </tbody>
        </table>

        <Pagination
          totalPages={orders.totalPages}
          page={orders.number}
          onChange={(p) => setSearchParams({ keyword, status, page: p })}
        />
      </div>
    </>
  );
}
