import { useEffect, useState } from 'react';
import api from '../../api/client';
import MyLayout from './MyLayout';

const CATEGORIES = ['상품', '주문', '배송', '기타'];
const EMPTY = { category: '상품', title: '', content: '' };

export default function MyInquiryPage() {
  const [inquiries, setInquiries] = useState([]);
  const [form, setForm] = useState(null);
  const [openId, setOpenId] = useState(null);

  const load = () => api.get('/inquiries').then((res) => setInquiries(res.data)).catch(() => {});
  useEffect(() => { load(); }, []);

  const submit = () => {
    api.post('/inquiries', form)
      .then((res) => { alert(res.data.message); setForm(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '접수에 실패했습니다.'));
  };

  return (
    <MyLayout title="1:1 문의">
      <div className="d-flex justify-content-end mb-2">
        <button className="btn btn-sm btn-brand" onClick={() => setForm({ ...EMPTY })}>+ 문의하기</button>
      </div>

      {form && (
        <div className="border rounded p-3 mb-3">
          <select className="form-select form-select-sm mb-2" style={{ maxWidth: 160 }} value={form.category}
            onChange={(e) => setForm((p) => ({ ...p, category: e.target.value }))}>
            {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
          <input className="form-control form-control-sm mb-2" placeholder="제목"
            value={form.title} onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))} />
          <textarea className="form-control form-control-sm mb-2" rows={4} placeholder="문의 내용을 입력해주세요"
            value={form.content} onChange={(e) => setForm((p) => ({ ...p, content: e.target.value }))} />
          <div className="d-flex gap-2">
            <button className="btn btn-sm btn-brand" onClick={submit}>접수하기</button>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setForm(null)}>취소</button>
          </div>
        </div>
      )}

      {inquiries.length === 0 && <div className="text-center text-muted py-5">문의 내역이 없습니다.</div>}

      {inquiries.map((q) => (
        <div key={q.id} className="border-bottom">
          <button className="w-100 bg-transparent border-0 text-start py-3 d-flex justify-content-between align-items-center"
            onClick={() => setOpenId(openId === q.id ? null : q.id)}>
            <span>
              <span className="badge bg-secondary me-2">{q.category}</span>
              <span className="fw-semibold">{q.title}</span>
            </span>
            <span className="small">
              {q.answer
                ? <span className="badge bg-brand-light text-brand border">답변완료</span>
                : <span className="badge bg-warning text-dark">답변대기</span>}
              <span className="text-muted ms-2">{q.createdAt?.slice(0, 10)}</span>
            </span>
          </button>
          {openId === q.id && (
            <div className="pb-3">
              <div className="bg-light rounded p-3 small mb-2" style={{ whiteSpace: 'pre-wrap' }}>{q.content}</div>
              {q.answer ? (
                <div className="bg-brand-light rounded p-3 small" style={{ whiteSpace: 'pre-wrap' }}>
                  <b className="d-block mb-1">답변 <span className="text-muted fw-normal">({q.answeredAt?.slice(0, 10)})</span></b>
                  {q.answer}
                </div>
              ) : (
                <p className="small text-muted mb-0">답변을 준비 중입니다. 잠시만 기다려주세요.</p>
              )}
            </div>
          )}
        </div>
      ))}
    </MyLayout>
  );
}
