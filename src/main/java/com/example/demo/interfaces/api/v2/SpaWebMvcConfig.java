package com.example.demo.interfaces.api.v2;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Vue.js SPA 정적 리소스 + History 모드 fallback 설정 (v2 — SPA 식 진입점).
 *
 * <p>SPA 가 Vue Router 의 HTML5 History 모드를 쓰기 때문에 다음과 같은 문제가 생긴다:</p>
 *
 * <pre>
 *   사용자가 /spa/members 를 브라우저 주소창에 직접 입력했을 때
 *   → Spring Boot 는 /spa/members 라는 실제 파일을 찾으려 함
 *   → 그런 파일은 없음 → 404 발생
 *   → 하지만 우리는 /spa/index.html 을 응답하고 Vue Router 가 라우팅하길 원함
 * </pre>
 *
 * <p>이 설정은 다음 규칙을 적용한다:</p>
 * <ol>
 *   <li>/spa/&#42;&#42; 요청 → classpath:/static/spa/ 에서 실제 파일을 먼저 찾는다 (정적 자산)</li>
 *   <li>실제 파일이 없으면 → /spa/index.html 을 반환 (SPA fallback)</li>
 *   <li>그러면 브라우저에서 Vue Router 가 현재 URL 을 보고 알맞은 컴포넌트를 렌더링</li>
 * </ol>
 *
 * <p>주의: 이 설정은 /spa/&#42;&#42; 에만 적용. /api/v2/&#42;&#42; 와 v1 의 Thymeleaf 페이지는 영향 없음.</p>
 */
@Configuration
public class SpaWebMvcConfig implements WebMvcConfigurer {

    private static final String SPA_BASE_PATH = "/spa/";
    private static final String SPA_STATIC_LOCATION = "classpath:/static/spa/";
    private static final String SPA_INDEX = "static/spa/index.html";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(SPA_BASE_PATH + "**")
                .addResourceLocations(SPA_STATIC_LOCATION)
                .resourceChain(true)
                .addResolver(new SpaFallbackResolver());
    }

    /**
     * 정적 파일이 없을 때 index.html 을 반환하는 커스텀 리졸버.
     * 실제 파일(예: /spa/assets/index-abc123.js)은 그대로 서빙하고,
     * Vue Router URL(예: /spa/members)은 index.html 로 대체.
     */
    private static class SpaFallbackResolver extends PathResourceResolver {
        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable()) {
                return requested;
            }
            // fallback — Vue Router 가 처리하도록 index.html 반환
            return new ClassPathResource(SPA_INDEX);
        }
    }
}
