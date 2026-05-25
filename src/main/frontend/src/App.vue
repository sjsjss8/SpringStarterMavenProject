<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
</script>

<template>
  <header class="app-header">
    <h1>Spring Starter — Vue 3 SPA</h1>
    <nav>
      <RouterLink to="/">홈</RouterLink>
      <RouterLink to="/members">회원 목록</RouterLink>
    </nav>
  </header>

  <main class="app-main">
    <!-- 현재 URL 에 매칭된 View 컴포넌트가 여기에 렌더링됨 -->
    <RouterView />
  </main>

  <footer class="app-footer">
    <small>이 페이지는 Vue Router 기반 SPA — 페이지 이동 시 새로고침이 일어나지 않습니다.</small>
  </footer>
</template>

<style>
* { box-sizing: border-box; }
body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }

.app-header {
  padding: 1rem 2rem;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}
.app-header h1 { margin: 0 0 0.5rem; font-size: 1.25rem; }
.app-header nav a {
  margin-right: 1rem;
  color: #2563eb;
  text-decoration: none;
}
.app-header nav a.router-link-active { font-weight: bold; }

.app-main { padding: 1.5rem 2rem; }

.app-footer {
  padding: 1rem 2rem;
  border-top: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 0.875rem;
}
</style>
