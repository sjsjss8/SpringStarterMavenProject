import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

/**
 * Vue 애플리케이션 진입점.
 *
 * createApp(App)        : 루트 컴포넌트 등록
 *   .use(router)        : Vue Router 플러그인 활성화 (라우팅 활성)
 *   .mount('#app')      : index.html 의 <div id="app"> 에 마운트
 */
createApp(App).use(router).mount('#app');
