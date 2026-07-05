import api from '../api/client';

/** 인증 세션 포함 파일 다운로드 (blob) — 실패 시 사용자에게 알림 */
export function downloadFile(apiPath, filename, params = {}) {
  return api.get(apiPath, { params, responseType: 'blob' })
    .then((res) => {
      const url = URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      // 다운로드 시작 여유를 두고 해제
      setTimeout(() => URL.revokeObjectURL(url), 3000);
    })
    .catch((err) => {
      alert(`다운로드에 실패했습니다. (${err.response?.status || '네트워크 오류'}) 다시 로그인 후 시도해주세요.`);
    });
}
