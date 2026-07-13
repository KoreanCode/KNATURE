import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { API_ORIGIN } from '../../api/client';
import MyLayout from './MyLayout';

const fmt = (n) => Number(n).toLocaleString();

export default function MyWishlistPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);

  const load = () => api.get('/wishlist').then((res) => setItems(res.data)).catch(() => setItems([]));
  useEffect(() => { load(); }, []);

  const remove = async (productId) => {
    if (!confirm('위시리스트에서 삭제하시겠습니까?')) return;
    try {
      await api.post('/wishlist/toggle', { productId });
      load();
    } catch (err) {
      alert(err.response?.data?.message || '삭제에 실패했습니다.');
    }
  };

  return (
    <MyLayout title="위시리스트">
      {items.length === 0 && <p className="text-muted text-center py-5">위시리스트에 담긴 상품이 없습니다.</p>}
      {items.map((w) => {
        const p = w.product || w;
        const productId = p.id ?? w.productId;
        const imageUrl = p.thumbnailUrl || p.imageUrl || p.images?.[0]?.imageUrl;
        const price = p.displayPrice ?? p.salePrice ?? p.price;
        return (
          <div key={w.id ?? productId} className="d-flex align-items-center gap-3 border-bottom py-3">
            <div className="rounded overflow-hidden bg-brand-light d-flex align-items-center justify-content-center flex-shrink-0"
              style={{ width: 72, height: 72, cursor: 'pointer' }} onClick={() => navigate(`/products/${productId}`)}>
              {imageUrl
                ? <img src={API_ORIGIN + imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                : <i className="bi bi-droplet text-brand opacity-50 fs-3"></i>}
            </div>
            <div className="flex-grow-1" style={{ cursor: 'pointer' }} onClick={() => navigate(`/products/${productId}`)}>
              <div className="fw-semibold">{p.name}</div>
              <div className="text-brand fw-bold">{fmt(price)}원</div>
            </div>
            <button className="btn btn-sm btn-outline-secondary flex-shrink-0" onClick={() => remove(productId)}>삭제</button>
          </div>
        );
      })}
    </MyLayout>
  );
}
