import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import ProductCard from '../../components/ProductCard';

const SORTS = [
  ['newest', '신상품'], ['name', '상품명'], ['priceAsc', '낮은가격'], ['priceDesc', '높은가격'],
];

export default function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });

  const categoryId = searchParams.get('categoryId') || '';
  const keyword = searchParams.get('keyword') || '';
  const sort = searchParams.get('sort') || 'newest';
  const page = parseInt(searchParams.get('page') || '0');

  useEffect(() => {
    api.get('/categories').then((res) => setCategories(res.data));
  }, []);

  useEffect(() => {
    const params = { sort, page, size: 12 };
    if (categoryId) params.categoryId = categoryId;
    if (keyword) params.keyword = keyword;
    api.get('/products', { params }).then((res) => setData(res.data));
  }, [categoryId, keyword, sort, page]);

  const setParam = (extra) => setSearchParams({ categoryId, keyword, sort, page: 0, ...extra });
  const currentCat = categories.find((c) => String(c.id) === categoryId);

  return (
    <div className="container py-4">
      <h4 className="fw-bold text-center mb-1">{keyword ? `'${keyword}' 검색 결과` : (currentCat?.name || 'ALL PRODUCTS')}</h4>
      <p className="text-center text-muted small mb-4">{data.totalElements}개의 상품</p>

      <div className="d-flex justify-content-center flex-wrap gap-2 mb-3">
        <button className={`btn btn-sm ${!categoryId ? 'btn-brand' : 'btn-outline-brand'}`} onClick={() => setParam({ categoryId: '' })}>전체</button>
        {categories.map((c) => (
          <button key={c.id} className={`btn btn-sm ${String(c.id) === categoryId ? 'btn-brand' : 'btn-outline-brand'}`}
            onClick={() => setParam({ categoryId: String(c.id) })}>{c.name}</button>
        ))}
      </div>

      <div className="d-flex justify-content-end mb-3">
        <select className="form-select form-select-sm" style={{ width: 130 }} value={sort} onChange={(e) => setParam({ sort: e.target.value })}>
          {SORTS.map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>

      <div className="row g-4">
        {data.content.map((p) => (
          <div key={p.id} className="col-6 col-md-3"><ProductCard product={p} /></div>
        ))}
        {data.totalElements === 0 && <div className="text-center text-muted py-5">상품이 없습니다.</div>}
      </div>

      {data.totalPages > 1 && (
        <nav className="mt-4">
          <ul className="pagination pagination-sm justify-content-center">
            {[...Array(data.totalPages)].map((_, i) => (
              <li key={i} className={`page-item ${i === data.number ? 'active' : ''}`}>
                <button className="page-link" onClick={() => setSearchParams({ categoryId, keyword, sort, page: i })}>{i + 1}</button>
              </li>
            ))}
          </ul>
        </nav>
      )}
    </div>
  );
}
