// 윈도우형 페이지네이션: 처음/끝 + 현재 페이지 주변(±2)만 노출하고 나머지는 '…' 로 생략.
// page/onChange 는 0-based 페이지 인덱스를 사용한다.
export default function Pagination({ totalPages, page, onChange }) {
  if (totalPages <= 1) return null;

  const last = totalPages - 1;
  const windowSize = 2; // 현재 페이지 양옆으로 보여줄 개수

  const pages = new Set([0, last]);
  for (let i = page - windowSize; i <= page + windowSize; i++) {
    if (i >= 0 && i <= last) pages.add(i);
  }
  const sorted = [...pages].sort((a, b) => a - b);

  // 연속되지 않는 구간 사이에 생략 표시(...) 삽입
  const items = [];
  let prev = null;
  for (const p of sorted) {
    if (prev !== null && p - prev > 1) items.push('ellipsis');
    items.push(p);
    prev = p;
  }

  return (
    <nav className="mt-3">
      <ul className="pagination pagination-sm justify-content-center mb-0">
        <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onChange(page - 1)} disabled={page === 0}>&laquo;</button>
        </li>
        {items.map((it, idx) =>
          it === 'ellipsis' ? (
            <li key={`e${idx}`} className="page-item disabled"><span className="page-link">…</span></li>
          ) : (
            <li key={it} className={`page-item ${it === page ? 'active' : ''}`}>
              <button className="page-link" onClick={() => onChange(it)}>{it + 1}</button>
            </li>
          )
        )}
        <li className={`page-item ${page === last ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onChange(page + 1)} disabled={page === last}>&raquo;</button>
        </li>
      </ul>
    </nav>
  );
}
