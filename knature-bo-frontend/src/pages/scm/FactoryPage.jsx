import { useEffect, useState } from 'react';
import api from '../../api/client';
import TopBar from '../../components/TopBar';

const fmt = (n) => Number(n).toLocaleString();
const EMPTY = { name: '', bizNumber: '', address: '', phone: '', managerName: '', leadTimeDays: 7, region: '' };

export default function FactoryPage() {
  const [factories, setFactories] = useState([]);
  const [form, setForm] = useState(null);
  const [managerFor, setManagerFor] = useState(null); // {factoryId, name, username, password, adminName}
  const [mappingFor, setMappingFor] = useState(null); // {factory, mappings, products, new:{productId,unitCost,moq,leadTimeDays}}

  const load = () => api.get('/factories').then((res) => setFactories(res.data));
  useEffect(() => { load(); }, []);

  const set = (k) => (e) => setForm((p) => ({ ...p, [k]: e.target.value }));

  const save = () => {
    const req = form.id ? api.put(`/factories/${form.id}`, form) : api.post('/factories', form);
    req.then(() => { alert('저장되었습니다.'); setForm(null); load(); })
      .catch((err) => alert(err.response?.data?.message || '저장에 실패했습니다.'));
  };

  const createManager = () => {
    api.post(`/factories/${managerFor.factoryId}/manager`, {
      username: managerFor.username, password: managerFor.password, name: managerFor.adminName,
    })
      .then((res) => { alert(res.data.message); setManagerFor(null); })
      .catch((err) => alert(err.response?.data?.message || '생성에 실패했습니다.'));
  };

  const openMapping = async (f) => {
    const [mappings, products] = await Promise.all([
      api.get(`/factories/${f.id}/products`).then((r) => r.data),
      api.get('/products', { params: { size: 60 } }).then((r) => r.data.content),
    ]);
    setMappingFor({ factory: f, mappings, products, new: { productId: '', unitCost: '', moq: '', leadTimeDays: '' } });
  };

  const addMapping = () => {
    api.post(`/factories/${mappingFor.factory.id}/products`, mappingFor.new)
      .then(() => openMapping(mappingFor.factory))
      .catch((err) => alert(err.response?.data?.message || '매핑 추가에 실패했습니다.'));
  };

  const removeMapping = (m) => {
    api.delete(`/factories/${mappingFor.factory.id}/products/${m.id}`)
      .then(() => openMapping(mappingFor.factory));
  };

  return (
    <>
      <TopBar title="공장(공급사) 관리" />
      <div className="content-card">
        <div className="d-flex justify-content-end mb-2">
          <button className="btn btn-sm btn-primary" onClick={() => setForm({ ...EMPTY })}>+ 공장 등록</button>
        </div>
        <table className="table table-hover">
          <thead className="table-light">
            <tr><th>공장명</th><th>사업자번호</th><th>연락처</th><th>담당자</th><th>담당지역</th><th className="text-center">리드타임</th><th className="text-center">상태</th><th className="text-center" style={{ width: 260 }}>관리</th></tr>
          </thead>
          <tbody>
            {factories.map((f) => (
              <tr key={f.id}>
                <td className="fw-semibold">{f.name}</td>
                <td>{f.bizNumber || '-'}</td>
                <td>{f.phone || '-'}</td>
                <td>{f.managerName || '-'}</td>
                <td className="small">{f.region || '-'}</td>
                <td className="text-center">{f.leadTimeDays}일</td>
                <td className="text-center">{f.active ? <span className="badge bg-success">거래중</span> : <span className="badge bg-secondary">중지</span>}</td>
                <td className="text-center">
                  <button className="btn btn-sm btn-outline-primary py-0 me-1" onClick={() => setForm({ ...f })}>수정</button>
                  <button className="btn btn-sm btn-outline-success py-0 me-1" onClick={() => openMapping(f)}>상품 매핑</button>
                  <button className="btn btn-sm btn-outline-secondary py-0"
                    onClick={() => setManagerFor({ factoryId: f.id, name: f.name, username: '', password: '', adminName: '' })}>담당자 계정</button>
                </td>
              </tr>
            ))}
            {factories.length === 0 && <tr><td colSpan={7} className="text-center text-muted py-4">등록된 공장이 없습니다.</td></tr>}
          </tbody>
        </table>
      </div>

      {form && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setForm(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">{form.id ? '공장 수정' : '공장 등록'}</h6>
                <button type="button" className="btn-close" onClick={() => setForm(null)} /></div>
              <div className="modal-body">
                <input className="form-control form-control-sm mb-2" placeholder="공장명 *" value={form.name} maxLength={100} onChange={set('name')} />
                <input className="form-control form-control-sm mb-2" placeholder="사업자번호" value={form.bizNumber || ''} maxLength={100} onChange={set('bizNumber')} />
                <input className="form-control form-control-sm mb-2" placeholder="주소" value={form.address || ''} maxLength={200} onChange={set('address')} />
                <input className="form-control form-control-sm mb-2" placeholder="연락처" value={form.phone || ''} maxLength={20} onChange={set('phone')} />
                <input className="form-control form-control-sm mb-2" placeholder="담당자명" value={form.managerName || ''} maxLength={50} onChange={set('managerName')} />
                <label className="form-label small mb-0">담당 지역 <small className="text-muted">(쉼표 구분 — 주문 배송지가 이 지역이면 자동발주 시 이 공장 우선)</small></label>
                <input className="form-control form-control-sm mb-2" placeholder="예: 서울,경기,인천" value={form.region || ''} maxLength={200} onChange={set('region')} />
                <label className="form-label small mb-0">기본 리드타임 (일)</label>
                <input type="number" min="0" max={365} className="form-control form-control-sm" value={form.leadTimeDays} onChange={set('leadTimeDays')} />
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setForm(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={save}>저장</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {managerFor && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setManagerFor(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">공장관리자 계정 — {managerFor.name}</h6>
                <button type="button" className="btn-close" onClick={() => setManagerFor(null)} /></div>
              <div className="modal-body">
                <p className="small text-muted">이 계정으로 BO 로그인 시 <b>자기 공장 발주 확인/생산상태 업데이트, 공장 재고</b>만 접근됩니다.</p>
                <input className="form-control form-control-sm mb-2" placeholder="아이디 *" maxLength={50} value={managerFor.username}
                  onChange={(e) => setManagerFor({ ...managerFor, username: e.target.value })} />
                <input type="password" className="form-control form-control-sm mb-2" placeholder="비밀번호 *" maxLength={64} value={managerFor.password}
                  onChange={(e) => setManagerFor({ ...managerFor, password: e.target.value })} />
                <input className="form-control form-control-sm" placeholder="담당자 이름 *" maxLength={50} value={managerFor.adminName}
                  onChange={(e) => setManagerFor({ ...managerFor, adminName: e.target.value })} />
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary btn-sm" onClick={() => setManagerFor(null)}>취소</button>
                <button className="btn btn-primary btn-sm" onClick={createManager}>계정 생성</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {mappingFor && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }} onClick={() => setMappingFor(null)}>
          <div className="modal-dialog modal-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header"><h6 className="modal-title">상품 매핑 — {mappingFor.factory.name}</h6>
                <button type="button" className="btn-close" onClick={() => setMappingFor(null)} /></div>
              <div className="modal-body">
                <table className="table table-sm">
                  <thead className="table-light"><tr><th>상품</th><th className="text-end">생산단가</th><th className="text-end">MOQ</th><th className="text-center">리드타임</th><th></th></tr></thead>
                  <tbody>
                    {mappingFor.mappings.map((m) => (
                      <tr key={m.id}>
                        <td>{m.product?.name}</td>
                        <td className="text-end">{fmt(m.unitCost)}원</td>
                        <td className="text-end">{m.moq}개</td>
                        <td className="text-center">{m.leadTimeDays ?? '공장기본'}일</td>
                        <td className="text-center"><button className="btn btn-sm btn-outline-danger py-0" onClick={() => removeMapping(m)}>삭제</button></td>
                      </tr>
                    ))}
                    {mappingFor.mappings.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-2">매핑된 상품이 없습니다.</td></tr>}
                  </tbody>
                </table>
                <div className="d-flex gap-1 align-items-center bg-light rounded p-2">
                  <select className="form-select form-select-sm" style={{ maxWidth: 220 }} value={mappingFor.new.productId}
                    onChange={(e) => setMappingFor({ ...mappingFor, new: { ...mappingFor.new, productId: e.target.value } })}>
                    <option value="">상품 선택</option>
                    {mappingFor.products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                  </select>
                  <input type="number" min="0" max={99999999} className="form-control form-control-sm" style={{ width: 110 }} placeholder="단가"
                    value={mappingFor.new.unitCost} onChange={(e) => setMappingFor({ ...mappingFor, new: { ...mappingFor.new, unitCost: e.target.value } })} />
                  <input type="number" min="0" max={999999} className="form-control form-control-sm" style={{ width: 90 }} placeholder="MOQ"
                    value={mappingFor.new.moq} onChange={(e) => setMappingFor({ ...mappingFor, new: { ...mappingFor.new, moq: e.target.value } })} />
                  <input type="number" min="0" max={365} className="form-control form-control-sm" style={{ width: 100 }} placeholder="리드타임"
                    value={mappingFor.new.leadTimeDays} onChange={(e) => setMappingFor({ ...mappingFor, new: { ...mappingFor.new, leadTimeDays: e.target.value } })} />
                  <button className="btn btn-sm btn-primary flex-shrink-0" onClick={addMapping}>추가</button>
                </div>
                <small className="text-muted">매핑은 자동 발주의 기준 데이터입니다 (최저 단가 공장으로 발주 생성).</small>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
