import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

/**
 * Vite 설정 — Spring Boot 통합 빌드
 *
 *   ─ 빌드 (mvn package) ───────────────────────────────────────────────
 *     이 프로젝트(src/main/frontend) 에서 `npm run build` 실행 시
 *     결과물이 ../resources/static/spa/ 로 출력됨.
 *     Spring Boot가 자동으로 classpath:/static/ 을 정적 리소스로 서빙하므로
 *     http://localhost:8081/spa/ 로 접속 가능.
 *
 *   ─ 개발 (npm run dev) ───────────────────────────────────────────────
 *     Vite Dev 서버가 5173 포트에서 기동 (HMR 지원).
 *     /api/v1/** 요청은 자동으로 Spring Boot (8081) 로 프록시되어 CORS 없이 개발.
 *     개발자는 http://localhost:5173/ 로 접속해서 코드 변경 즉시 반영 확인.
 */
export default defineConfig({
  plugins: [vue()],

  // SPA 가 /spa/ 하위 경로에서 호스팅되므로 base 를 맞춰줘야
  // 빌드된 HTML 의 <link>, <script> 경로가 /spa/assets/... 로 생성됨
  base: '/spa/',

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  // 빌드 결과물을 Spring Boot 의 static 리소스 경로로 출력
  build: {
    outDir: '../resources/static/spa',
    emptyOutDir: true,          // 매 빌드마다 기존 산출물 정리
    sourcemap: false,           // 운영 배포는 sourcemap 미포함 (필요 시 true)
  },

  // 개발 서버 — Vite Dev 모드 (npm run dev)
  server: {
    port: 5173,
    proxy: {
      // REST API 호출은 모두 Spring Boot 로 전달 (CORS 회피)
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // Actuator 도 같이 프록시 (헬스체크 확인 시 편의)
      '/actuator': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});
