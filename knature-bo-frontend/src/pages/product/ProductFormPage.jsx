import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const API_ORIGIN = 'http://localhost:9090';

export default function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ name: '', code: '', price: 0, salePrice: '', description: '', detailContent: '', status: 'ON_SALE', displayed: true, categoryId: '' });
  const [images, setImages] = useState([]);   // [{imageUrl, isMain, sortOrder}]
  const [options, setOptions] = useState([]); // [{name, additionalPrice, stockQuantity, sortOrder}]
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    api.get('/products/categories').then((res) => setCategories(res.data));
    if (id) {
      api.get(`/products/${id}`).then((res) => {
        const p = res.data;
        setForm({ name: p.name, code: p.code || '', price: p.price, salePrice: p.salePrice || '', description: p.description || '', detailContent: p.detailContent || '', status: p.status, displayed: p.displayed ?? true, categoryId: p.category?.id || '' });
        setImages((p.images || []).map((img) => ({ imageUrl: img.imageUrl, isMain: img.isMain, sortOrder: img.sortOrder })));
        setOptions((p.options || []).map((o) => ({ name: o.name, additionalPrice: o.additionalPrice, stockQuantity: o.stockQuantity, sortOrder: o.sortOrder })));
      });
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const body = {
      ...form,
      price: Number(form.price),
      salePrice: form.salePrice ? Number(form.salePrice) : null,
      category: form.categoryId ? { id: form.categoryId } : null,
      images: images.map((img, i) => ({ ...img, sortOrder: i, isMain: !!img.isMain })),
      options: options.map((o, i) => ({ ...o, additionalPrice: Number(o.additionalPrice) || 0, stockQuantity: Number(o.stockQuantity) || 0, sortOrder: i })),
    };
    if (id) await api.put(`/products/${id}`, body);
    else await api.post('/products', body);
    navigate('/products');
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const uploadImage = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const fd = new FormData();
      fd.append('file', file);
      const res = await api.post('/files', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
      setImages((prev) => [...prev, { imageUrl: res.data.url, isMain: prev.length === 0, sortOrder: prev.length }]);
    } catch (err) {
      alert(err.response?.data?.message || '이미지 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const setMain = (idx) => setImages(images.map((img, i) => ({ ...img, isMain: i === idx })));
  const removeImage = (idx) => {
    const next = images.filter((_, i) => i !== idx);
    if (images[idx].isMain && next.length > 0) next[0] = { ...next[0], isMain: true };
    setImages(next);
  };

  const addOption = () => setOptions([...options, { name: '', additionalPrice: 0, stockQuantity: 0, sortOrder: options.length }]);
  const setOption = (idx, key) => (e) => setOptions(options.map((o, i) => i === idx ? { ...o, [key]: e.target.value } : o));
  const removeOption = (idx) => setOptions(options.filter((_, i) => i !== idx));

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
            <label className="form-label fw-bold">상품 이미지 <small className="text-muted">(첫 번째 = 대표, FO 상품 이미지 연동)</small></label>
            <div className="d-flex flex-wrap gap-3 mb-2">
              {images.map((img, i) => (
                <div key={i} className="border rounded p-2 text-center" style={{ width: 140 }}>
                  <img src={API_ORIGIN + img.imageUrl} alt="" style={{ width: '100%', height: 100, objectFit: 'cover' }} />
                  <div className="mt-1 d-flex justify-content-center gap-1">
                    {img.isMain
                      ? <span className="badge bg-primary">대표</span>
                      : <button type="button" className="btn btn-sm btn-outline-primary py-0" onClick={() => setMain(i)}>대표 지정</button>}
                    <button type="button" className="btn btn-sm btn-outline-danger py-0" onClick={() => removeImage(i)}>삭제</button>
                  </div>
                </div>
              ))}
            </div>
            <input type="file" accept="image/*" className="form-control" style={{ maxWidth: 400 }} onChange={uploadImage} disabled={uploading} />
            {uploading && <small className="text-muted">업로드 중...</small>}
          </div>

          <div className="mb-3">
            <label className="form-label fw-bold">옵션 / 재고 <small className="text-muted">(용량 등, FO 옵션 선택 연동)</small></label>
            <table className="table table-sm" style={{ maxWidth: 700 }}>
              <thead className="table-light">
                <tr><th>옵션명</th><th style={{ width: 150 }}>추가금액</th><th style={{ width: 120 }}>옵션 재고</th><th style={{ width: 70 }}></th></tr>
              </thead>
              <tbody>
                {options.map((o, i) => (
                  <tr key={i}>
                    <td><input type="text" className="form-control form-control-sm" placeholder="예: 300ml" value={o.name} onChange={setOption(i, 'name')} required /></td>
                    <td><input type="number" className="form-control form-control-sm" value={o.additionalPrice} onChange={setOption(i, 'additionalPrice')} /></td>
                    <td><input type="number" min="0" className="form-control form-control-sm" value={o.stockQuantity} onChange={setOption(i, 'stockQuantity')} /></td>
                    <td><button type="button" className="btn btn-sm btn-outline-danger" onClick={() => removeOption(i)}>삭제</button></td>
                  </tr>
                ))}
                {options.length === 0 && <tr><td colSpan={4} className="text-muted text-center py-2">옵션 없음 (단일 상품)</td></tr>}
              </tbody>
            </table>
            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={addOption}>+ 옵션 추가</button>
          </div>

          <div className="mb-3">
            <label className="form-label fw-bold">상품 설명</label>
            <textarea className="form-control" rows={3} value={form.description} onChange={set('description')} />
          </div>
          <div className="mb-3">
            <label className="form-label fw-bold">상세 내용 <small className="text-muted">(HTML 가능, FO 상세페이지 연동)</small></label>
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
            <div className="col-md-4">
              <label className="form-label fw-bold">진열 상태</label>
              <select className="form-select" value={form.displayed ? 'Y' : 'N'} onChange={(e) => setForm({ ...form, displayed: e.target.value === 'Y' })}>
                <option value="Y">진열함</option>
                <option value="N">진열 안 함</option>
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
