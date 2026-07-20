import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:9090/api',
  withCredentials: true,
  // CSRF: BO-XSRF-TOKEN 쿠키를 읽어 X-XSRF-TOKEN 헤더로 전송 (FO와 쿠키명 분리)
  xsrfCookieName: 'BO-XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  withXSRFToken: true,
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
