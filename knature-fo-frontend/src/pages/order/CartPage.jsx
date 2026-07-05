import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api, { API_ORIGIN } from '../../api/client';
import { getCart, updateQuantity, removeItems } from '../../utils/cart';

const fmt = (n) => Number(n).toLocaleString();
const keyOf = (it) => `${it.productId}:${it.optionId ?? ''}`;

export default function CartPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState(getCart());
  const [checked, setChecked] = useState(items.map(keyOf));
  const [stockMap, setStockMap] = useState({}); // productId → {stock, status}

  // 서버 최신 재고/품절 상태 동기화
  useEffect(() => {
    const ids = [...new Set(items.map((it) => it.productId))];
    Promise.all(ids.map((pid) =>
      api.get(`/products/${pid}`).then((res) => [pid, { stock: res.data.stockQuantity ?? 0, status: res.data.status }])
        .catch(() => [pid, { stock: 0, status: 'HIDDEN' }])
    )).then((entries) => setStockMap(Object.fromEntries(entries)));
  }, []);

  const refresh = () => { const next = getCart(); setItems(next); setChecked((prev) => prev.filter((k) => next.some((it) => keyOf(it) === k))); };
  const isSoldOut = (it) => (stockMap[it.productId]?.status ?? '') === 'SOLD_OUT' || (stockMap[it.productId]?.stock ?? 1) === 0;
  const toggle = (k) => setChecked((prev) => prev.includes(k) ? prev.filter((x) => x !== k) : [...prev, k]);
  const toggleAll = (e) => setChecked(e.target.checked ? items.map(keyOf) : []);

  const changeQty = (it, q) => {
    const max = stockMap[it.productId]?.stock ?? 99;
    if (q > max) { alert(`재고가 부족합니다. (남은 재고 ${max}개)`); q = max; }
    updateQuantity(it.productId, it.optionId, Math.max(1, q));
    refresh();
  };

  const removeChecked = () => {
    if (checked.length === 0) return alert('삭제할 상품을 선택해주세요.');
    removeItems(items.filter((it) => checked.includes(keyOf(it))).map((it) => ({ productId: it.productId, optionId: it.optionId })));
    refresh();
  };

  const selectedItems = items.filter((it) => checked.includes(keyOf(it)) && !isSoldOut(it));
  const totalAmount = selectedItems.reduce((sum, it) => sum + it.unitPrice * it.quantity, 0);

  const goOrder = () => {
    if (selectedItems.length === 0) return alert('주문할 상품을 선택해주세요. (품절 상품 제외)');
    navigate('/order', { state: { items: selectedItems } });
  };

  return (
    <div className="container py-4" style={{ maxWidth: 900 }}>
      <h4 className="fw-bold text-center mb-4">장바구니</h4>

      {items.length === 0 ? (
        <div className="text-center py-5">
          <i className="bi bi-bag text-muted" style={{ fontSize: '3rem' }}></i>
          <p className="text-muted mt-3">장바구니가 비어 있습니다.</p>
          <Link to="/products" className="btn btn-brand px-4">쇼핑 계속하기</Link>
        </div>
      ) : (
        <>
          <div className="d-flex justify-content-between align-items-center mb-2">
            <div className="form-check">
              <input type="checkbox" className="form-check-input" id="all"
                checked={checked.length === items.length && items.length > 0} onChange={toggleAll} />
              <label className="form-check-label small" htmlFor="all">전체 선택</label>
            </div>
            <button className="btn btn-sm btn-outline-secondary" onClick={removeChecked}>선택 삭제</button>
          </div>

          {items.map((it) => {
            const sold = isSoldOut(it);
            return (
              <div key={keyOf(it)} className={`d-flex align-items-center gap-3 border rounded p-3 mb-2 ${sold ? 'opacity-50' : ''}`}>
                <input type="checkbox" className="form-check-input" checked={checked.includes(keyOf(it))} onChange={() => toggle(keyOf(it))} />
                <div className="product-thumb" style={{ width: 72, height: 72, flexShrink: 0 }}>
                  {it.imageUrl ? <img src={API_ORIGIN + it.imageUrl} alt="" /> : <i className="bi bi-droplet text-brand"></i>}
                </div>
                <div className="flex-grow-1">
                  <div className="fw-semibold">{it.name} {sold && <span className="badge bg-danger">SOLD OUT</span>}</div>
                  {it.optionName && <div className="small text-muted">옵션: {it.optionName}</div>}
                  <div className="small">{fmt(it.unitPrice)}원</div>
                </div>
                <div className="input-group input-group-sm" style={{ width: 110 }}>
                  <button className="btn btn-outline-secondary" disabled={sold} onClick={() => changeQty(it, it.quantity - 1)}>-</button>
                  <input className="form-control text-center" value={it.quantity} readOnly />
                  <button className="btn btn-outline-secondary" disabled={sold} onClick={() => changeQty(it, it.quantity + 1)}>+</button>
                </div>
                <div className="fw-bold text-end" style={{ width: 100 }}>{fmt(it.unitPrice * it.quantity)}원</div>
              </div>
            );
          })}

          <div className="bg-brand-light rounded p-3 mt-3 d-flex justify-content-between align-items-center">
            <div className="small text-muted">상품금액 {fmt(totalAmount)}원 + 배송비 0원 (무료)</div>
            <div className="fs-5 fw-bold">결제 예정 금액: <span className="text-brand">{fmt(totalAmount)}원</span></div>
          </div>

          <div className="d-flex gap-2 mt-3">
            <Link to="/products" className="btn btn-outline-brand flex-fill py-2">쇼핑 계속하기</Link>
            <button className="btn btn-brand flex-fill py-2" onClick={goOrder}>선택 상품 주문하기 ({selectedItems.length})</button>
          </div>
        </>
      )}
    </div>
  );
}
