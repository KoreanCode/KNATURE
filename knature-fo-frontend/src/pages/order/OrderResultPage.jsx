import { Link, useLocation } from 'react-router-dom';

const fmt = (n) => Number(n).toLocaleString();
const PAY_LABELS = { BANK_TRANSFER: '무통장입금', CREDIT_CARD: '신용카드', KAKAO_PAY: '카카오페이', NAVER_PAY: '네이버페이' };

export default function OrderResultPage() {
  const { state } = useLocation();
  if (!state) {
    return (
      <div className="container py-5 text-center">
        <p className="text-muted">주문 정보가 없습니다.</p>
        <Link to="/" className="btn btn-brand">메인으로</Link>
      </div>
    );
  }

  const isBank = state.paymentMethod === 'BANK_TRANSFER';

  return (
    <div className="container py-5 text-center" style={{ maxWidth: 560 }}>
      <i className="bi bi-check-circle text-brand" style={{ fontSize: '3.5rem' }}></i>
      <h4 className="fw-bold mt-3">주문이 완료되었습니다</h4>
      <p className="text-muted small">주문해주셔서 감사합니다. 마이페이지에서 주문 현황을 확인하실 수 있습니다.</p>

      <div className="border rounded p-4 text-start my-4">
        <div className="d-flex justify-content-between py-1"><span className="text-muted">주문번호</span><b>{state.orderNumber}</b></div>
        <div className="d-flex justify-content-between py-1"><span className="text-muted">결제수단</span><span>{PAY_LABELS[state.paymentMethod]}</span></div>
        <div className="d-flex justify-content-between py-1"><span className="text-muted">결제금액</span><b className="text-brand">{fmt(state.paymentAmount)}원</b></div>
        {state.receiver && (
          <div className="d-flex justify-content-between py-1">
            <span className="text-muted">배송지</span>
            <span className="text-end small">{state.receiver.receiverName} · {state.receiver.address} {state.receiver.addressDetail}</span>
          </div>
        )}
      </div>

      {state.guest && (
        <div className="alert alert-info text-start small">
          비회원 주문은 <Link to="/order/guest" className="fw-bold">비회원 주문 조회</Link>에서 주문번호와 주문 시 입력한 비밀번호로 확인하실 수 있습니다.
        </div>
      )}

      {isBank && (
        <div className="alert alert-warning text-start small">
          <b>입금 안내</b><br />
          {state.bank}<br />
          입금 금액: <b>{fmt(state.paymentAmount)}원</b><br />
          주문일로부터 <b>7일 이내 미입금 시 자동 취소</b>됩니다.
        </div>
      )}

      <div className="d-flex gap-2 justify-content-center mt-4">
        {state.guest
          ? <Link to="/order/guest" className="btn btn-outline-brand px-4">비회원 주문 조회</Link>
          : <Link to="/myshop/orders" className="btn btn-outline-brand px-4">주문 내역 보기</Link>}
        <Link to="/products" className="btn btn-brand px-4">쇼핑 계속하기</Link>
      </div>
    </div>
  );
}
