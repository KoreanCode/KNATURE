import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const SHOP_FIELDS = [
  { key: 'shop.name', label: '상호', placeholder: '아람티앤씨' },
  { key: 'shop.ceo', label: '대표자' },
  { key: 'shop.bizNumber', label: '사업자등록번호' },
  { key: 'shop.address', label: '주소' },
  { key: 'shop.tel', label: '대표 연락처', placeholder: '1544-1089' },
  { key: 'shop.email', label: '대표 이메일' },
  { key: 'shop.bank', label: '입금 계좌 (무통장)', placeholder: '우리은행 1005-000-000000 (예금주: 아람티앤씨)' },
];

const DELIVERY_FIELDS = [
  { key: 'delivery.baseFee', label: '기본 배송비 (원)', placeholder: '0 (현재 전상품 무료배송)' },
  { key: 'delivery.freeThreshold', label: '무료배송 기준금액 (원)', placeholder: '0 = 항상 무료' },
  { key: 'delivery.remoteAreaFee', label: '산간벽지 추가비용 (원)' },
];

const MILEAGE_FIELDS = [
  { key: 'mileage.joinBonus', label: '가입 적립금 (P)', placeholder: '기본 5000' },
  { key: 'mileage.reviewBonus', label: '리뷰 적립금 (P)', placeholder: '기본 500' },
  { key: 'mileage.usableAfterDays', label: '구매 적립 사용 가능 시점 (배송완료 후 N일, 0=즉시)', type: 'number', min: 0, max: 365, defaultValue: '20' },
];

export default function SettingsPage() {
  const [tab, setTab] = useState('shop');
  const [settings, setSettings] = useState({});
  const [admins, setAdmins] = useState([]);
  const [adminForm, setAdminForm] = useState(null); // {id?, username, password, name}
  const [me, setMe] = useState(null);

  useEffect(() => {
    api.get('/settings').then((res) => setSettings(res.data));
    api.get('/auth/me').then((res) => setMe(res.data));
    loadAdmins();
  }, []);

  const loadAdmins = () => api.get('/admins').then((res) => setAdmins(res.data));

  const setVal = (key) => (e) => setSettings({ ...settings, [key]: e.target.value });

  const saveSettings = () => {
    api.put('/settings', settings).then(() => alert('설정이 저장되었습니다.'));
  };

  const saveAdmin = () => {
    const req = adminForm.id
      ? api.put(`/admins/${adminForm.id}`, { name: adminForm.name, password: adminForm.password })
      : api.post('/admins', { username: adminForm.username, password: adminForm.password, name: adminForm.name });
    req.then(() => { alert('저장되었습니다.'); setAdminForm(null); loadAdmins(); })
      .catch((err) => alert(err.response?.data?.message || '저장에 실패했습니다.'));
  };

  const deleteAdmin = (a) => {
    if (!confirm(`'${a.username}' 계정을 삭제하시겠습니까?`)) return;
    api.delete(`/admins/${a.id}`)
      .then(() => { alert('삭제되었습니다.'); loadAdmins(); })
      .catch((err) => alert(err.response?.data?.message || '삭제에 실패했습니다.'));
  };

  const renderFields = (fields) => (
    <>
      {fields.map((f) => (
        <div className="row mb-2 align-items-center" key={f.key}>
          <label className="col-sm-3 col-form-label fw-bold">{f.label}</label>
          <div className="col-sm-6">
            {f.type === 'number' ? (
              <input type="number" className="form-control form-control-sm" value={settings[f.key] ?? f.defaultValue ?? ''}
                min={f.min} max={f.max}
                placeholder={f.placeholder || ''} onChange={setVal(f.key)} />
            ) : (
              <input type="text" className="form-control form-control-sm" value={settings[f.key] || ''}
                maxLength={f.key === 'shop.address' ? 200 : f.key === 'shop.tel' ? 20 : 100}
                placeholder={f.placeholder || ''} onChange={setVal(f.key)} />
            )}
          </div>
        </div>
      ))}
      <button className="btn btn-primary btn-sm mt-2" onClick={saveSettings}>저장</button>
    </>
  );

  return (
    <>
      <TopBar title="설정" />
      <div className="content-card">
        <ul className="nav nav-tabs mb-3">
          {[['shop', '상점 정보'], ['delivery', '배송 정책'], ['mileage', '적립금 정책'], ['admins', '관리자 계정']].map(([k, label]) => (
            <li className="nav-item" key={k}>
              <button className={`nav-link ${tab === k ? 'active' : ''}`} onClick={() => setTab(k)}>{label}</button>
            </li>
          ))}
        </ul>

        {tab === 'shop' && renderFields(SHOP_FIELDS)}
        {tab === 'delivery' && renderFields(DELIVERY_FIELDS)}
        {tab === 'mileage' && renderFields(MILEAGE_FIELDS)}

        {tab === 'admins' && (
          <>
            <div className="d-flex justify-content-end mb-2">
              <button className="btn btn-sm btn-primary" onClick={() => setAdminForm({ username: '', password: '', name: '' })}>+ 계정 등록</button>
            </div>
            <table className="table table-hover">
              <thead className="table-light">
                <tr><th style={{ width: 60 }}>번호</th><th>아이디</th><th>이름</th><th>권한</th><th>등록일</th><th className="text-center" style={{ width: 160 }}>관리</th></tr>
              </thead>
              <tbody>
                {admins.map((a, i) => (
                  <tr key={a.id}>
                    <td>{i + 1}</td>
                    <td>{a.username}{me?.username === a.username && <span className="badge bg-info ms-1">본인</span>}</td>
                    <td>{a.name}</td>
                    <td>{a.role === 'SUPER_ADMIN' ? '최고관리자' : '공장관리자'}</td>
                    <td>{a.createdAt?.slice(0, 10)}</td>
                    <td className="text-center">
                      <button className="btn btn-sm btn-outline-primary me-1"
                        onClick={() => setAdminForm({ id: a.id, username: a.username, password: '', name: a.name })}>수정</button>
                      <button className="btn btn-sm btn-outline-danger" disabled={me?.username === a.username}
                        onClick={() => deleteAdmin(a)}>삭제</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )}
      </div>

      {adminForm && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setAdminForm(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header">
                <h6 className="modal-title">{adminForm.id ? '계정 수정' : '계정 등록'}</h6>
                <button type="button" className="btn-close" onClick={() => setAdminForm(null)} />
              </div>
              <div className="modal-body">
                <div className="mb-2">
                  <label className="form-label">아이디 *</label>
                  <input type="text" className="form-control" value={adminForm.username} maxLength={50} disabled={!!adminForm.id}
                    onChange={(e) => setAdminForm({ ...adminForm, username: e.target.value })} />
                </div>
                <div className="mb-2">
                  <label className="form-label">비밀번호 {adminForm.id ? '(변경 시에만 입력)' : '*'}</label>
                  <input type="password" className="form-control" value={adminForm.password} maxLength={64}
                    onChange={(e) => setAdminForm({ ...adminForm, password: e.target.value })} />
                </div>
                <div>
                  <label className="form-label">이름 *</label>
                  <input type="text" className="form-control" value={adminForm.name} maxLength={50}
                    onChange={(e) => setAdminForm({ ...adminForm, name: e.target.value })} />
                </div>
                <small className="text-muted">1차에서는 최고관리자 권한만 등록됩니다. (공장관리자는 2차)</small>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setAdminForm(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={saveAdmin}>저장</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
