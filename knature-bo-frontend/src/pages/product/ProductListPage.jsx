import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const STATUS_LABELS = { ON_SALE: '판매중', SOLD_OUT: '품절', HIDDEN: '숨김' };
const STATUS_COLORS = { ON_SALE: 'bg-success', SOLD_OUT: 'bg-danger', HIDDEN: 'bg-secondary' };

export default function ProductListPage() {
  const [products, setProducts] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const navigate = useNavigate();

  const page = parseInt(searchParams.get('page') || '0');
  const status = searchParams.get('status') || '';

  useEffect(() => {
    const params = { page, size: 20 };
    if (keyword) params.keyword = keyword;
    if (status) params.status = status;
    api.get('/products', { params }).then((res) => setProducts(res.data));
  }, [page, status, searchParams]);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearchParams({ keyword, status, page: 0 });
  };

  const fmt = (n) => n ? Number(n).toLocaleString() : '-';

  return (
    <>
      <TopBar title="상품 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3">
          <form className="d-flex gap-2" onSubmit={handleSearch}>
            <input type="text" className="form-control form-control-sm" placeholder="상품명 검색" value={keyword} onChange={(e) => setKeyword(e.target.value)} style={{ width: 200 }} />
            <select className="form-select form-select-sm" style={{ width: 130 }} value={status} onChange={(e) => setSearchParams({ keyword, status: e.target.value, page: 0 })}>
              <option value="">전체 상태</option>
              {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="btn btn-sm btn-outline-primary">검색</button>
          </form>
          <button className="btn btn-sm btn-primary" onClick={() => navigate('/products/new')}>+ 상품 등록</button>
        </div>

        <table className="table table-hover">
          <thead className="table-light">
            <tr>
              <th style={{ width: 60 }}>ID</th>
              <th>상품명</th>
              <th>카테고리</th>
              <th className="text-end">판매가</th>
              <th className="text-end">할인가</th>
              <th className="text-center">상태</th>
              <th className="text-center">등록일</th>
            </tr>
          </thead>
          <tbody>
            {products.content.map((p) => (
              <tr key={p.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/products/${p.id}`)}>
                <td>{p.id}</td>
                <td>{p.name}</td>
                <td>{p.category?.name || '-'}</td>
                <td className="text-end">{fmt(p.price)}원</td>
                <td className="text-end">{p.salePrice ? fmt(p.salePrice) + '원' : '-'}</td>
                <td className="text-center"><span className={`badge ${STATUS_COLORS[p.status]}`}>{STATUS_LABELS[p.status]}</span></td>
                <td className="text-center">{p.createdAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {products.totalElements === 0 && (
              <tr><td colSpan={7} className="text-center text-muted py-4">등록된 상품이 없습니다.</td></tr>
            )}
          </tbody>
        </table>

        {products.totalPages > 1 && (
          <nav>
            <ul className="pagination pagination-sm justify-content-center">
              {[...Array(products.totalPages)].map((_, i) => (
                <li key={i} className={`page-item ${i === products.number ? 'active' : ''}`}>
                  <button className="page-link" onClick={() => setSearchParams({ keyword, status, page: i })}>{i + 1}</button>
                </li>
              ))}
            </ul>
          </nav>
        )}
      </div>
    </>
  );
}
