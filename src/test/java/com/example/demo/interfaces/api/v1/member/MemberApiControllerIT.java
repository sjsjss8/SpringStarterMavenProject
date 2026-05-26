package com.example.demo.interfaces.api.v1.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.global.common.api.ApiResponse;

/**
 * 회원 API v1 통합 테스트.
 *
 * <p>실제 HTTP 요청 → Controller → Service → H2 DB 전 계층을 관통하는 E2E 검증.</p>
 *
 * <ul>
 *   <li>RANDOM_PORT: 테스트마다 랜덤 포트로 서버 기동 (충돌 방지)</li>
 *   <li>@ActiveProfiles("test"): H2 인메모리 DB 사용 (실제 MariaDB 불필요)</li>
 *   <li>@TestMethodOrder: 등록 → 조회 → 수정 → 삭제 순서 보장</li>
 * </ul>
 *
 * <p>파일명 패턴 *IT.java → Maven Failsafe 가 실행 (mvn verify)</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("회원 API 통합 테스트 (v1)")
class MemberApiControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    static final String BASE_URL = "/api/v1/members";
    static final String TEST_ID  = "it_user01";

    // ── 등록 ──────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("회원 등록 성공 → 201 Created")
    void insert_success() {
        MemberRequestDto request = MemberRequestDto.builder()
                .id(TEST_ID)
                .name("통합테스트유저")
                .email("it@test.com")
                .build();

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(BASE_URL, request, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("회원 등록 실패 - 중복 ID → 409 Conflict")
    void insert_duplicateId() {
        MemberRequestDto request = MemberRequestDto.builder()
                .id(TEST_ID)
                .name("중복유저")
                .email("dup@test.com")
                .build();

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(BASE_URL, request, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    // ── 전체 조회 ─────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("전체 회원 조회 성공 → 200 OK")
    void findAll_success() {
        ResponseEntity<ApiResponse> response =
                restTemplate.getForEntity(BASE_URL, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── 단건 조회 ─────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("단건 조회 성공 → 200 OK")
    void findById_success() {
        ResponseEntity<ApiResponse> response =
                restTemplate.getForEntity(BASE_URL + "/" + TEST_ID, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("단건 조회 실패 - 존재하지 않는 ID → 404 Not Found")
    void findById_notFound() {
        ResponseEntity<ApiResponse> response =
                restTemplate.getForEntity(BASE_URL + "/not_exist_id", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    // ── 검색 ──────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("이름으로 검색 성공 → 200 OK")
    void search_byName() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                BASE_URL + "/search?searchType=name&searchKeyword=통합",
                ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── 수정 ──────────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("회원 수정 성공 → 200 OK")
    void update_success() {
        MemberRequestDto request = MemberRequestDto.builder()
                .id(TEST_ID)
                .name("수정된이름")
                .email("updated@test.com")
                .build();

        HttpEntity<MemberRequestDto> entity = new HttpEntity<>(request);
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/" + TEST_ID, HttpMethod.PUT, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @Order(8)
    @DisplayName("회원 수정 실패 - 존재하지 않는 ID → 404 Not Found")
    void update_notFound() {
        MemberRequestDto request = MemberRequestDto.builder()
                .id("not_exist_id")
                .name("이름")
                .email("email@test.com")
                .build();

        HttpEntity<MemberRequestDto> entity = new HttpEntity<>(request);
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/not_exist_id", HttpMethod.PUT, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    // ── 삭제 ──────────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("회원 삭제 성공 → 200 OK")
    void delete_success() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/" + TEST_ID, HttpMethod.DELETE, null, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("삭제 후 조회 → 404 Not Found")
    void findById_afterDelete() {
        ResponseEntity<ApiResponse> response =
                restTemplate.getForEntity(BASE_URL + "/" + TEST_ID, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
    }
}
