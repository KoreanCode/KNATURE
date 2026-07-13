import { useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const fmt = (n) => Number(n).toLocaleString();
const TYPE_LABELS = { ADMIN: '관리자', USE: '사용', REFUND: '취소환급' };

export default function DepositPage() {
  const [keyword, setKeyword] = useState('');
  const [members, setMembers] = useState([]);
  const [selected, setSelected] = useState(null); // {memberId, name, username, balance, history}
  const [adjust, setAdjust] = useState({ amount: '', reason: '' });

  const search = (e) => {
    e.preventDefault();
    api.get('/members', { params: { keyword, size: 10 } }).then((res) => setMembers(res.data.content));
  };

  const load = (memberId) => {
    api.get(`/deposits/${memberId}`).then((res) => setSelected(res.data));
  };

  const submitAdjust = (sign) => {
    const amount = Math.abs(parseInt(adjust.amount) || 0) * sign;
    if (!amount) { alert('금액을 입력해주세요.'); return; }
    api.post(`/deposits/${selected.memberId}`, { amount: String(amount), reason: adjust.reason })
      .then((res) => { alert(res.data.message); setAdjust({ amount: '', reason: '' }); load(selected.memberId); })
      .catch((err) => alert(err.response?.data?.message || '처리에 실패했습니다.'));
  };

  return (
    <>
      <TopBar title="예치금 관리" />
      <div className="content-card mb-3">
        <form className="d-flex gap-2" onSubmit={search}>
          <input className="form-control form-control-sm" style={{ width: 220 }} placeholder="회원 이름/아이디 검색" maxLength={50}
            value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <button className="btn btn-sm btn-outline-primary">검색</button>
        </form>
        {members.length > 0 && (
          <table className="table table-sm table-hover mt-3 mb-0">
            <thead className="table-light"><tr><th>아이디</th><th>이름</th><th>등급</th><th className="text-end">보유 예치금</th><th></th></tr></thead>
            <tbody>
              {members.map((m) => (
                <tr key={m.id}>
                  <td>{m.username}</td><td>{m.name}</td><td>{m.grade}</td>
                  <td className="text-end">{fmt(m.deposit ?? 0)}원</td>
                  <td className="text-center"><button className="btn btn-sm btn-outline-primary py-0" onClick={() => load(m.id)}>관리</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <small className="text-muted">예치금은 현금성 잔고로, 주문 결제 시 차감 사용됩니다. (취소 시 자동 환급)</small>
      </div>

      {selected && (
        <div className="content-card">
          <h6 className="mb-3">{selected.name} ({selected.username}) — 보유 <span className="text-primary fw-bold">{fmt(selected.balance)}원</span></h6>

          <div className="d-flex gap-2 align-items-center mb-3 p-2 bg-light rounded">
            <input type="number" min="0" max={99999999} className="form-control form-control-sm" style={{ width: 140 }} placeholder="금액"
              value={adjust.amount} onChange={(e) => setAdjust({ ...adjust, amount: e.target.value })} />
            <input className="form-control form-control-sm" style={{ width: 260 }} placeholder="사유 (필수)" maxLength={200}
              value={adjust.reason} onChange={(e) => setAdjust({ ...adjust, reason: e.target.value })} />
            <button className="btn btn-sm btn-primary" onClick={() => submitAdjust(1)}>지급</button>
            <button className="btn btn-sm btn-outline-danger" onClick={() => submitAdjust(-1)}>차감</button>
          </div>

          <table className="table table-sm">
            <thead className="table-light">
              <tr><th>일시</th><th>구분</th><th className="text-end">변동</th><th className="text-end">잔액</th><th>사유</th></tr>
            </thead>
            <tbody>
              {selected.history.map((h) => (
                <tr key={h.id}>
                  <td>{h.createdAt?.replace('T', ' ').slice(0, 16)}</td>
                  <td><span className="badge bg-secondary">{TYPE_LABELS[h.depositType]}</span></td>
                  <td className={`text-end fw-bold ${h.amount > 0 ? 'text-primary' : 'text-danger'}`}>{h.amount > 0 ? '+' : ''}{fmt(h.amount)}원</td>
                  <td className="text-end">{fmt(h.balanceAfter)}원</td>
                  <td className="small">{h.reason}</td>
                </tr>
              ))}
              {selected.history.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-3">예치금 내역이 없습니다.</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
