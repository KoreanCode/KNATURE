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
  const [from, setFrom] = useState(searchParams.get('from') || '');
  const [to, setTo] = useState(searchParams.get('to') || '');
  const navigate = useNavigate();

  const page = parseInt(searchParams.get('page') || '0');
  const status = searchParams.get('status') || '';

  const filterParams = () => {
    const params = {};
    if (keyword) params.keyword = keyword;
    if (status) params.status = status;
    if (searchParams.get('from')) params.from = searchParams.get('from');
    if (searchParams.get('to')) params.to = searchParams.get('to');
    return params;
  };

  useEffect(() => {
    api.get('/orders', { params: { ...filterParams(), page, size: 20 } }).then((res) => setOrders(res.data));
  }, [page, status, searchParams]);

  const handleSearch = (e) => { e.preventDefault(); setSearchParams({ keyword, status, from, to, page: 0 }); };
  const fmt = (n) => Number(n).toLocaleString();

  const downloadExcel = () => {
    api.get('/orders/excel', { params: filterParams(), responseType: 'blob' }).then((res) => {
      const url = URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = '주문목록.csv';
      a.click();
      URL.revokeObjectURL(url);
    });
  };

  return (
    <>
      <TopBar title="주문 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3 flex-wrap gap-2">
          <form className="d-flex gap-2 flex-wrap" onSubmit={handleSearch}>
            <input type="date" className="form-control form-control-sm" style={{ width: 150 }} value={from} onChange={(e) => setFrom(e.target.value)} />
            <span className="align-self-center">~</span>
            <input type="date" className="form-control form-control-sm" style={{ width: 150 }} value={to} onChange={(e) => setTo(e.target.value)} />
            <input type="text" className="form-control form-control-sm" placeholder="주문번호/주문자명" value={keyword} onChange={(e) => setKeyword(e.target.value)} style={{ width: 200 }} />
            <select className="form-select form-select-sm" style={{ width: 140 }} value={status} onChange={(e) => setSearchParams({ keyword, status: e.target.value, from, to, page: 0 })}>
              <option value="">전체 상태</option>
              {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="btn btn-sm btn-outline-primary">검색</button>
          </form>
          <button className="btn btn-sm btn-outline-success" onClick={downloadExcel}>
            <i className="bi bi-file-earmark-excel"></i> 엑셀 다운로드
          </button>
        </div>

        <table className="table table-hover">
          <thead className="table-light">
            <tr><th style={{ width: 60 }}>번호</th><th>주문번호</th><th>주문자</th><th className="text-end">결제금액</th><th className="text-center">결제수단</th><th className="text-center">상태</th><th className="text-center">송장</th><th className="text-center">주문일</th></tr>
          </thead>
          <tbody>
            {orders.content.map((o, i) => (
              <tr key={o.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/orders/${o.id}`)}>
                <td>{orders.number * 20 + i + 1}</td><td>{o.orderNumber}</td><td>{o.ordererName}</td>
                <td className="text-end">{fmt(o.paymentAmount)}원</td>
                <td className="text-center">{PAY_LABELS[o.paymentMethod]}</td>
                <td className="text-center"><span className="badge bg-info">{STATUS_LABELS[o.status]}</span></td>
                <td className="text-center">{o.trackingNumber ? <span className="badge bg-secondary">등록</span> : '-'}</td>
                <td className="text-center">{o.createdAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {orders.totalElements === 0 && <tr><td colSpan={8} className="text-center text-muted py-4">주문 내역이 없습니다.</td></tr>}
          </tbody>
        </table>

        <Pagination
          totalPages={orders.totalPages}
          page={orders.number}
          onChange={(p) => setSearchParams({ keyword, status, from, to, page: p })}
        />
      </div>
    </>
  );
}
