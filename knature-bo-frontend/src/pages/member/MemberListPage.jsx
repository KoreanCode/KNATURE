import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import Pagination from '../../components/Pagination';
import { downloadFile } from '../../utils/download';

const GRADE_LABELS = { NEW: '뉴', RUBY: '루비', SILVER: '실버', GOLD: '골드', DIAMOND: '다이아몬드', PLATINUM: '플래티넘' };

export default function MemberListPage() {
  const [members, setMembers] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const navigate = useNavigate();

  const page = parseInt(searchParams.get('page') || '0');
  const grade = searchParams.get('grade') || '';

  useEffect(() => {
    const params = { page, size: 20 };
    if (keyword) params.keyword = keyword;
    if (grade) params.grade = grade;
    api.get('/members', { params }).then((res) => setMembers(res.data));
  }, [page, grade, searchParams]);

  const handleSearch = (e) => { e.preventDefault(); setSearchParams({ keyword, grade, page: 0 }); };
  const fmt = (n) => Number(n).toLocaleString();

  return (
    <>
      <TopBar title="고객 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-between mb-3 flex-wrap gap-2">
          <form className="d-flex gap-2" onSubmit={handleSearch}>
            <input type="text" className="form-control form-control-sm" placeholder="이름/아이디 검색" value={keyword} maxLength={50} onChange={(e) => setKeyword(e.target.value)} style={{ width: 200 }} />
            <select className="form-select form-select-sm" style={{ width: 140 }} value={grade} onChange={(e) => setSearchParams({ keyword, grade: e.target.value, page: 0 })}>
              <option value="">전체 등급</option>
              {Object.entries(GRADE_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="btn btn-sm btn-outline-primary">검색</button>
          </form>
          <button className="btn btn-sm btn-outline-success" onClick={() => {
            const params = {};
            if (keyword) params.keyword = keyword;
            if (grade) params.grade = grade;
            downloadFile('/members/excel', '회원목록.xlsx', params);
          }}><i className="bi bi-file-earmark-excel"></i> 엑셀 다운로드</button>
        </div>

        <table className="table table-hover">
          <thead className="table-light">
            <tr><th style={{ width: 60 }}>번호</th><th>아이디</th><th>이름</th><th>이메일</th><th className="text-center">등급</th><th className="text-end">총 구매금액</th><th className="text-center">가입일</th></tr>
          </thead>
          <tbody>
            {members.content.map((m, i) => (
              <tr key={m.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/members/${m.id}`)}>
                <td>{members.number * 20 + i + 1}</td><td>{m.username}</td><td>{m.name}</td><td>{m.email}</td>
                <td className="text-center"><span className="badge bg-primary">{GRADE_LABELS[m.grade]}</span></td>
                <td className="text-end">{fmt(m.totalPurchaseAmount)}원</td>
                <td className="text-center">{m.createdAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {members.totalElements === 0 && <tr><td colSpan={7} className="text-center text-muted py-4">등록된 회원이 없습니다.</td></tr>}
          </tbody>
        </table>

        <Pagination
          totalPages={members.totalPages}
          page={members.number}
          onChange={(p) => setSearchParams({ keyword, grade, page: p })}
        />
      </div>
    </>
  );
}
