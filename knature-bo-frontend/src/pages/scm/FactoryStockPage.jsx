import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

export default function FactoryStockPage() {
  const [me, setMe] = useState(null);
  const [factories, setFactories] = useState([]);
  const [factoryId, setFactoryId] = useState('');
  const [stocks, setStocks] = useState([]);
  const [products, setProducts] = useState([]);
  const [edit, setEdit] = useState({ productId: '', quantity: '' });

  const isSuper = me?.role === 'SUPER_ADMIN';

  useEffect(() => {
    api.get('/auth/me').then((res) => {
      setMe(res.data);
      if (res.data.role === 'SUPER_ADMIN') {
        api.get('/factories').then((r) => setFactories(r.data));
      } else {
        setFactoryId(String(res.data.factoryId || ''));
      }
    });
    api.get('/products', { params: { size: 60 } }).then((r) => setProducts(r.data.content)).catch(() => {});
  }, []);

  const load = () => {
    const params = {};
    if (factoryId) params.factoryId = factoryId;
    api.get('/factory-stocks', { params }).then((res) => setStocks(res.data)).catch(() => {});
  };
  useEffect(load, [factoryId]);

  const save = () => {
    const targetFactory = isSuper ? factoryId : me?.factoryId;
    if (!targetFactory || !edit.productId || edit.quantity === '') { alert('공장/상품/수량을 입력해주세요.'); return; }
    api.put('/factory-stocks', { factoryId: String(targetFactory), productId: edit.productId, quantity: edit.quantity })
      .then((res) => { alert(res.data.message); setEdit({ productId: '', quantity: '' }); load(); })
      .catch((err) => alert(err.response?.data?.message || '저장에 실패했습니다.'));
  };

  return (
    <>
      <TopBar title={`공장 재고${me?.factoryName ? ` — ${me.factoryName}` : ''}`} />
      <div className="content-card">
        {isSuper && (
          <select className="form-select form-select-sm mb-3" style={{ width: 220 }} value={factoryId}
            onChange={(e) => setFactoryId(e.target.value)}>
            <option value="">전체 공장</option>
            {factories.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
          </select>
        )}

        <div className="d-flex gap-1 align-items-center bg-light rounded p-2 mb-3">
          <span className="small fw-bold me-1">재고 입력:</span>
          <select className="form-select form-select-sm" style={{ maxWidth: 220 }} value={edit.productId}
            onChange={(e) => setEdit({ ...edit, productId: e.target.value })}>
            <option value="">상품 선택</option>
            {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
          <input type="number" min="0" className="form-control form-control-sm" style={{ width: 110 }} placeholder="수량"
            value={edit.quantity} onChange={(e) => setEdit({ ...edit, quantity: e.target.value })} />
          <button className="btn btn-sm btn-primary flex-shrink-0" onClick={save}>저장</button>
          {isSuper && !factoryId && <small className="text-muted ms-2">(입력하려면 공장을 먼저 선택)</small>}
        </div>

        <table className="table table-hover">
          <thead className="table-light">
            <tr><th>공장</th><th>상품</th><th className="text-end">보유 재고</th><th className="text-center">갱신일</th></tr>
          </thead>
          <tbody>
            {stocks.map((s) => (
              <tr key={s.id}>
                <td>{s.factory?.name}</td>
                <td>{s.product?.name}</td>
                <td className="text-end fw-bold">{Number(s.quantity).toLocaleString()}개</td>
                <td className="text-center small">{s.updatedAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {stocks.length === 0 && <tr><td colSpan={4} className="text-center text-muted py-4">등록된 공장 재고가 없습니다.</td></tr>}
          </tbody>
        </table>
        <small className="text-muted">입고(출고통보 후 입고 확정) 시 해당 수량만큼 공장 재고가 자동 차감됩니다.</small>
      </div>
    </>
  );
}
