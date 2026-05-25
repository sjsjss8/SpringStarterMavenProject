package com.example.demo.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI(Swagger UI) 글로벌 설정.
 *
 * <p>이 프로젝트는 두 가지 UI 방식을 동시에 지원하므로 API 도 두 그룹으로 분리되어 있다:</p>
 * <ul>
 *   <li><b>v1</b> — Thymeleaf 페이지의 JS 가 호출 (안정성 우선, breaking change 금지)</li>
 *   <li><b>v2</b> — Vue 3 SPA 가 호출 (자유로운 응답 포맷 변경 가능)</li>
 * </ul>
 *
 * <p>Swagger UI ({@code /swagger-ui.html}) 상단 드롭다운에서 v1/v2 를 선택해 볼 수 있다.</p>
 *
 * <p>접속 경로 ({@code application.yml} 의 {@code springdoc} 설정 기준):</p>
 * <ul>
 *   <li>Swagger UI:  {@code http://localhost:8081/swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code http://localhost:8081/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    /** 앱 전체 메타데이터 (제목/버전/연락처 등). */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SpringStarterMavenProject API")
                        .version("v1.0")
                        .description("""
                                Spring Boot 스타터 프로젝트의 REST API 명세서.

                                **API 버전 정책**
                                - `v1` (`/api/v1/**`): Thymeleaf 식 UI 의 JS 가 호출. 하위 호환 유지 필수.
                                - `v2` (`/api/v2/**`): Vue 3 SPA 가 호출. 신규 필드/포맷 자유롭게 도입 가능.

                                **공통 응답 포맷**
                                모든 응답은 `ApiResponse<T>` 로 감싸진다:
                                ```json
                                { "success": true, "data": { ... } }
                                ```
                                실패 시:
                                ```json
                                { "success": false, "error": { "code": "...", "message": "..." } }
                                ```
                                """)
                        .contact(new Contact()
                                .name("Backend Team")
                                .email("sangjin.yoo@initech.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addServersItem(new Server()
                        .url("http://localhost:8081")
                        .description("로컬 / Docker 컨테이너 (기본)"))
                .addServersItem(new Server()
                        .url("https://dev.example.com")
                        .description("개발 서버 (dev 프로파일)"));
    }

    /** v1 — Thymeleaf 호환 API 그룹. */
    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1 (Thymeleaf 호환)")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    /** v2 — SPA 전용 API 그룹. */
    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
                .group("v2 (SPA 전용)")
                .pathsToMatch("/api/v2/**")
                .build();
    }
}
