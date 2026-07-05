import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';

export default function Footer() {
  const [info, setInfo] = useState({});

  useEffect(() => {
    api.get('/shop-info').then((res) => setInfo(res.data)).catch(() => {});
  }, []);

  return (
    <footer className="fo-footer mt-5">
      <div className="container py-4">
        <div className="d-flex flex-wrap justify-content-between gap-3">
          <div>
            <div className="fw-bold text-dark mb-2">KNATURE</div>
            <div>{info['shop.name'] || '아람티앤씨'}{info['shop.ceo'] ? ` | 대표: ${info['shop.ceo']}` : ''}</div>
            {info['shop.bizNumber'] && <div>사업자등록번호: {info['shop.bizNumber']}</div>}
            {info['shop.address'] && <div>{info['shop.address']}</div>}
            <div>고객센터: {info['shop.tel'] || '1544-1089'}{info['shop.email'] ? ` | ${info['shop.email']}` : ''}</div>
            {info['shop.bank'] && <div>입금계좌: {info['shop.bank']}</div>}
          </div>
          <div className="d-flex flex-column gap-1">
            <Link to="/page/guide">이용안내</Link>
            <Link to="/page/agreement">이용약관</Link>
            <Link to="/page/privacy">개인정보처리방침</Link>
          </div>
          <div className="d-flex gap-3 fs-5">
            <a href="https://instagram.com" target="_blank" rel="noreferrer" aria-label="인스타그램"><i className="bi bi-instagram"></i></a>
            <a href="https://youtube.com" target="_blank" rel="noreferrer" aria-label="유튜브"><i className="bi bi-youtube"></i></a>
            <a href="https://pf.kakao.com" target="_blank" rel="noreferrer" aria-label="카카오톡"><i className="bi bi-chat-fill"></i></a>
          </div>
        </div>
        <div className="mt-3 pt-3 border-top small">© KNATURE. All rights reserved.</div>
      </div>
    </footer>
  );
}
