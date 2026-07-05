import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

/** 의존성 없는 SVG 라인차트 — 최근 7일 매출 */
function SalesLineChart({ data }) {
  const entries = Object.entries(data); // [['6/29', 0], ...]
  if (entries.length === 0) return null;
  const W = 700, H = 220, PAD = { top: 20, right: 20, bottom: 30, left: 70 };
  const innerW = W - PAD.left - PAD.right;
  const innerH = H - PAD.top - PAD.bottom;
  const max = Math.max(...entries.map(([, v]) => v), 1);
  const x = (i) => PAD.left + (entries.length === 1 ? innerW / 2 : (i * innerW) / (entries.length - 1));
  const y = (v) => PAD.top + innerH - (v / max) * innerH;
  const points = entries.map(([, v], i) => `${x(i)},${y(v)}`).join(' ');
  const fmt = (n) => n >= 10000 ? `${Math.round(n / 10000).toLocaleString()}만` : n.toLocaleString();

  return (
    <svg viewBox={`0 0 ${W} ${H}`} style={{ width: '100%', maxWidth: 900 }} role="img" aria-label="최근 7일 매출 라인차트">
      {[0, 0.5, 1].map((r) => (
        <g key={r}>
          <line x1={PAD.left} y1={y(max * r)} x2={W - PAD.right} y2={y(max * r)} stroke="#e9ecef" strokeWidth="1" />
          <text x={PAD.left - 8} y={y(max * r) + 4} textAnchor="end" fontSize="11" fill="#868e96">{fmt(max * r)}</text>
        </g>
      ))}
      <polyline points={points} fill="none" stroke="#0d6efd" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round" />
      {entries.map(([label, v], i) => (
        <g key={label}>
          <circle cx={x(i)} cy={y(v)} r="4" fill="#0d6efd" />
          <text x={x(i)} y={H - 8} textAnchor="middle" fontSize="11" fill="#495057">{label}</text>
          {v > 0 && <text x={x(i)} y={y(v) - 10} textAnchor="middle" fontSize="10" fill="#0d6efd">{fmt(v)}</text>}
        </g>
      ))}
    </svg>
  );
}

export default function DashboardPage() {
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get('/dashboard').then((res) => setData(res.data));
  }, []);

  if (!data) return <div className="p-4">로딩중...</div>;

  const fmt = (n) => Number(n).toLocaleString();

  return (
    <>
      <TopBar title="대시보드" />

      <div className="row mb-4">
        <div className="col-md-3">
          <div className="stat-card">
            <div className="label">오늘 매출</div>
            <div className="number text-primary">{fmt(data.todaySales)}원</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="stat-card">
            <div className="label">오늘 주문</div>
            <div className="number">{data.todayOrders}건</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="stat-card">
            <div className="label">전체 회원</div>
            <div className="number">{data.totalMembers}명</div>
            <small className="text-success">오늘 +{data.newMembers}</small>
          </div>
        </div>
        <div className="col-md-3">
          <div className="stat-card">
            <div className="label">전체 상품</div>
            <div className="number">{data.totalProducts}개</div>
          </div>
        </div>
      </div>

      <div className="row mb-4">
        <div className="col-md-4">
          <div className="content-card">
            <h6><i className="bi bi-exclamation-circle text-warning"></i> 주문 현황 (오늘의 할 일)</h6>
            <table className="table table-sm mt-2 mb-0">
              <tbody>
                {Object.entries(data.orderCounts).map(([k, v]) => (
                  <tr key={k}><td>{k}</td><td className="text-end"><span className={`badge ${v > 0 ? 'bg-danger' : 'bg-secondary'}`}>{v}건</span></td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <div className="col-md-4">
          <div className="content-card">
            <h6><i className="bi bi-headset text-info"></i> CS 현황</h6>
            <table className="table table-sm mt-2 mb-0">
              <tbody>
                {Object.entries(data.csCounts).map(([k, v]) => (
                  <tr key={k}><td>{k}</td><td className="text-end"><span className={`badge ${v > 0 ? 'bg-warning' : 'bg-secondary'}`}>{v}건</span></td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <div className="col-md-4">
          <div className="content-card">
            <h6><i className="bi bi-check2-circle text-success"></i> 오늘 처리한 일</h6>
            <table className="table table-sm mt-2 mb-0">
              <tbody>
                {Object.entries(data.todayProcessed || {}).map(([k, v]) => (
                  <tr key={k}><td>{k}</td><td className="text-end"><span className={`badge ${v > 0 ? 'bg-success' : 'bg-secondary'}`}>{v}건</span></td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="content-card">
        <h6><i className="bi bi-graph-up text-success"></i> 최근 7일 매출</h6>
        <SalesLineChart data={data.weeklySales} />
        <table className="table table-sm mt-3">
          <thead><tr><th>날짜</th><th className="text-end">매출</th></tr></thead>
          <tbody>
            {Object.entries(data.weeklySales).map(([k, v]) => (
              <tr key={k}><td>{k}</td><td className="text-end">{fmt(v)}원</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
