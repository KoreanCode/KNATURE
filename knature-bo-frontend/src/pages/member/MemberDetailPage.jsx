import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const GRADE_LABELS = { NEW: '뉴', RUBY: '루비', SILVER: '실버', GOLD: '골드', DIAMOND: '다이아몬드', PLATINUM: '플래티넘' };
const ORDER_STATUS_LABELS = { PENDING_PAYMENT: '입금전', PREPARING: '배송준비중', SHIPPING: '배송중', DELIVERED: '배송완료', CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료', EXCHANGE_REQUESTED: '교환요청', EXCHANGE_COMPLETED: '교환완료', RETURN_REQUESTED: '반품요청', RETURN_COMPLETED: '반품완료', REFUND_COMPLETED: '환불완료' };

export default function MemberDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [member, setMember] = useState(null);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get(`/members/${id}`)
      .then((res) => setMember(res.data))
      .catch(() => setError('회원 정보를 불러오지 못했습니다.'));
    api.get(`/members/${id}/orders`)
      .then((res) => setOrders(res.data))
      .catch(() => setOrders([]));
  }, [id]);

  const fmt = (n) => (n != null ? Number(n).toLocaleString() : '-');

  if (error) {
    return (
      <>
        <TopBar title="회원 상세" />
        <div className="content-card"><p className="text-danger">{error}</p>
          <button className="btn btn-sm btn-secondary" onClick={() => navigate('/members')}>목록으로</button>
        </div>
      </>
    );
  }

  if (!member) {
    return (<><TopBar title="회원 상세" /><div className="content-card">로딩중...</div></>);
  }

  return (
    <>
      <TopBar title="회원 상세" />

      <div className="content-card mb-3">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h6 className="mb-0">회원 정보</h6>
          <button className="btn btn-sm btn-outline-secondary" onClick={() => navigate('/members')}>← 목록으로</button>
        </div>
        <table className="table table-bordered mb-0">
          <tbody>
            <tr>
              <th className="table-light" style={{ width: 140 }}>아이디</th><td>{member.username}</td>
              <th className="table-light" style={{ width: 140 }}>이름</th><td>{member.name}</td>
            </tr>
            <tr>
              <th className="table-light">이메일</th><td>{member.email}</td>
              <th className="table-light">연락처</th><td>{member.phone || '-'}</td>
            </tr>
            <tr>
              <th className="table-light">등급</th>
              <td>
                <div className="d-flex gap-2 align-items-center">
                  <select className="form-select form-select-sm" style={{ width: 130 }} value={member.grade}
                    onChange={(e) => setMember({ ...member, grade: e.target.value })}>
                    {Object.entries(GRADE_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                  </select>
                  <button className="btn btn-sm btn-outline-primary" onClick={() => {
                    api.patch(`/members/${id}/grade`, { grade: member.grade })
                      .then(() => alert('회원 등급이 변경되었습니다.'));
                  }}>등급 변경</button>
                </div>
              </td>
              <th className="table-light">상태</th>
              <td>{member.active
                ? <span className="badge bg-success">활성</span>
                : <span className="badge bg-secondary">비활성</span>}</td>
            </tr>
            <tr>
              <th className="table-light">총 구매금액</th><td>{fmt(member.totalPurchaseAmount)}원</td>
              <th className="table-light">가입일</th><td>{member.createdAt?.slice(0, 10)}</td>
            </tr>
            <tr>
              <th className="table-light">주소</th>
              <td colSpan={3}>
                {member.zipcode ? `[${member.zipcode}] ` : ''}
                {member.address || '-'} {member.addressDetail || ''}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="content-card">
        <h6 className="mb-3">구매 이력 <span className="text-muted">({orders.length}건)</span></h6>
        <table className="table table-hover">
          <thead className="table-light">
            <tr>
              <th style={{ width: 60 }}>번호</th>
              <th>주문번호</th>
              <th className="text-end">결제금액</th>
              <th className="text-center">상태</th>
              <th className="text-center">주문일</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o, i) => (
              <tr key={o.id}>
                <td>{i + 1}</td>
                <td>{o.orderNumber}</td>
                <td className="text-end">{fmt(o.paymentAmount)}원</td>
                <td className="text-center"><span className="badge bg-info">{ORDER_STATUS_LABELS[o.status] || o.status}</span></td>
                <td className="text-center">{o.createdAt?.slice(0, 10)}</td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr><td colSpan={5} className="text-center text-muted py-4">구매 이력이 없습니다.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
