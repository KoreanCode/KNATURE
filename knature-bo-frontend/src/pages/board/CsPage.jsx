import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

export default function CsPage() {
  const [tab, setTab] = useState('inquiry');
  const [inquiries, setInquiries] = useState([]);
  const [unansweredOnly, setUnansweredOnly] = useState(false);
  const [answering, setAnswering] = useState(null); // {id, title, content, answer}
  const [reviews, setReviews] = useState([]);

  const loadInquiries = () => api.get('/inquiries', { params: { unanswered: unansweredOnly } }).then((res) => setInquiries(res.data));
  const loadReviews = () => api.get('/reviews').then((res) => setReviews(res.data));

  useEffect(() => { loadInquiries(); }, [unansweredOnly]);
  useEffect(() => { if (tab === 'review') loadReviews(); }, [tab]);

  const submitAnswer = () => {
    api.post(`/inquiries/${answering.id}/answer`, { answer: answering.answer })
      .then((res) => { alert(res.data.message); setAnswering(null); loadInquiries(); })
      .catch((err) => alert(err.response?.data?.message || '답변 등록에 실패했습니다.'));
  };

  const toggleBlind = (r) => {
    api.patch(`/reviews/${r.id}/blind`).then((res) => { alert(res.data.message); loadReviews(); });
  };

  const removeReview = (r) => {
    if (!confirm('이 후기를 삭제하시겠습니까?')) return;
    api.delete(`/reviews/${r.id}`).then(() => loadReviews());
  };

  return (
    <>
      <TopBar title="문의/후기 관리" />
      <div className="content-card">
        <ul className="nav nav-tabs mb-3">
          <li className="nav-item"><button className={`nav-link ${tab === 'inquiry' ? 'active' : ''}`} onClick={() => setTab('inquiry')}>1:1 문의</button></li>
          <li className="nav-item"><button className={`nav-link ${tab === 'review' ? 'active' : ''}`} onClick={() => setTab('review')}>상품 후기</button></li>
        </ul>

        {tab === 'inquiry' && (
          <>
            <div className="form-check form-switch mb-2">
              <input type="checkbox" className="form-check-input" id="unans" checked={unansweredOnly}
                onChange={(e) => setUnansweredOnly(e.target.checked)} />
              <label className="form-check-label small" htmlFor="unans">미답변만 보기</label>
            </div>
            <table className="table table-hover">
              <thead className="table-light">
                <tr><th style={{ width: 90 }}>분류</th><th>제목</th><th style={{ width: 120 }}>작성자</th><th className="text-center" style={{ width: 100 }}>상태</th><th className="text-center" style={{ width: 120 }}>등록일</th><th className="text-center" style={{ width: 90 }}>관리</th></tr>
              </thead>
              <tbody>
                {inquiries.map((q) => (
                  <tr key={q.id}>
                    <td><span className="badge bg-secondary">{q.category}</span></td>
                    <td>{q.title}</td>
                    <td>{q.member?.name} ({q.member?.username})</td>
                    <td className="text-center">
                      {q.answer ? <span className="badge bg-success">답변완료</span> : <span className="badge bg-warning text-dark">미답변</span>}
                    </td>
                    <td className="text-center small">{q.createdAt?.slice(0, 10)}</td>
                    <td className="text-center">
                      <button className="btn btn-sm btn-outline-primary py-0"
                        onClick={() => setAnswering({ id: q.id, title: q.title, content: q.content, answer: q.answer || '' })}>
                        {q.answer ? '답변보기' : '답변하기'}
                      </button>
                    </td>
                  </tr>
                ))}
                {inquiries.length === 0 && <tr><td colSpan={6} className="text-center text-muted py-4">문의가 없습니다.</td></tr>}
              </tbody>
            </table>
          </>
        )}

        {tab === 'review' && (
          <table className="table table-hover">
            <thead className="table-light">
              <tr><th style={{ width: 180 }}>상품</th><th style={{ width: 70 }}>별점</th><th>내용</th><th style={{ width: 110 }}>작성자</th><th className="text-center" style={{ width: 120 }}>작성일</th><th className="text-center" style={{ width: 160 }}>관리</th></tr>
            </thead>
            <tbody>
              {reviews.map((r) => (
                <tr key={r.id} className={r.blinded ? 'table-secondary' : ''}>
                  <td className="small">{r.product?.name}</td>
                  <td>{'★'.repeat(r.rating)}</td>
                  <td className="small">{r.content}</td>
                  <td className="small">{r.member?.name}</td>
                  <td className="text-center small">{r.createdAt?.slice(0, 10)}</td>
                  <td className="text-center">
                    <button className={`btn btn-sm py-0 me-1 ${r.blinded ? 'btn-secondary' : 'btn-outline-warning'}`}
                      onClick={() => toggleBlind(r)}>{r.blinded ? '블라인드 해제' : '블라인드'}</button>
                    <button className="btn btn-sm btn-outline-danger py-0" onClick={() => removeReview(r)}>삭제</button>
                  </td>
                </tr>
              ))}
              {reviews.length === 0 && <tr><td colSpan={6} className="text-center text-muted py-4">등록된 후기가 없습니다.</td></tr>}
            </tbody>
          </table>
        )}
      </div>

      {answering && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setAnswering(null)}>
          <div className="modal-dialog modal-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">문의 답변 — {answering.title}</h6>
                <button type="button" className="btn-close" onClick={() => setAnswering(null)} /></div>
              <div className="modal-body">
                <div className="bg-light rounded p-3 mb-3 small" style={{ whiteSpace: 'pre-wrap' }}>{answering.content}</div>
                <textarea className="form-control" rows={5} placeholder="답변 내용을 입력해주세요"
                  value={answering.answer} onChange={(e) => setAnswering({ ...answering, answer: e.target.value })} />
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setAnswering(null)}>닫기</button>
                <button className="btn btn-primary btn-sm" onClick={submitAnswer}>답변 등록</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
