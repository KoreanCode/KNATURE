import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';
import { downloadFile } from '../../utils/download';

/** 의존성 없는 SVG 라인차트 — DashboardPage와 동일 패턴 (기간이 길면 x축 라벨 간격 조절) */
function SalesLineChart({ data }) {
  const entries = Object.entries(data); // [['7/1', 12000], ...]
  if (entries.length === 0) return null;
  const W = 1400, H = 300, PAD = { top: 28, right: 40, bottom: 40, left: 90 };
  const innerW = W - PAD.left - PAD.right;
  const innerH = H - PAD.top - PAD.bottom;
  const max = Math.max(...entries.map(([, v]) => v), 1);
  const x = (i) => PAD.left + (entries.length === 1 ? innerW / 2 : (i * innerW) / (entries.length - 1));
  const y = (v) => PAD.top + innerH - (v / max) * innerH;
  const points = entries.map(([, v], i) => `${x(i)},${y(v)}`).join(' ');
  const fmt = (n) => n >= 10000 ? `${Math.round(n / 10000).toLocaleString()}만` : n.toLocaleString();
  const labelStep = Math.ceil(entries.length / 15); // 라벨이 겹치지 않도록 간격 조절
  const valueStep = Math.ceil(entries.length / 10);

  return (
    <svg viewBox={`0 0 ${W} ${H}`} style={{ width: '100%', display: 'block' }} role="img" aria-label="일별 매출 라인차트">
      {[0, 0.5, 1].map((r) => (
        <g key={r}>
          <line x1={PAD.left} y1={y(max * r)} x2={W - PAD.right} y2={y(max * r)} stroke="#e9ecef" strokeWidth="1" />
          <text x={PAD.left - 10} y={y(max * r) + 5} textAnchor="end" fontSize="14" fill="#868e96">{fmt(max * r)}</text>
        </g>
      ))}
      <polyline points={points} fill="none" stroke="#0d6efd" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round" />
      {entries.map(([label, v], i) => (
        <g key={label}>
          <circle cx={x(i)} cy={y(v)} r={entries.length > 60 ? 2.5 : 4.5} fill="#0d6efd" />
          {i % labelStep === 0 && <text x={x(i)} y={H - 12} textAnchor="middle" fontSize="14" fill="#495057">{label}</text>}
          {v > 0 && i % valueStep === 0 && <text x={x(i)} y={y(v) - 12} textAnchor="middle" fontSize="13" fill="#0d6efd">{fmt(v)}</text>}
        </g>
      ))}
    </svg>
  );
}

