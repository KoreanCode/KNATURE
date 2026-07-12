import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const TYPES = [['NOTICE', '공지사항'], ['EVENT', '이벤트/뉴스'], ['FAQ', 'FAQ']];
const FAQ_CATEGORIES = ['주문', '배송', '교환/반품', '상품', '회원'];
const EMPTY = { title: '', content: '', category: '', imageUrl: '', startDate: '', endDate: '', pinned: false };

export default function BoardPage() {
  const [type, setType] = useState('NOTICE');
  const [articles, setArticles] = useState([]);
  const [form, setForm] = useState(null); // {id?, ...EMPTY}

  const load = () => api.get('/articles', { params: { type } }).then((res) => setArticles(res.data));
  useEffect(() => { load(); }, [type]);

  const set = (k) => (e) => setForm((p) => ({ ...p, [k]: e.target.value }));

  const save = () => {
    const body = { ...form, type, pinned: String(!!form.pinned) };
    const req = form.id ? api.put(`/articles/${form.id}`, body) : api.post('/articles', body);
    req.then(() => { alert('저장되었습니다.'); setForm(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '저장에 실패했습니다.'));
  };

  const remove = (a) => {
    if (!confirm(`'${a.title}' 을(를) 삭제하시겠습니까?`)) return;
    api.delete(`/articles/${a.id}`).then(() => load());
  };

  const toggleVisible = (a) => {
    api.put(`/articles/${a.id}`, { visible: String(!a.visible) }).then(() => load());
  };

  const isFaq = type === 'FAQ';
  const isEvent = type === 'EVENT';

  return (
    <>
      <TopBar title="게시판 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3">
          <ul className="nav nav-tabs">
            {TYPES.map(([k, label]) => (
              <li className="nav-item" key={k}>
                <button className={`nav-link ${type === k ? 'active' : ''}`} onClick={() => setType(k)}>{label}</button>
              </li>
            ))}
          </ul>
          <button className="btn btn-sm btn-primary" onClick={() => setForm({ ...EMPTY })}>+ 글 작성</button>
        </div>

        <table className="table table-hover">
          <thead className="table-light">
            <tr>
              <th style={{ width: 60 }}>번호</th>
              {isFaq && <th style={{ width: 110 }}>분류</th>}
              <th>{isFaq ? '질문' : '제목'}</th>
              {isEvent && <th style={{ width: 200 }}>기간</th>}
              <th className="text-center" style={{ width: 90 }}>노출</th>
              <th className="text-center" style={{ width: 130 }}>등록일</th>
              <th className="text-center" style={{ width: 140 }}>관리</th>
            </tr>
          </thead>
          <tbody>
            {articles.map((a, i) => (
              <tr key={a.id}>
                <td>{a.pinned ? <span className="badge bg-danger">고정</span> : articles.length - i}</td>
                {isFaq && <td>{a.category || '-'}</td>}
                <td>{a.title}</td>
                {isEvent && <td className="small">{a.startDate || '?'} ~ {a.endDate || '?'}</td>}
                <td className="text-center">
                  <button className={`btn btn-sm py-0 ${a.visible ? 'btn-success' : 'btn-outline-secondary'}`}
                    onClick={() => toggleVisible(a)}>{a.visible ? '노출' : '숨김'}</button>
                </td>
                <td className="text-center small">{a.createdAt?.slice(0, 10)}</td>
                <td className="text-center">
                  <button className="btn btn-sm btn-outline-primary py-0 me-1"
                    onClick={() => setForm({ id: a.id, title: a.title, content: a.content, category: a.category || '', imageUrl: a.imageUrl || '', startDate: a.startDate || '', endDate: a.endDate || '', pinned: a.pinned })}>수정</button>
                  <button className="btn btn-sm btn-outline-danger py-0" onClick={() => remove(a)}>삭제</button>
                </td>
              </tr>
            ))}
            {articles.length === 0 && <tr><td colSpan={7} className="text-center text-muted py-4">게시글이 없습니다.</td></tr>}
          </tbody>
        </table>
      </div>

      {form && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setForm(null)}>
          <div className="modal-dialog modal-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header">
                <h6 className="modal-title">{TYPES.find(([k]) => k === type)[1]} {form.id ? '수정' : '작성'}</h6>
                <button type="button" className="btn-close" onClick={() => setForm(null)} />
              </div>
              <div className="modal-body">
                {isFaq && (
                  <select className="form-select form-select-sm mb-2" value={form.category} onChange={set('category')}>
                    <option value="">분류 선택</option>
                    {FAQ_CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
                  </select>
                )}
                <input className="form-control mb-2" placeholder={isFaq ? '질문 *' : '제목 *'} value={form.title} maxLength={200} onChange={set('title')} />
                <textarea className="form-control mb-2" rows={8} placeholder={isFaq ? '답변 *' : '내용 * (HTML 가능)'} value={form.content} maxLength={2000} onChange={set('content')} />
                {isEvent && (
                  <div className="d-flex gap-2 mb-2">
                    <input type="date" className="form-control form-control-sm" value={form.startDate} onChange={set('startDate')} />
                    <span className="align-self-center">~</span>
                    <input type="date" className="form-control form-control-sm" value={form.endDate} onChange={set('endDate')} />
                  </div>
                )}
                {type === 'NOTICE' && (
                  <div className="form-check">
                    <input type="checkbox" className="form-check-input" id="pinned" checked={!!form.pinned}
                      onChange={(e) => setForm((p) => ({ ...p, pinned: e.target.checked }))} />
                    <label className="form-check-label small" htmlFor="pinned">상단 고정</label>
                  </div>
                )}
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
