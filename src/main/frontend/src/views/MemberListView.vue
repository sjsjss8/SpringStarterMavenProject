<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { memberApi, type Member } from '@/api/memberApi';

/**
 * 회원 목록 뷰 — 마운트 시 백엔드 REST API 를 호출해 데이터를 채운다.
 *
 * 이 컴포넌트가 보여주는 SPA 패턴:
 *   1. mounted 훅에서 axios → /api/v1/members 호출
 *   2. 반환된 JSON 으로 ref 상태 갱신
 *   3. <template> 의 v-for 가 자동으로 DOM 업데이트
 *
 * Thymeleaf 와 다른 점:
 *   - 서버는 JSON 만 응답. HTML 은 이 .vue 파일이 브라우저에서 그림.
 *   - 이 페이지로 직접 이동(/spa/members)해도 초기 HTML 은 항상 같은 index.html.
 */
const members = ref<Member[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

async function loadMembers() {
  loading.value = true;
  error.value = null;
  try {
    members.value = await memberApi.findAll();
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    loading.value = false;
  }
}

onMounted(loadMembers);
</script>

<template>
  <section>
    <div class="toolbar">
      <h2>회원 목록</h2>
      <button @click="loadMembers" :disabled="loading">
        {{ loading ? '로딩 중…' : '새로고침' }}
      </button>
    </div>

    <p v-if="error" class="error">⚠ {{ error }}</p>

    <table v-if="!loading && !error">
      <thead>
        <tr>
          <th>ID</th>
          <th>이름</th>
          <th>이메일</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in members" :key="m.id">
          <td>{{ m.id }}</td>
          <td>{{ m.name }}</td>
          <td>{{ m.email }}</td>
        </tr>
        <tr v-if="members.length === 0">
          <td colspan="3" class="empty">등록된 회원이 없습니다.</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; }
.toolbar button {
  padding: 0.5rem 1rem;
  border: 1px solid #2563eb;
  background: #2563eb;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}
.toolbar button:disabled { opacity: 0.5; cursor: wait; }

table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
th, td { padding: 0.5rem 1rem; border-bottom: 1px solid #e5e7eb; text-align: left; }
th { background: #f9fafb; }
.empty { color: #9ca3af; text-align: center; padding: 2rem; }
.error { color: #dc2626; }
</style>
