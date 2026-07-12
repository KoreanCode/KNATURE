import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import { downloadFile } from '../../utils/download';

const fmt = (n) => Number(n).toLocaleString();
const STATUS_LABELS = { REQUESTED: '승인대기', APPROVED: '승인', CONFIRMED: '공장확인', IN_PRODUCTION: '생산중', SHIPPED: '출고', RECEIVED: '입고완료', REJECTED: '반려', CANCELLED: '취소' };
const STATUS_COLORS = { REQUESTED: 'bg-warning text-dark', APPROVED: 'bg-primary', CONFIRMED: 'bg-info', IN_PRODUCTION: 'bg-info', SHIPPED: 'bg-success', RECEIVED: 'bg-secondary', REJECTED: 'bg-danger', CANCELLED: 'bg-secondary' };
const EMPTY = { factoryId: '', productId: '', quantity: '', dueDate: '', memo: '' };

export default function PurchaseOrderPage() {
  const [me, setMe] = useState(null);
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState('');
  const [factories, setFactories] = useState([]);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(null);
  const [receiving, setReceiving] = useState(null); // {po, receivedQuantity, goodQuantity, defectQuantity, defectReason, lotNumber, expiryDate}

  const isSuper = me?.role === 'SUPER_ADMIN';

  const load = () => {
    const params = {};
    if (status) params.status = status;
    api.get('/purchase-orders', { params }).then((res) => setOrders(res.data)).catch(() => {});
  };

  useEffect(() => {
    api.get('/auth/me').then((res) => {
      setMe(res.data);
      if (res.data.role === 'SUPER_ADMIN') {
        api.get('/factories').then((r) => setFactories(r.data));
        api.get('/products', { params: { size: 60 } }).then((r) => setProducts(r.data.content));
      }
    });
  }, []);
  useEffect(load, [status]);

  const create = () => {
    api.post('/purchase-orders', form)
      .then(() => { alert('발주가 생성되었습니다. (승인대기)'); setForm(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '생성에 실패했습니다.'));
  };

  const autoRun = () => {
    api.post('/purchase-orders/auto-run', {})
      .then((res) => { alert(res.data.message); load(); })
      .catch((err) => alert(err.response?.data?.message || '실행에 실패했습니다.'));
  };

  const changeStatus = (po, to) => {
    api.patch(`/purchase-orders/${po.id}/status`, { status: to })
      .then((res) => { alert(res.data.message); load(); })
      .catch((err) => alert(err.response?.data?.message || '상태 변경에 실패했습니다.'));
  };

  const submitReceive = () => {
    api.post(`/purchase-orders/${receiving.po.id}/receive`, {
      receivedQuantity: receiving.receivedQuantity,
      goodQuantity: receiving.goodQuantity,
      defectQuantity: receiving.defectQuantity || '0',
      defectReason: receiving.defectReason,
      lotNumber: receiving.lotNumber,
      expiryDate: receiving.expiryDate,
    })
      .then((res) => { alert(res.data.message); setReceiving(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '입고 등록에 실패했습니다.'));
  };

  /** 역할·상태별 가능한 액션 버튼 */
  const actions = (po) => {
    const btns = [];
    if (isSuper) {
      if (po.status === 'REQUESTED') {
        btns.push(['승인', 'APPROVED', 'btn-primary'], ['반려', 'REJECTED', 'btn-outline-danger']);
      }
      if (['REQUESTED', 'APPROVED'].includes(po.status)) {
        btns.push(['취소', 'CANCELLED', 'btn-outline-secondary']);
      }
    } else {
      if (po.status === 'APPROVED') btns.push(['공장확인', 'CONFIRMED', 'btn-info']);
      if (po.status === 'CONFIRMED') btns.push(['생산시작', 'IN_PRODUCTION', 'btn-info']);
      if (po.status === 'IN_PRODUCTION') btns.push(['출고통보', 'SHIPPED', 'btn-success']);
    }
    return btns;
  };

  const dueBadge = (po) => {
    if (!po.dueDate || ['RECEIVED', 'REJECTED', 'CANCELLED'].includes(po.status)) return null;
    const days = Math.ceil((new Date(po.dueDate) - new Date()) / 86400000);
    if (days < 0) return <span className="badge bg-danger ms-1">D+{-days}</span>;
    if (days <= 3) return <span className="badge bg-warning text-dark ms-1">D-{days}</span>;
    return null;
  };

  return (
    <>
      <TopBar title={`발주 관리${me?.factoryName ? ` — ${me.factoryName}` : ''}`} />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3 flex-wrap gap-2">
          <select className="form-select form-select-sm" style={{ width: 140 }} value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">전체 상태</option>
            {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          {isSuper && (
            <div className="d-flex gap-2">
              <button className="btn btn-sm btn-outline-success" onClick={autoRun}>
                <i className="bi bi-lightning"></i> 자동발주 실행
              </button>
              <button className="btn btn-sm btn-primary" onClick={() => setForm({ ...EMPTY })}>+ 수동 발주</button>
            </div>
          )}
        </div>

        <table className="table table-hover">
          <thead className="table-light">
            <tr><th>발주번호</th><th>공장</th><th>상품</th><th className="text-end">수량</th><th className="text-end">총액</th><th className="text-center">납기</th><th className="text-center">상태</th><th className="text-center" style={{ width: 230 }}>처리</th></tr>
          </thead>
          <tbody>
            {orders.map((po) => (
              <tr key={po.id}>
                <td className="small">
                  {po.poNumber}{po.createdBy === 'auto' && <span className="badge bg-warning text-dark ms-1">자동</span>}
                  {po.memo && <div className="text-muted" style={{ fontSize: 11 }}>{po.memo}</div>}
                </td>
                <td>{po.factory?.name}</td>
                <td className="small">{po.product?.name}</td>
                <td className="text-end">{fmt(po.quantity)}개{po.receivedQuantity > 0 && <div className="small text-muted">입고 {po.receivedQuantity}</div>}</td>
                <td className="text-end">{fmt(po.totalCost)}원</td>
                <td className="text-center small">{po.dueDate || '-'}{dueBadge(po)}</td>
                <td className="text-center"><span className={`badge ${STATUS_COLORS[po.status]}`}>{STATUS_LABELS[po.status]}</span></td>
                <td className="text-center">
                  {actions(po).map(([label, to, cls]) => (
                    <button key={to} className={`btn btn-sm ${cls} py-0 me-1`} onClick={() => changeStatus(po, to)}>{label}</button>
                  ))}
                  {isSuper && po.status === 'SHIPPED' && (
                    <button className="btn btn-sm btn-success py-0 me-1"
                      onClick={() => setReceiving({ po, receivedQuantity: po.quantity - po.receivedQuantity, goodQuantity: po.quantity - po.receivedQuantity, defectQuantity: 0, defectReason: '', lotNumber: '', expiryDate: '' })}>입고</button>
                  )}
                  {isSuper && (
                    <button className="btn btn-sm btn-outline-secondary py-0"
                      onClick={() => downloadFile(`/purchase-orders/${po.id}/excel`, `발주서_${po.poNumber}.xlsx`)}>발주서</button>
                  )}
                </td>
              </tr>
            ))}
            {orders.length === 0 && <tr><td colSpan={8} className="text-center text-muted py-4">발주가 없습니다.</td></tr>}
          </tbody>
        </table>
        {!isSuper && <small className="text-muted">소속 공장의 발주만 표시됩니다. 공장확인 → 생산시작 → 출고통보 순서로 진행해주세요.</small>}
      </div>

      {form && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setForm(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">수동 발주 생성</h6>
                <button type="button" className="btn-close" onClick={() => setForm(null)} /></div>
              <div className="modal-body">
                <select className="form-select form-select-sm mb-2" value={form.factoryId}
                  onChange={(e) => setForm({ ...form, factoryId: e.target.value })}>
                  <option value="">공장 선택 *</option>
                  {factories.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
                </select>
                <select className="form-select form-select-sm mb-2" value={form.productId}
                  onChange={(e) => setForm({ ...form, productId: e.target.value })}>
                  <option value="">상품 선택 *</option>
                  {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
                <input type="number" className="form-control form-control-sm mb-2" placeholder="발주 수량 *"
                  value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
                <label className="form-label small mb-0">납기 요청일 (비우면 리드타임 자동 계산)</label>
                <input type="date" className="form-control form-control-sm mb-2" value={form.dueDate}
                  onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
                <input className="form-control form-control-sm" placeholder="비고"
                  value={form.memo} onChange={(e) => setForm({ ...form, memo: e.target.value })} />
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setForm(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={create}>발주 생성</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {receiving && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setReceiving(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">입고 등록 — {receiving.po.poNumber}</h6>
                <button type="button" className="btn-close" onClick={() => setReceiving(null)} /></div>
              <div className="modal-body">
                <p className="small text-muted">{receiving.po.product?.name} · 발주 {receiving.po.quantity}개 (기입고 {receiving.po.receivedQuantity}개)</p>
                <div className="row g-2 mb-2">
                  <div className="col-4"><label className="form-label small mb-0">입고 수량 *</label>
                    <input type="number" className="form-control form-control-sm" value={receiving.receivedQuantity}
                      onChange={(e) => setReceiving({ ...receiving, receivedQuantity: e.target.value })} /></div>
                  <div className="col-4"><label className="form-label small mb-0">양품 *</label>
                    <input type="number" className="form-control form-control-sm" value={receiving.goodQuantity}
                      onChange={(e) => setReceiving({ ...receiving, goodQuantity: e.target.value })} /></div>
                  <div className="col-4"><label className="form-label small mb-0">불량</label>
                    <input type="number" className="form-control form-control-sm" value={receiving.defectQuantity}
                      onChange={(e) => setReceiving({ ...receiving, defectQuantity: e.target.value })} /></div>
                </div>
                <input className="form-control form-control-sm mb-2" placeholder="불량 사유 (불량 시)"
                  value={receiving.defectReason} onChange={(e) => setReceiving({ ...receiving, defectReason: e.target.value })} />
                <div className="row g-2">
                  <div className="col-6"><label className="form-label small mb-0">LOT 번호</label>
                    <input className="form-control form-control-sm" placeholder="예: L20260712-01" value={receiving.lotNumber}
                      onChange={(e) => setReceiving({ ...receiving, lotNumber: e.target.value })} /></div>
                  <div className="col-6"><label className="form-label small mb-0">유통기한</label>
                    <input type="date" className="form-control form-control-sm" value={receiving.expiryDate}
                      onChange={(e) => setReceiving({ ...receiving, expiryDate: e.target.value })} /></div>
                </div>
                <small className="text-muted d-block mt-2">양품은 본사 재고에 자동 반영되며, 품절 상품은 판매중으로 해제됩니다.</small>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setReceiving(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={submitReceive}>입고 확정</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
