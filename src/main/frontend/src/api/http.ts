import axios from 'axios';

/**
 * Axios 공통 인스턴스 — 모든 백엔드 호출이 이걸 거치도록 설계.
 *
 *   baseURL: '/api/v1'
 *     - 개발 모드 (Vite): /api/v1 → http://localhost:8081 로 자동 프록시 (vite.config.ts 의 server.proxy)
 *     - 운영 모드 (JAR):  Spring Boot 가 동일 origin 에서 /api/v1 를 노출하므로 그대로 동작
 *
 *   인터셉터 — 표준 응답 래퍼 {success, data, error} 자동 풀기:
 *     백엔드가 항상 ApiResponse<T> 형태로 응답하므로,
 *     성공 시 response.data.data 만 호출자에게 전달 → 컴포넌트 코드가 깔끔해짐.
 */
const http = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 응답 인터셉터 — ApiResponse<T> 언래핑 + 에러 통합 처리
http.interceptors.response.use(
  (response) => {
    const body = response.data;
    if (body && typeof body === 'object' && 'success' in body) {
      if (body.success) {
        return { ...response, data: body.data };
      }
      return Promise.reject(new Error(body.error?.message ?? 'Unknown error'));
    }
    return response;
  },
  (error) => {
    const message = error.response?.data?.error?.message ?? error.message ?? 'Network error';
    return Promise.reject(new Error(message));
  }
);

export default http;
