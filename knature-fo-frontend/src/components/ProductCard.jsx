import { Link } from 'react-router-dom';
import { API_ORIGIN } from '../api/client';

const fmt = (n) => Number(n).toLocaleString();

export default function ProductCard({ product }) {
  const soldOut = product.status === 'SOLD_OUT';
  const mainImage = (product.images || []).find((img) => img.isMain) || (product.images || [])[0];
  const hasDiscount = product.salePrice && product.salePrice > 0 && product.salePrice < product.price;

  return (
    <Link to={`/products/${product.id}`} className="card product-card h-100">
      <div className="product-thumb">
        {mainImage
          ? <img src={API_ORIGIN + mainImage.imageUrl} alt={product.name} />
          : <i className="bi bi-droplet fs-1 text-brand opacity-50"></i>}
        {soldOut && <div className="sold-badge">SOLD OUT</div>}
      </div>
      <div className="card-body px-1 py-2">
        <div className="small text-muted">{product.category?.name}</div>
        <div className="fw-semibold">{product.name}</div>
        <div className="mt-1">
          {hasDiscount ? (
            <>
              <span className="text-muted text-decoration-line-through small me-2">{fmt(product.price)}원</span>
              <span className="fw-bold text-brand">{fmt(product.salePrice)}원</span>
            </>
          ) : (
            <span className="fw-bold">{fmt(product.price)}원</span>
          )}
        </div>
        <div className="small text-muted mt-1">무료배송 · 2~4일 소요</div>
      </div>
    </Link>
  );
}
