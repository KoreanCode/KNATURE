import axios from 'axios';

export const API_ORIGIN = 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  withCredentials: true,
  // CSRF: XSRF-TOKEN 쿠키를 읽어 X-XSRF-TOKEN 헤더로 전송
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  withXSRFToken: true,
});

export default api;
