import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api, { API_ORIGIN } from '../../api/client';
import { addToCart } from '../../utils/cart';

const fmt = (n) => Number(n).toLocaleString();

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [imageIdx, setImageIdx] = useState(0);
  const [optionId, setOptionId] = useState('');
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    api.get(`/products/${id}`).then((res) => setProduct(res.data)).catch(() => navigate('/products'));
  }, [id]);

  if (!product) return <div className="container py-5 text-center">로딩중...</div>;

  const soldOut = product.status === 'SOLD_OUT';
  const stock = product.stockQuantity ?? 0;
  const images = product.images || [];
  const options = product.options || [];
  const selectedOption = options.find((o) => String(o.id) === optionId);
  const unitPrice = product.displayPrice + (selectedOption?.additionalPrice || 0);
  const total = unitPrice * quantity;

  const setQty = (q) => setQuantity(Math.min(Math.max(1, q), Math.max(stock, 1)));

  const buildCartItem = () => ({
    productId: product.id,
    optionId: selectedOption ? selectedOption.id : null,
    name: product.name,
    optionName: selectedOption ? selectedOption.name : null,
    unitPrice,
    imageUrl: images[0]?.imageUrl || null,
    quantity,
  });

  const validate = () => {
    if (soldOut) { alert('품절된 상품입니다.'); return false; }
    if (options.length > 0 && !selectedOption) { alert('옵션을 선택해주세요.'); return false; }
    if (quantity > stock) { alert(`재고가 부족합니다. (남은 재고 ${stock}개)`); return false; }
    return true;
  };

  const toCart = () => {
    if (!validate()) return;
    addToCart(buildCartItem());
    if (confirm('장바구니에 담았습니다. 장바구니로 이동할까요?')) navigate('/cart');
  };

  const buyNow = () => {
    if (!validate()) return;
    navigate('/order', { state: { items: [buildCartItem()] } });
  };

  return (
    <div className="container py-4">
      <div className="row g-5">
        {/* 이미지 갤러리 */}
        <div className="col-md-6">
          <div className="product-thumb mb-2" style={{ maxHeight: 480 }}>
            {images[imageIdx]
              ? <img src={API_ORIGIN + images[imageIdx].imageUrl} alt={product.name} />
              : <i className="bi bi-droplet text-brand opacity-50" style={{ fontSize: '5rem' }}></i>}
            {soldOut && <div className="sold-badge">SOLD OUT</div>}
          </div>
          {images.length > 1 && (
            <div className="d-flex gap-2">
              {images.map((img, i) => (
                <button key={i} onClick={() => setImageIdx(i)}
                  className={`border rounded p-0 overflow-hidden ${i === imageIdx ? 'border-success' : ''}`}
                  style={{ width: 64, height: 64 }}>
                  <img src={API_ORIGIN + img.imageUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 상품 정보 */}
        <div className="col-md-6">
          <div className="small text-muted">{product.category?.name}</div>
          <h4 className="fw-bold">{product.name}</h4>
          <p className="text-muted small">{product.description}</p>

          <div className="border-top border-bottom py-3 my-3">
            {product.salePrice && product.salePrice < product.price ? (
              <div>
                <span className="text-muted text-decoration-line-through me-2">{fmt(product.price)}원</span>
                <span className="fs-4 fw-bold text-brand">{fmt(product.salePrice)}원</span>
                <span className="badge bg-danger ms-2">{product.discountRate}%</span>
              </div>
            ) : (
              <div className="fs-4 fw-bold">{fmt(product.price)}원</div>
            )}
            <div className="small text-muted mt-2">배송비 무료 · 주문 후 2~4일 소요</div>
          </div>

          {options.length > 0 && (
            <select className="form-select mb-2" value={optionId} onChange={(e) => setOptionId(e.target.value)}>
              <option value="">옵션 선택 *</option>
              {options.map((o) => (
                <option key={o.id} value={o.id}>
                  {o.name}{o.additionalPrice > 0 ? ` (+${fmt(o.additionalPrice)}원)` : ''}
                </option>
              ))}
            </select>
          )}

          <div className="d-flex align-items-center gap-2 mb-3">
            <span className="small">수량</span>
            <div className="input-group input-group-sm" style={{ width: 130 }}>
              <button className="btn btn-outline-secondary" onClick={() => setQty(quantity - 1)}>-</button>
              <input type="number" className="form-control text-center" value={quantity} min="1"
                onChange={(e) => setQty(parseInt(e.target.value) || 1)} />
              <button className="btn btn-outline-secondary" onClick={() => setQty(quantity + 1)}>+</button>
            </div>
            {!soldOut && stock <= 5 && <span className="small text-danger">남은 재고 {stock}개</span>}
          </div>

          <div className="d-flex justify-content-between border-top pt-3 mb-3">
            <span>총 상품금액</span>
            <span className="fs-5 fw-bold text-brand">{fmt(total)}원</span>
          </div>

          <div className="d-flex gap-2">
            <button className="btn btn-outline-brand flex-fill py-2" onClick={toCart} disabled={soldOut}>장바구니</button>
            <button className="btn btn-brand flex-fill py-2" onClick={buyNow} disabled={soldOut}>
              {soldOut ? 'SOLD OUT' : '바로구매'}
            </button>
          </div>
        </div>
      </div>

      {/* 상세 설명 */}
      <div className="mt-5">
        <ul className="nav nav-tabs">
          <li className="nav-item"><span className="nav-link active">상세정보</span></li>
        </ul>
        <div className="py-4">
          {product.detailContent
            ? <div dangerouslySetInnerHTML={{ __html: product.detailContent }} />
            : <p className="text-muted text-center py-5">상세 설명이 준비 중입니다.</p>}
        </div>

        <div className="bg-brand-light rounded p-4 small">
          <b>배송/교환/반품 안내</b>
          <ul className="mb-0 mt-2 text-muted">
            <li>배송: 전 상품 무료배송, 주문(입금) 확인 후 2~4일 내 출고됩니다.</li>
            <li>교환/반품: 상품 수령 후 7일 이내 마이페이지에서 신청 가능합니다.</li>
            <li>단순 변심으로 인한 반품 배송비는 고객 부담입니다. 상품 하자 시 전액 판매자 부담.</li>
            <li>화장품 특성상 개봉/사용한 제품은 교환·반품이 불가합니다.</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
