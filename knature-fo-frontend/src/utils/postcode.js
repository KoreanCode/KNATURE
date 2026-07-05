// 다음(카카오) 우편번호 서비스 — 주소 검색 팝업 (명세: 배송지 주소 입력)
const SCRIPT_URL = 'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';

let loading = null;

function loadScript() {
  if (window.daum?.Postcode) return Promise.resolve();
  if (loading) return loading;
  loading = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = SCRIPT_URL;
    script.onload = resolve;
    script.onerror = () => { loading = null; reject(new Error('우편번호 서비스 로드 실패')); };
    document.head.appendChild(script);
  });
  return loading;
}

/** 주소 검색 팝업 → {zipcode, address} 콜백 */
export async function openPostcode(onComplete) {
  try {
    await loadScript();
    new window.daum.Postcode({
      oncomplete: (data) => {
        onComplete({ zipcode: data.zonecode, address: data.roadAddress || data.jibunAddress });
      },
    }).open();
  } catch {
    alert('우편번호 서비스를 불러오지 못했습니다. 주소를 직접 입력해주세요.');
  }
}
