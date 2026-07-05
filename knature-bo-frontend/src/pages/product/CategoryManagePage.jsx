import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

export default function CategoryManagePage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(null); // {id?, name, slug, sortOrder}

  const load = () => api.get('/products/categories').then((res) => setCategories(res.data));
  useEffect(() => { load(); }, []);

  const save = () => {
    const body = { name: form.name, slug: form.slug, sortOrder: String(form.sortOrder || 0) };
    const req = form.id ? api.put(`/products/categories/${form.id}`, body) : api.post('/products/categories', body);
    req.then(() => { alert('저장되었습니다.'); setForm(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '저장에 실패했습니다.'));
  };

  const remove = (c) => {
    if (!confirm(`'${c.name}' 분류를 삭제하시겠습니까?`)) return;
    api.delete(`/products/categories/${c.id}`)
      .then(() => { alert('삭제되었습니다.'); load(); })
      .catch((err) => alert(err.response?.data?.message || '삭제에 실패했습니다. (사용 중인 상품이 있는지 확인)'));
  };

  return (
    <>
      <TopBar title="상품 분류 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3">
          <button className="btn btn-sm btn-outline-secondary" onClick={() => navigate('/products')}>← 상품 목록</button>
          <button className="btn btn-sm btn-primary" onClick={() => setForm({ name: '', slug: '', sortOrder: categories.length + 1 })}>+ 분류 추가</button>
        </div>
        <table className="table table-hover">
          <thead className="table-light">
            <tr><th style={{ width: 60 }}>순서</th><th>분류명</th><th>슬러그</th><th className="text-center" style={{ width: 160 }}>관리</th></tr>
          </thead>
          <tbody>
            {categories.map((c) => (
              <tr key={c.id}>
                <td>{c.sortOrder}</td>
                <td>{c.name}</td>
                <td><code>{c.slug}</code></td>
                <td className="text-center">
                  <button className="btn btn-sm btn-outline-primary me-1"
                    onClick={() => setForm({ id: c.id, name: c.name, slug: c.slug, sortOrder: c.sortOrder })}>수정</button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => remove(c)}>삭제</button>
                </td>
              </tr>
            ))}
            {categories.length === 0 && <tr><td colSpan={4} className="text-center text-muted py-4">등록된 분류가 없습니다.</td></tr>}
          </tbody>
        </table>
        <small className="text-muted">분류는 FO 카테고리 GNB와 연동됩니다. 사용 중인 상품이 있는 분류는 삭제할 수 없습니다.</small>
      </div>

      {form && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setForm(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header">
                <h6 className="modal-title">{form.id ? '분류 수정' : '분류 추가'}</h6>
                <button type="button" className="btn-close" onClick={() => setForm(null)} />
              </div>
              <div className="modal-body">
                <div className="mb-2">
                  <label className="form-label">분류명 *</label>
                  <input type="text" className="form-control" value={form.name} placeholder="예: HYDRA CALMING LINE"
                    onChange={(e) => setForm({ ...form, name: e.target.value })} />
                </div>
                <div className="mb-2">
                  <label className="form-label">슬러그 * <small className="text-muted">(URL용 영문)</small></label>
                  <input type="text" className="form-control" value={form.slug} placeholder="예: hydra-calming-line"
                    onChange={(e) => setForm({ ...form, slug: e.target.value })} />
                </div>
                <div>
                  <label className="form-label">정렬 순서</label>
                  <input type="number" className="form-control" value={form.sortOrder}
                    onChange={(e) => setForm({ ...form, sortOrder: e.target.value })} />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setForm(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={save}>저장</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
