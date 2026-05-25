<script setup lang="ts">
// 홈 뷰 — SPA 진입 안내 페이지. 외부 API 호출 없음.
</script>

<template>
  <section>
    <h2>Vue 3 SPA 데모</h2>
    <p>이 페이지는 <strong>Single Page Application</strong> 입니다.</p>
    <ul>
      <li>상단 네비게이션 클릭 시 페이지 새로고침 없이 화면이 바뀝니다 (Vue Router)</li>
      <li>회원 데이터는 백엔드 <code>/api/v1/members</code> 를 axios 로 호출해서 받아옵니다</li>
      <li>이 페이지 자체는 Spring Boot 의 <code>classpath:/static/spa/index.html</code> 한 장이 전부입니다</li>
    </ul>

    <h3>Thymeleaf 샘플과의 차이</h3>
    <table>
      <thead>
        <tr><th></th><th>Thymeleaf (/sample/members)</th><th>Vue SPA (/spa/members)</th></tr>
      </thead>
      <tbody>
        <tr><td>HTML 생성</td><td>서버에서</td><td>브라우저에서</td></tr>
        <tr><td>페이지 이동</td><td>풀 새로고침</td><td>JS 라우터 (새로고침 없음)</td></tr>
        <tr><td>서버 응답</td><td>HTML + 데이터</td><td>JSON 만</td></tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
table { border-collapse: collapse; margin-top: 1rem; }
th, td { padding: 0.5rem 1rem; border: 1px solid #e5e7eb; }
th { background: #f9fafb; text-align: left; }
</style>
