import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import Pagination from '../../components/Pagination';
import { downloadFile } from '../../utils/download';

const STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', RETURN_REQUESTED: '반품요청', REFUND_COMPLETED: '환불완료' };
const PAY_LABELS = { CREDIT_CARD: '신용카드', BANK_TRANSFER: '무통장입금', KAKAO_PAY: '카카오페이', NAVER_PAY: '네이버페이' };

export default function OrderListPage() {
  const [orders, setOrders] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [items, setItems] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [from, setFrom] = useState(searchParams.get('from') || '');
  const [to, setTo] = useState(searchParams.get('to') || '');
  const [bulkModal, setBulkModal] = useState(false);
  const [bulkFile, setBulkFile] = useState(null);
  const [bulkResult, setBulkResult] = useState(null);
  const navigate = useNavigate();

  const page = parseInt(searchParams.get('page') || '0');
  const status = searchParams.get('status') || '';
  const tab = searchParams.get('tab') || 'order'; // order | item

  const filterParams = () => {
    const params = {};
    if (keyword) params.keyword = keyword;
    if (status) params.status = status;
    if (searchParams.get('from')) params.from = searchParams.get('from');
    if (searchParams.get('to')) params.to = searchParams.get('to');
    return params;
  };

  useEffect(() => {
    const params = { ...filterParams(), page, size: 20 };
    if (tab === 'item') {
      api.get('/orders/items', { params }).then((res) => setItems(res.data));
    } else {
      api.get('/orders', { params }).then((res) => setOrders(res.data));
    }
  }, [page, status, tab, searchParams]);

  const setParams = (extra) => setSearchParams({ keyword, status, from, to, tab, page: 0, ...extra });
  const handleSearch = (e) => { e.preventDefault(); setParams({}); };
  const fmt = (n) => Number(n).toLocaleString();

  const downloadExcel = () => downloadFile('/orders/excel', '주문목록.xlsx', filterParams());

  const downloadTemplate = () => downloadFile('/orders/shipping-template', '송장일괄등록_템플릿.csv');

  const uploadBulk = () => {
    if (!bulkFile) { alert('CSV 파일을 선택해주세요.'); return; }
    const fd = new FormData();
    fd.append('file', bulkFile);
    api.post('/orders/shipping-bulk', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((res) => { setBulkResult(res.data); setParams({}); })
      .catch((err) => alert(err.response?.data?.message || '업로드에 실패했습니다.'));
  };

  const current = tab === 'item' ? items : orders;

  return (
    <>
      <TopBar title="주문 관리" />
      <div className="content-card">
        <ul className="nav nav-tabs mb-3">
          <li className="nav-item">
            <button className={`nav-link ${tab === 'order' ? 'active' : ''}`} onClick={() => setParams({ tab: 'order' })}>주문번호별</button>
          </li>
          <li className="nav-item">
            <button className={`nav-link ${tab === 'item' ? 'active' : ''}`} onClick={() => setParams({ tab: 'item' })}>품목별</button>
          </li>
        </ul>

        <div className="d-flex justify-content-between mb-3 flex-wrap gap-2">
          <form className="d-flex gap-2 flex-wrap" onSubmit={handleSearch}>
            <input type="date" className="form-control form-control-sm" style={{ width: 150 }} value={from} onChange={(e) => setFrom(e.target.value)} />
            <span className="align-self-center">~</span>
            <input type="date" className="form-control form-control-sm" style={{ width: 150 }} value={to} onChange={(e) => setTo(e.target.value)} />
            <input type="text" className="form-control form-control-sm" placeholder={tab === 'item' ? '주문번호/주문자/상품명' : '주문번호/주문자명'} value={keyword} maxLength={50} onChange={(e) => setKeyword(e.target.value)} style={{ width: 200 }} />
            <select className="form-select form-select-sm" style={{ width: 140 }} value={status} onChange={(e) => setParams({ status: e.target.value })}>
              <option value="">전체 상태</option>
              {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="btn btn-sm btn-outline-primary">검색</button>
          </form>
          <div className="d-flex gap-2">
            <button className="btn btn-sm btn-outline-primary" onClick={() => { setBulkModal(true); setBulkResult(null); setBulkFile(null); }}>
              <i className="bi bi-upload"></i> 송장 일괄 등록
            </button>
            <button className="btn btn-sm btn-outline-success" onClick={downloadExcel}>
              <i className="bi bi-file-earmark-excel"></i> 엑셀 다운로드
            </button>
          </div>
        </div>

        {tab === 'order' ? (
          <table className="table table-hover">
            <thead className="table-light">
              <tr><th style={{ width: 60 }}>번호</th><th>주문번호</th><th>주문자</th><th className="text-end">결제금액</th><th className="text-center">결제수단</th><th className="text-center">상태</th><th className="text-center">송장</th><th className="text-center">주문일</th></tr>
            </thead>
            <tbody>
              {orders.content.map((o, i) => (
                <tr key={o.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/orders/${o.id}`)}>
                  <td>{orders.number * 20 + i + 1}</td><td>{o.orderNumber}</td>
                  <td>{o.ordererName}{o.guest === true && <span className="badge bg-secondary ms-1">비회원</span>}</td>
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
        ) : (
          <table className="table table-hover">
            <thead className="table-light">
              <tr><th style={{ width: 60 }}>번호</th><th>주문번호</th><th>상품명</th><th>옵션</th><th className="text-center">수량</th><th className="text-end">합계</th><th>주문자</th><th className="text-center">상태</th><th className="text-center">주문일</th></tr>
            </thead>
            <tbody>
              {items.content.map((it, i) => (
                <tr key={it.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/orders/${it.order?.id}`)}>
                  <td>{items.number * 20 + i + 1}</td>
                  <td>{it.order?.orderNumber}</td>
                  <td>{it.productName}</td>
                  <td>{it.optionName || '-'}</td>
                  <td className="text-center">{it.quantity}</td>
                  <td className="text-end">{fmt(it.totalPrice)}원</td>
                  <td>{it.order?.ordererName}{it.order?.guest === true && <span className="badge bg-secondary ms-1">비회원</span>}</td>
                  <td className="text-center"><span className="badge bg-info">{STATUS_LABELS[it.order?.status]}</span></td>
                  <td className="text-center">{it.order?.createdAt?.slice(0, 10)}</td>
                </tr>
              ))}
              {items.totalElements === 0 && <tr><td colSpan={9} className="text-center text-muted py-4">품목 내역이 없습니다.</td></tr>}
            </tbody>
          </table>
        )}

        <Pagination
          totalPages={current.totalPages}
          page={current.number}
          onChange={(p) => setSearchParams({ keyword, status, from, to, tab, page: p })}
        />
      </div>

      {bulkModal && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setBulkModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header">
                <h6 className="modal-title">송장 일괄 등록</h6>
                <button type="button" className="btn-close" onClick={() => setBulkModal(false)} />
              </div>
              <div className="modal-body">
                <p className="mb-2"><small>CSV 형식: <code>주문번호,택배사,송장번호</code> — 등록 시 해당 주문은 <b>배송중</b>으로 변경됩니다. (Excel에서 CSV로 저장한 파일 지원)</small></p>
                <button type="button" className="btn btn-sm btn-outline-secondary mb-3" onClick={downloadTemplate}>
                  <i className="bi bi-download"></i> 템플릿 다운로드
                </button>
                <input type="file" accept=".csv" className="form-control" onChange={(e) => setBulkFile(e.target.files?.[0] || null)} />
                {bulkResult && (
                  <div className={`alert ${bulkResult.fail > 0 ? 'alert-warning' : 'alert-success'} mt-3 mb-0`}>
                    <div><b>성공 {bulkResult.success}건 / 실패 {bulkResult.fail}건</b></div>
                    {bulkResult.errors?.length > 0 && (
                      <ul className="mb-0 mt-1" style={{ fontSize: 13 }}>
                        {bulkResult.errors.map((e, i) => <li key={i}>{e}</li>)}
                      </ul>
                    )}
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setBulkModal(false)}>닫기</button>
                <button className="btn btn-primary btn-sm" onClick={uploadBulk}>업로드</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
