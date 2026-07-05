import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import Pagination from '../../components/Pagination';

const STATUS_LABELS = { ON_SALE: '판매중', SOLD_OUT: '품절', HIDDEN: '숨김' };
const STATUS_COLORS = { ON_SALE: 'bg-success', SOLD_OUT: 'bg-danger', HIDDEN: 'bg-secondary' };

export default function StockListPage() {
  const [stocks, setStocks] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [adjusting, setAdjusting] = useState(null); // { id, name, quantity, reason }
  const [historyFor, setHistoryFor] = useState(null); // { id, name, rows }

  const page = parseInt(searchParams.get('page') || '0');

  const load = () => {
    const params = { page, size: 20 };
    if (keyword) params.keyword = keyword;
    api.get('/stocks', { params }).then((res) => setStocks(res.data));
  };
  useEffect(load, [page, searchParams]);

  const handleSearch = (e) => { e.preventDefault(); setSearchParams({ keyword, page: 0 }); };

  const openAdjust = (p) => setAdjusting({ id: p.id, name: p.name, quantity: p.stockQuantity ?? 0, reason: '' });

  const saveAdjust = () => {
    api.patch(`/stocks/${adjusting.id}`, { quantity: String(adjusting.quantity), reason: adjusting.reason })
      .then(() => { alert('재고가 조정되었습니다.'); setAdjusting(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '재고 조정에 실패했습니다. 사유를 입력했는지 확인해주세요.'));
  };

  const openHistory = (p) => {
    api.get(`/stocks/${p.id}/history`).then((res) => setHistoryFor({ id: p.id, name: p.name, rows: res.data }));
  };

  return (
    <>
      <TopBar title="재고 관리" />
      <div className="content-card">
        <form className="d-flex gap-2 mb-3" onSubmit={handleSearch}>
          <input type="text" className="form-control form-control-sm" placeholder="상품명 검색" value={keyword} onChange={(e) => setKeyword(e.target.value)} style={{ width: 200 }} />
          <button className="btn btn-sm btn-outline-primary">검색</button>
        </form>

        <table className="table table-hover">
          <thead className="table-light">
            <tr>
              <th style={{ width: 60 }}>번호</th>
              <th>상품명</th>
              <th>상품코드</th>
              <th className="text-end">재고 수량</th>
              <th className="text-center">판매상태</th>
              <th className="text-center" style={{ width: 180 }}>관리</th>
            </tr>
          </thead>
          <tbody>
            {stocks.content.map((p, i) => (
              <tr key={p.id} className={(p.stockQuantity ?? 0) === 0 ? 'table-danger' : ''}>
                <td>{stocks.number * 20 + i + 1}</td>
                <td>{p.name}</td>
                <td>{p.code || '-'}</td>
                <td className="text-end fw-bold">{(p.stockQuantity ?? 0).toLocaleString()}개</td>
                <td className="text-center"><span className={`badge ${STATUS_COLORS[p.status]}`}>{STATUS_LABELS[p.status]}</span></td>
                <td className="text-center">
                  <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openAdjust(p)}>재고 조정</button>
                  <button className="btn btn-sm btn-outline-secondary" onClick={() => openHistory(p)}>이력</button>
                </td>
              </tr>
            ))}
            {stocks.totalElements === 0 && (
              <tr><td colSpan={6} className="text-center text-muted py-4">상품이 없습니다.</td></tr>
            )}
          </tbody>
        </table>

        <Pagination totalPages={stocks.totalPages} page={stocks.number} onChange={(p) => setSearchParams({ keyword, page: p })} />
      </div>

      {adjusting && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setAdjusting(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">재고 조정 — {adjusting.name}</h6>
                <button type="button" className="btn-close" onClick={() => setAdjusting(null)} /></div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">재고 수량 *</label>
                  <input type="number" min="0" className="form-control" value={adjusting.quantity}
                    onChange={(e) => setAdjusting({ ...adjusting, quantity: e.target.value })} />
                  <small className="text-muted">0으로 저장 시 자동 품절 처리, 재고 확보 시 품절 자동 해제됩니다.</small>
                </div>
                <div>
                  <label className="form-label">조정 사유 *</label>
                  <input type="text" className="form-control" placeholder="예: 실사 반영, 입고, 파손 폐기" value={adjusting.reason}
                    onChange={(e) => setAdjusting({ ...adjusting, reason: e.target.value })} />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setAdjusting(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={saveAdjust}>저장</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {historyFor && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setHistoryFor(null)}>
          <div className="modal-dialog modal-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">재고 조정 이력 — {historyFor.name}</h6>
                <button type="button" className="btn-close" onClick={() => setHistoryFor(null)} /></div>
              <div className="modal-body">
                <table className="table table-sm">
                  <thead className="table-light">
                    <tr><th>일시</th><th className="text-end">변경 전</th><th className="text-end">변경 후</th><th>사유</th><th>처리자</th></tr>
                  </thead>
                  <tbody>
                    {historyFor.rows.map((h) => (
                      <tr key={h.id}>
                        <td>{h.createdAt?.replace('T', ' ').slice(0, 16)}</td>
                        <td className="text-end">{h.beforeQuantity}</td>
                        <td className="text-end fw-bold">{h.afterQuantity}</td>
                        <td>{h.reason}</td>
                        <td>{h.adjustedBy}</td>
                      </tr>
                    ))}
                    {historyFor.rows.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-3">조정 이력이 없습니다.</td></tr>}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
