import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import Pagination from '../../components/Pagination';

const STATUS_LABELS = { ON_SALE: '판매중', SOLD_OUT: '품절', HIDDEN: '숨김' };
const STATUS_COLORS = { ON_SALE: 'bg-success', SOLD_OUT: 'bg-danger', HIDDEN: 'bg-secondary' };

export default function ProductListPage() {
  const [products, setProducts] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [categories, setCategories] = useState([]);
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [selected, setSelected] = useState([]);
  const [bulkStatus, setBulkStatus] = useState('ON_SALE');
  const navigate = useNavigate();

  const page = parseInt(searchParams.get('page') || '0');
  const status = searchParams.get('status') || '';
  const categoryId = searchParams.get('categoryId') || '';

  const load = () => {
    const params = { page, size: 20 };
    if (keyword) params.keyword = keyword;
    if (status) params.status = status;
    if (categoryId) params.categoryId = categoryId;
    api.get('/products', { params }).then((res) => { setProducts(res.data); setSelected([]); });
  };

  useEffect(load, [page, status, categoryId, searchParams]);
  useEffect(() => { api.get('/products/categories').then((res) => setCategories(res.data)); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearchParams({ keyword, status, categoryId, page: 0 });
  };

  const fmt = (n) => n ? Number(n).toLocaleString() : '-';

  const toggleAll = (e) => setSelected(e.target.checked ? products.content.map((p) => p.id) : []);
  const toggleOne = (id) => setSelected((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

  const applyBulk = () => {
    if (selected.length === 0) { alert('상품을 선택해주세요.'); return; }
    api.patch('/products/bulk-status', { ids: selected, status: bulkStatus })
      .then((res) => { alert(res.data.message); load(); });
  };

  return (
    <>
      <TopBar title="상품 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3 flex-wrap gap-2">
          <form className="d-flex gap-2 flex-wrap" onSubmit={handleSearch}>
            <input type="text" className="form-control form-control-sm" placeholder="상품명 검색" value={keyword} maxLength={50} onChange={(e) => setKeyword(e.target.value)} style={{ width: 180 }} />
            <select className="form-select form-select-sm" style={{ width: 170 }} value={categoryId} onChange={(e) => setSearchParams({ keyword, status, categoryId: e.target.value, page: 0 })}>
              <option value="">전체 분류</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select className="form-select form-select-sm" style={{ width: 120 }} value={status} onChange={(e) => setSearchParams({ keyword, status: e.target.value, categoryId, page: 0 })}>
              <option value="">전체 상태</option>
              {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="btn btn-sm btn-outline-primary">검색</button>
          </form>
          <div className="d-flex gap-2">
            <button className="btn btn-sm btn-outline-secondary" onClick={() => navigate('/products/categories-manage')}>분류 관리</button>
            <button className="btn btn-sm btn-primary" onClick={() => navigate('/products/new')}>+ 상품 등록</button>
          </div>
        </div>

        {selected.length > 0 && (
          <div className="d-flex gap-2 align-items-center mb-2 p-2 bg-light rounded">
            <span className="fw-bold">{selected.length}개 선택</span>
            <select className="form-select form-select-sm" style={{ width: 120 }} value={bulkStatus} onChange={(e) => setBulkStatus(e.target.value)}>
              {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="btn btn-sm btn-warning" onClick={applyBulk}>상태 일괄 변경</button>
          </div>
        )}

        <table className="table table-hover">
          <thead className="table-light">
            <tr>
              <th style={{ width: 36 }}>
                <input type="checkbox" className="form-check-input"
                  checked={products.content.length > 0 && selected.length === products.content.length}
                  onChange={toggleAll} />
              </th>
              <th style={{ width: 60 }}>번호</th>
              <th>상품명</th>
              <th>카테고리</th>
              <th className="text-end">판매가</th>
              <th className="text-end">할인가</th>
              <th className="text-end">재고</th>
              <th className="text-center">상태</th>
              <th className="text-center">등록일</th>
            </tr>
          </thead>
          <tbody>
            {products.content.map((p, i) => (
              <tr key={p.id}>
                <td onClick={(e) => e.stopPropagation()}>
                  <input type="checkbox" className="form-check-input" checked={selected.includes(p.id)} onChange={() => toggleOne(p.id)} />
                </td>
                <td style={{ cursor: 'pointer' }} onClick={() => navigate(`/products/${p.id}/edit`)}>{products.number * 20 + i + 1}</td>
                <td style={{ cursor: 'pointer' }} onClick={() => navigate(`/products/${p.id}/edit`)}>{p.name}</td>
                <td>{p.category?.name || '-'}</td>
                <td className="text-end">{fmt(p.price)}원</td>
                <td className="text-end">{p.salePrice ? fmt(p.salePrice) + '원' : '-'}</td>
                <td className="text-end">{(p.stockQuantity ?? 0).toLocaleString()}</td>
                <td className="text-center"><span className={`badge ${STATUS_COLORS[p.status]}`}>{STATUS_LABELS[p.status]}</span></td>
                <td className="text-center">{p.createdAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {products.totalElements === 0 && (
              <tr><td colSpan={9} className="text-center text-muted py-4">등록된 상품이 없습니다.</td></tr>
            )}
          </tbody>
        </table>

        <Pagination
          totalPages={products.totalPages}
          page={products.number}
          onChange={(p) => setSearchParams({ keyword, status, categoryId, page: p })}
        />
      </div>
    </>
  );
}
