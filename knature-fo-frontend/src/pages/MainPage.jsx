import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import ProductCard from '../components/ProductCard';

const BANNERS = [
  { title: 'NATURE IN SKIN', subtitle: '자연에서 온 순한 스킨케어, KNATURE', cta: '전체 상품 보기', link: '/products' },
  { title: 'HYDRA CALMING', subtitle: '민감한 피부를 위한 수분 진정 라인', cta: '라인 보러가기', link: '/products' },
];

const SECTION_LABELS = { MD_CHOICE: 'MD CHOICE', MONTH_BEST: 'MONTH BEST' };
const SECTION_SUBTITLES = { MD_CHOICE: 'MD가 추천하는 이달의 제품', MONTH_BEST: '이번 달 가장 사랑받은 제품' };

export default function MainPage() {
  const [categories, setCategories] = useState([]);
  const [activeCat, setActiveCat] = useState(null); // null = 전체
  const [products, setProducts] = useState([]);
  const [sections, setSections] = useState({});
  const [banner, setBanner] = useState(0);

  useEffect(() => {
    api.get('/categories').then((res) => setCategories(res.data));
    api.get('/display-sections').then((res) => setSections(res.data)).catch(() => {});
    const timer = setInterval(() => setBanner((b) => (b + 1) % BANNERS.length), 10000); // 자동 10초
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const params = { size: 8 };
    if (activeCat) params.categoryId = activeCat;
    api.get('/products', { params }).then((res) => setProducts(res.data.content));
  }, [activeCat]);

  const b = BANNERS[banner];

  return (
    <div className="container py-4">
      {/* 메인 배너 슬라이드 */}
      <div className="main-banner mb-3">
        <h1 className="display-5 fw-bold" style={{ letterSpacing: 6 }}>{b.title}</h1>
        <p className="fs-5 opacity-75 mb-4">{b.subtitle}</p>
        <Link to={b.link} className="btn btn-light px-4">{b.cta}</Link>
      </div>
      <div className="d-flex justify-content-center gap-2 mb-5">
        {BANNERS.map((_, i) => (
          <button key={i} onClick={() => setBanner(i)} aria-label={`배너 ${i + 1}`}
            className="border-0 rounded-circle p-0"
            style={{ width: 10, height: 10, background: i === banner ? 'var(--brand)' : '#ccc' }} />
        ))}
      </div>

      {/* 진열 섹션 — BO 진열 관리 연동 (MD CHOICE / MONTH BEST) */}
      {Object.entries(SECTION_LABELS).map(([key, label]) => {
        const items = sections[key] || [];
        if (items.length === 0) return null;
        return (
          <div key={key} className="mb-5">
            <div className="text-center mb-4">
              <h4 className="fw-bold">{label}</h4>
              <p className="text-muted small mb-0">{SECTION_SUBTITLES[key]}</p>
            </div>
            <div className="row g-4">
              {items.map((p) => (
                <div key={p.id} className="col-6 col-md-3">
                  <ProductCard product={p} />
                </div>
              ))}
            </div>
          </div>
        );
      })}

      {/* 상품 섹션 — 라인별 탭 */}
      <div className="text-center mb-4">
        <h4 className="fw-bold">PRODUCT LINE</h4>
        <div className="d-flex justify-content-center flex-wrap gap-2 mt-3">
          <button className={`btn btn-sm ${activeCat === null ? 'btn-brand' : 'btn-outline-brand'}`}
            onClick={() => setActiveCat(null)}>전체</button>
          {categories.map((c) => (
            <button key={c.id} className={`btn btn-sm ${activeCat === c.id ? 'btn-brand' : 'btn-outline-brand'}`}
              onClick={() => setActiveCat(c.id)}>{c.name}</button>
          ))}
        </div>
      </div>

      <div className="row g-4">
        {products.map((p) => (
          <div key={p.id} className="col-6 col-md-3">
            <ProductCard product={p} />
          </div>
        ))}
        {products.length === 0 && <div className="text-center text-muted py-5">등록된 상품이 없습니다.</div>}
      </div>
    </div>
  );
}
