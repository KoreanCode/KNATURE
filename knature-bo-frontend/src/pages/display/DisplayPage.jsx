import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const SECTIONS = [
  ['MD_CHOICE', 'MD CHOICE'],
  ['MONTH_BEST', 'MONTH BEST'],
];

const fmt = (n) => Number(n).toLocaleString();

export default function DisplayPage() {
  const [displays, setDisplays] = useState([]);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState({ section: 'MD_CHOICE', productId: '', sortOrder: '' });

  const load = () => api.get('/displays').then((res) => setDisplays(res.data));
  useEffect(() => {
    load();
    api.get('/products', { params: { size: 60 } }).then((res) => setProducts(res.data.content));
  }, []);

  const add = () => {
    api.post('/displays', form)
      .then(() => { setForm((p) => ({ ...p, productId: '', sortOrder: '' })); load(); })
      .catch((err) => alert(err.response?.data?.message || '진열 추가에 실패했습니다.'));
  };

  const changeOrder = (d) => {
    const v = prompt('정렬 순서 (숫자가 작을수록 앞)', d.sortOrder);
    if (v === null) return;
    api.put(`/displays/${d.id}`, { sortOrder: v }).then(load)
      .catch((err) => alert(err.response?.data?.message || '변경에 실패했습니다.'));
  };

  const remove = (d) => {
    if (!confirm(`"${d.product?.name}"을(를) ${d.section} 진열에서 제외할까요?`)) return;
    api.delete(`/displays/${d.id}`).then(load);
  };

  return (
    <>
      <TopBar title="진열 관리" />
      <div className="content-card mb-3">
        <div className="d-flex gap-2 align-items-center bg-light rounded p-2">
          <select className="form-select form-select-sm" style={{ width: 160 }} value={form.section}
            onChange={(e) => setForm((p) => ({ ...p, section: e.target.value }))}>
            {SECTIONS.map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <select className="form-select form-select-sm" style={{ maxWidth: 260 }} value={form.productId}
            onChange={(e) => setForm((p) => ({ ...p, productId: e.target.value }))}>
            <option value="">진열할 상품 선택</option>
            {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
          <input type="number" className="form-control form-control-sm" style={{ width: 110 }} placeholder="정렬순서"
            min="0" max="999" value={form.sortOrder}
            onChange={(e) => setForm((p) => ({ ...p, sortOrder: e.target.value }))} />
          <button className="btn btn-sm btn-primary flex-shrink-0" onClick={add} disabled={!form.productId}>진열 추가</button>
        </div>
        <small className="text-muted">FO 메인 페이지의 MD CHOICE / MONTH BEST 섹션에 노출됩니다. (숨김·미노출 상품은 자동 제외)</small>
      </div>

      {SECTIONS.map(([key, label]) => {
        const items = displays.filter((d) => d.section === key);
        return (
          <div className="content-card mb-3" key={key}>
            <h6 className="mb-3">{label} <span className="text-muted small">({items.length}개)</span></h6>
            <table className="table table-hover mb-0">
              <thead className="table-light">
                <tr><th style={{ width: 80 }}>순서</th><th>상품</th><th className="text-end">판매가</th>
                  <th className="text-center">상태</th><th className="text-center" style={{ width: 160 }}>관리</th></tr>
              </thead>
              <tbody>
                {items.map((d) => (
                  <tr key={d.id}>
                    <td>{d.sortOrder}</td>
                    <td className="fw-semibold">{d.product?.name}</td>
                    <td className="text-end">{fmt(d.product?.salePrice ?? d.product?.price ?? 0)}원</td>
                    <td className="text-center">
                      {d.product?.displayed && d.product?.status !== 'HIDDEN'
                        ? <span className="badge bg-success">노출중</span>
                        : <span className="badge bg-secondary">미노출(FO 제외)</span>}
                    </td>
                    <td className="text-center">
                      <button className="btn btn-sm btn-outline-primary py-0 me-1" onClick={() => changeOrder(d)}>순서</button>
                      <button className="btn btn-sm btn-outline-danger py-0" onClick={() => remove(d)}>제외</button>
                    </td>
                  </tr>
                ))}
                {items.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-3">진열된 상품이 없습니다.</td></tr>}
              </tbody>
            </table>
          </div>
        );
      })}
    </>
  );
}
