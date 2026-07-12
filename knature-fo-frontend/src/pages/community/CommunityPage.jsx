import { useEffect, useState } from 'react';
import api from '../../api/client';

const TABS = [['NOTICE', '공지사항'], ['EVENT', '이벤트/뉴스'], ['FAQ', 'FAQ']];

export default function CommunityPage() {
  const [tab, setTab] = useState('NOTICE');
  const [articles, setArticles] = useState([]);
  const [openId, setOpenId] = useState(null);

  useEffect(() => {
    setOpenId(null);
    api.get('/articles', { params: { type: tab } }).then((res) => setArticles(res.data)).catch(() => setArticles([]));
  }, [tab]);

  return (
    <div className="container py-4" style={{ maxWidth: 860 }}>
      <h4 className="fw-bold text-center mb-4">COMMUNITY</h4>

      <ul className="nav nav-tabs justify-content-center mb-4">
        {TABS.map(([k, label]) => (
          <li className="nav-item" key={k}>
            <button className={`nav-link ${tab === k ? 'active' : ''}`} onClick={() => setTab(k)}>{label}</button>
          </li>
        ))}
      </ul>

      {articles.length === 0 && <div className="text-center text-muted py-5">등록된 글이 없습니다.</div>}

      {articles.map((a) => (
        <div key={a.id} className="border-bottom">
          <button className="w-100 bg-transparent border-0 text-start py-3 d-flex justify-content-between align-items-center"
            onClick={() => setOpenId(openId === a.id ? null : a.id)}>
            <span>
              {a.pinned && <span className="badge bg-brand-light text-brand border me-2">공지</span>}
              {tab === 'FAQ' && a.category && <span className="badge bg-secondary me-2">{a.category}</span>}
              <span className="fw-semibold">{tab === 'FAQ' ? 'Q. ' : ''}{a.title}</span>
            </span>
            <span className="small text-muted">
              {tab === 'EVENT' && a.startDate ? `${a.startDate} ~ ${a.endDate || ''}` : a.createdAt?.slice(0, 10)}
              <i className={`bi ms-2 ${openId === a.id ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
            </span>
          </button>
          {openId === a.id && (
            <div className="bg-brand-light rounded p-4 mb-3" style={{ whiteSpace: 'pre-wrap' }}>
              {tab === 'FAQ' && <b className="d-block mb-2">A.</b>}
              <div dangerouslySetInnerHTML={{ __html: a.content }} />
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
