import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

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
        <div className="col-md-6">
          <div className="content-card">
            <h6><i className="bi bi-exclamation-circle text-warning"></i> 주문 현황</h6>
            <table className="table table-sm mt-2 mb-0">
              <tbody>
                {Object.entries(data.orderCounts).map(([k, v]) => (
                  <tr key={k}><td>{k}</td><td className="text-end"><span className={`badge ${v > 0 ? 'bg-danger' : 'bg-secondary'}`}>{v}건</span></td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <div className="col-md-6">
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
      </div>

      <div className="content-card">
        <h6><i className="bi bi-graph-up text-success"></i> 최근 7일 매출</h6>
        <table className="table table-sm mt-2">
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
