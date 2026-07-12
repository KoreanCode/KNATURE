import { useEffect, useState } from 'react';
import api from '../../api/client';
import MyLayout from './MyLayout';
import { openPostcode } from '../../utils/postcode';

const EMPTY = { alias: '', receiverName: '', receiverPhone: '', zipcode: '', address: '', addressDetail: '', isDefault: false };

export default function MyAddressPage() {
  const [addresses, setAddresses] = useState([]);
  const [form, setForm] = useState(null); // {id?, ...EMPTY}

  const load = () => api.get('/addresses').then((res) => setAddresses(res.data));
  useEffect(() => { load(); }, []);

  const save = async () => {
    const body = { ...form, isDefault: String(form.isDefault) };
    try {
      if (form.id) await api.put(`/addresses/${form.id}`, body);
      else await api.post('/addresses', body);
      alert('저장되었습니다.');
      setForm(null);
      load();
    } catch (err) {
      alert(err.response?.data?.message || '저장에 실패했습니다.');
    }
  };

  const remove = async (a) => {
    if (!confirm(`'${a.alias}' 배송지를 삭제하시겠습니까?`)) return;
    await api.delete(`/addresses/${a.id}`);
    load();
  };

  const set = (k) => (e) => setForm((prev) => ({ ...prev, [k]: e.target.value }));

  return (
    <MyLayout title="배송지 관리">
      <div className="d-flex justify-content-end mb-2">
        <button className="btn btn-sm btn-brand" onClick={() => setForm({ ...EMPTY })}>+ 배송지 추가</button>
      </div>

      {addresses.length === 0 && <div className="text-center text-muted py-4">등록된 배송지가 없습니다.</div>}
      {addresses.map((a) => (
        <div key={a.id} className="border rounded p-3 mb-2 d-flex justify-content-between align-items-center">
          <div className="small">
            <b>{a.alias}</b> {a.isDefault && <span className="badge bg-brand-light text-brand border ms-1">기본</span>}
            <div>{a.receiverName} · {a.receiverPhone}</div>
            <div className="text-muted">[{a.zipcode}] {a.address} {a.addressDetail}</div>
          </div>
          <div className="d-flex gap-1">
            <button className="btn btn-sm btn-outline-brand" onClick={() => setForm({ ...a, isDefault: a.isDefault })}>수정</button>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => remove(a)}>삭제</button>
          </div>
        </div>
      ))}

      {form && (
        <div className="border rounded p-3 mt-3" style={{ maxWidth: 520 }}>
          <b className="d-block mb-2">{form.id ? '배송지 수정' : '배송지 추가'}</b>
          <input className="form-control form-control-sm mb-1" placeholder="배송지명 (예: 집, 회사)" maxLength={50} value={form.alias} onChange={set('alias')} />
          <div className="d-flex gap-1 mb-1">
            <input className="form-control form-control-sm" placeholder="수령인 *" maxLength={50} value={form.receiverName} onChange={set('receiverName')} />
            <input className="form-control form-control-sm" placeholder="연락처 *" maxLength={20} value={form.receiverPhone} onChange={set('receiverPhone')} />
          </div>
          <div className="d-flex gap-1 mb-1">
            <input className="form-control form-control-sm" style={{ maxWidth: 120 }} placeholder="우편번호 *" value={form.zipcode} readOnly onChange={set('zipcode')} />
            <input className="form-control form-control-sm" placeholder="주소 *" value={form.address} readOnly onChange={set('address')} />
            <button type="button" className="btn btn-sm btn-outline-brand flex-shrink-0"
              onClick={() => openPostcode(({ zipcode, address }) => setForm((p) => ({ ...p, zipcode, address })))}>주소 검색</button>
          </div>
          <input className="form-control form-control-sm mb-2" placeholder="상세주소" maxLength={100} value={form.addressDetail || ''} onChange={set('addressDetail')} />
          <div className="form-check mb-2">
            <input type="checkbox" className="form-check-input" id="isDefault" checked={!!form.isDefault}
              onChange={(e) => setForm((p) => ({ ...p, isDefault: e.target.checked }))} />
            <label className="form-check-label small" htmlFor="isDefault">기본 배송지로 설정</label>
          </div>
          <div className="d-flex gap-2">
            <button className="btn btn-sm btn-brand" onClick={save}>저장</button>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setForm(null)}>취소</button>
          </div>
        </div>
      )}
    </MyLayout>
  );
}
