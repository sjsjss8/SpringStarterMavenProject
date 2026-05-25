import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

/**
 * Vue Router 설정 — 클라이언트 사이드 라우팅.
 *
 *   createWebHistory('/spa/')
 *     HTML5 History API 모드. 페이지 이동이 풀 로드 없이 일어나며 URL 도 자연스럽게 바뀜.
 *     base 가 '/spa/' 인 이유: Spring Boot 가 SPA 를 이 경로에서 호스팅하므로.
 *     예: /spa/members 클릭 → 서버 호출 없이 MemberListView 가 렌더링됨.
 *
 *   Spring Boot 측 대응:
 *     사용자가 /spa/members 를 직접 입력해도 정적 파일이 없으므로
 *     SpaWebMvcConfig 가 index.html 로 forward → Vue Router 가 그제서야 라우팅 수행.
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    // 코드 스플리팅 (lazy loading) — 필요 시점에 청크 로드
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/members',
    name: 'members',
    component: () => import('@/views/MemberListView.vue'),
  },
];

const router = createRouter({
  history: createWebHistory('/spa/'),
  routes,
});

export default router;
