package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 애플리케이션 컨텍스트 로딩 테스트
 * - @ActiveProfiles("test") : H2 인메모리 DB 사용 (MariaDB 불필요)
 * - CI/CD (Jenkins) 환경에서도 실제 DB 없이 통과
 */
@SpringBootTest
@ActiveProfiles("test")
class MemberApplicationTests {

	@Test
	void contextLoads() {
	}

}
