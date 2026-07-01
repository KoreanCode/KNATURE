import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

export default function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ name: '', code: '', price: 0, salePrice: '', description: '', detailContent: '', status: 'ON_SALE', categoryId: '' });

  useEffect(() => {
    api.get('/products/categories').then((res) => setCategories(res.data));
    if (id) {
      api.get(`/products/${id}`).then((res) => {
        const p = res.data;
        setForm({ name: p.name, code: p.code || '', price: p.price, salePrice: p.salePrice || '', description: p.description || '', detailContent: p.detailContent || '', status: p.status, categoryId: p.category?.id || '' });
      });
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const body = { ...form, price: Number(form.price), salePrice: form.salePrice ? Number(form.salePrice) : null, category: form.categoryId ? { id: form.categoryId } : null };
    if (id) await api.put(`/products/${id}`, body);
    else await api.post('/products', body);
    navigate('/products');
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <>
      <TopBar title={id ? '상품 수정' : '상품 등록'} />
      <div className="content-card">
        <form onSubmit={handleSubmit}>
          <div className="row mb-3">
            <div className="col-md-8">
              <label className="form-label fw-bold">상품명 *</label>
              <input type="text" className="form-control" value={form.name} onChange={set('name')} required />
            </div>
            <div className="col-md-4">
              <label className="form-label fw-bold">상품코드</label>
              <input type="text" className="form-control" value={form.code} onChange={set('code')} />
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-4">
              <label className="form-label fw-bold">카테고리</label>
              <select className="form-select" value={form.categoryId} onChange={set('categoryId')}>
                <option value="">선택</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label fw-bold">판매가 *</label>
              <input type="number" className="form-control" value={form.price} onChange={set('price')} required />
            </div>
            <div className="col-md-4">
              <label className="form-label fw-bold">할인가</label>
              <input type="number" className="form-control" value={form.salePrice} onChange={set('salePrice')} />
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label fw-bold">상품 설명</label>
            <textarea className="form-control" rows={3} value={form.description} onChange={set('description')} />
          </div>
          <div className="mb-3">
            <label className="form-label fw-bold">상세 내용</label>
            <textarea className="form-control" rows={8} value={form.detailContent} onChange={set('detailContent')} />
          </div>
          <div className="row mb-3">
            <div className="col-md-4">
              <label className="form-label fw-bold">판매 상태</label>
              <select className="form-select" value={form.status} onChange={set('status')}>
                <option value="ON_SALE">판매중</option>
                <option value="SOLD_OUT">품절</option>
                <option value="HIDDEN">숨김</option>
              </select>
            </div>
          </div>
          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-primary">저장</button>
            <button type="button" className="btn btn-outline-secondary" onClick={() => navigate('/products')}>취소</button>
          </div>
        </form>
      </div>
    </>
  );
}