const toYMD = (d) => {
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const daysAgo = (n) => {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return toYMD(d);
};

export default function AnalyticsPage() {
  const today = toYMD(new Date());
  const [from, setFrom] = useState(daysAgo(29));
  const [to, setTo] = useState(today);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  const load = (f = from, t = to) => {
    setLoading(true);
    api.get('/analytics', { params: { from: f, to: t } })
      .then((res) => setData(res.data))
      .catch((err) => alert(err.response?.data?.message || '매출 분석 조회에 실패했습니다.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const setQuick = (f, t) => {
    setFrom(f);
    setTo(t);
    load(f, t);
  };

  const quickThisMonth = () => {
    const d = new Date();
    setQuick(toYMD(new Date(d.getFullYear(), d.getMonth(), 1)), today);
  };

  const downloadExcel = () => downloadFile('/analytics/excel', `매출분석_${from}_${to}.xlsx`, { from, to });

  const fmt = (n) => Number(n).toLocaleString();

  // 일별 데이터 → 차트용 { 'M/D': total }
  const chartData = {};
  (data?.daily || []).forEach((d) => {
    const [, m, day] = d.date.split('-');
    chartData[`${Number(m)}/${Number(day)}`] = d.total;
  });

  return (
    <>
      <TopBar title="매출 분석" />

      {/* 기간 선택 */}
      <div className="content-card mb-4">
        <div className="d-flex flex-wrap align-items-center gap-2">
          <input type="date" className="form-control form-control-sm" style={{ width: 160 }}
            value={from} max={to} onChange={(e) => setFrom(e.target.value)} />
          <span>~</span>
          <input type="date" className="form-control form-control-sm" style={{ width: 160 }}
            value={to} min={from} max={today} onChange={(e) => setTo(e.target.value)} />
          <button className="btn btn-sm btn-primary" onClick={() => load()} disabled={loading}>
            <i className="bi bi-search"></i> 조회
          </button>
          <div className="btn-group btn-group-sm ms-2">
            <button className="btn btn-outline-secondary" onClick={() => setQuick(daysAgo(6), today)}>최근 7일</button>
            <button className="btn btn-outline-secondary" onClick={() => setQuick(daysAgo(29), today)}>최근 30일</button>
            <button className="btn btn-outline-secondary" onClick={quickThisMonth}>이번 달</button>
          </div>
          <button className="btn btn-sm btn-outline-success ms-auto" onClick={downloadExcel}>
            <i className="bi bi-file-earmark-excel"></i> 엑셀 다운로드
          </button>
        </div>
      </div>

      {loading && <div className="p-4">로딩중...</div>}

      {!loading && data && (
        <>
          {/* 요약 카드 */}
          <div className="row mb-4">
            <div className="col-md-4">
              <div className="stat-card">
                <div className="label">총매출 ({data.summary.from} ~ {data.summary.to})</div>
                <div className="number text-primary">{fmt(data.summary.totalSales)}원</div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="stat-card">
                <div className="label">주문 수</div>
                <div className="number">{fmt(data.summary.orderCount)}건</div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="stat-card">
                <div className="label">평균 주문금액</div>
                <div className="number">{fmt(data.summary.avgOrderAmount)}원</div>
              </div>
            </div>
          </div>

          {/* 일별 매출 */}
          <div className="content-card mb-4">
            <h6><i className="bi bi-graph-up text-success"></i> 일별 매출</h6>
            <SalesLineChart data={chartData} />
            <table className="table table-sm mt-3 mb-0">
              <thead className="table-light">
                <tr><th>날짜</th><th className="text-end">주문수</th><th className="text-end">매출</th></tr>
              </thead>
              <tbody>
                {data.daily.map((d) => (
                  <tr key={d.date}>
                    <td>{d.date}</td>
                    <td className="text-end">{fmt(d.orders)}건</td>
                    <td className="text-end">{fmt(d.total)}원</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="row mb-4">
            {/* 상품별 매출 */}
            <div className="col-md-6">
              <div className="content-card h-100">
                <h6><i className="bi bi-box-seam text-primary"></i> 상품별 매출</h6>
                <table className="table table-sm mt-2 mb-0">
                  <thead className="table-light">
                    <tr><th>상품(옵션)</th><th className="text-end">판매수량</th><th className="text-end">매출액</th></tr>
                  </thead>
                  <tbody>
                    {data.byProduct.length === 0 && (
                      <tr><td colSpan={3} className="text-muted text-center">데이터가 없습니다.</td></tr>
                    )}
                    {data.byProduct.map((p) => (
                      <tr key={p.name}>
                        <td>{p.name}</td>
                        <td className="text-end">{fmt(p.quantity)}개</td>
                        <td className="text-end">{fmt(p.total)}원</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="col-md-6 d-flex flex-column">
              {/* 등급별 매출 */}
              <div className="content-card mb-4">
                <h6><i className="bi bi-award text-warning"></i> 등급별 매출</h6>
                <table className="table table-sm mt-2 mb-0">
                  <thead className="table-light">
                    <tr><th>등급</th><th className="text-end">주문수</th><th className="text-end">매출액</th></tr>
                  </thead>
                  <tbody>
                    {data.byGrade.length === 0 && (
                      <tr><td colSpan={3} className="text-muted text-center">데이터가 없습니다.</td></tr>
                    )}
                    {data.byGrade.map((g) => (
                      <tr key={g.grade}>
                        <td>{g.grade}</td>
                        <td className="text-end">{fmt(g.orders)}건</td>
                        <td className="text-end">{fmt(g.total)}원</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* 결제수단별 매출 */}
              <div className="content-card">
                <h6><i className="bi bi-credit-card text-info"></i> 결제수단별 매출</h6>
                <table className="table table-sm mt-2 mb-0">
                  <thead className="table-light">
                    <tr><th>수단</th><th className="text-end">주문수</th><th className="text-end">매출액</th></tr>
                  </thead>
                  <tbody>
                    {data.byPayment.length === 0 && (
                      <tr><td colSpan={3} className="text-muted text-center">데이터가 없습니다.</td></tr>
                    )}
                    {data.byPayment.map((p) => (
                      <tr key={p.method}>
                        <td>{p.method}</td>
                        <td className="text-end">{fmt(p.orders)}건</td>
                        <td className="text-end">{fmt(p.total)}원</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
}
