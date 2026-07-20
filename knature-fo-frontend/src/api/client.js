import axios from 'axios';

export const API_ORIGIN = 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  withCredentials: true,
  // CSRF: FO-XSRF-TOKEN 쿠키를 읽어 X-XSRF-TOKEN 헤더로 전송 (BO와 쿠키명 분리)
  xsrfCookieName: 'FO-XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  withXSRFToken: true,
});

export default api;
