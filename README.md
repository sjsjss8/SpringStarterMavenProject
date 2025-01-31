1. 현대적인 개발 아키텍처
[Frontend]
 - SPA + REST API
  : React/Vue.js/Angular
  : API 통신
  : 클라이언트 사이드 렌더링
  
 * 최근에는 JSP가 레거시 취급된다. 그 이유는 다음과 같다.
   - JSP가 처음 요청되거나 변경되면 서블릿으로 변환되고 컴파일 된 후 실행되는 과정을 거쳐야 하기 때문에 레이턴시가 발생하는 문제
   - 또한 JSP를 사용하면 스파게티 코드가 될 수 있는 상황에 놓여진다.
   - 특히나 최근에 많이 사용하는 스프링 부트의 경우엔 내장 톰캣을 사용하는 실행가능한 JAR파일 빌드할 경우 JSP를 인식할 수 없다.
   그렇기 때문에 굳이 SPA 프레임워크를 사용하지 않더라도 JSP대신 HTML, CSS, JS를 분리해서 사용하는것이 최신 트렌드이다.
  
[Backend]
 - Spring Boot
  : REST API 제공
  : JSON 응답
  : 비즈니스 로직 처리
  
2. 스프링 웹 애플리케이션 계층과 각 계층별 사용되는 클래스 및 어노테이션
* 계층을 나누는 이유 : 애플리케이션의 역할과 책임을 논리적으로 분리한 구조

[Presentation Layer] // 외부 요청을 받고 응답을 반환하는 계층. 즉 외부에 '보여지는(Presentation)' 계층
 - Controller > @RestController, @Controller, @RequestMapping, @Autowired..
 - DTO (Data Transfer Object) > @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor, @ToString..
 - Validator > @Component..
 - ExceptionHandler > @RestControllerAdvice, @ExceptionHandler(Exception.class)..
 - Filter > @Component..
 - Interceptor > @Component..
 
[Business Layer] // 실제 비즈니스 로직을 처리하는 계층
 - Service
  • Service Interface
  • Service Implementation > @Service, @Transactional, @RequiredArgsConstructor, @Autowired..
 - Domain Model > @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor, @ToString..
 - Exception
 - Event > @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor, @ToString..
 
[Data Access Layer 또는 Persistence Layer] // 데이터베이스와의 통신을 담당하는 계층
 - Repository(JPA 사용시) > @Repository..
  • Repository Interface
  • Repository Implementation (필요한 경우)
 - QueryDSL(JPA 사용시)
 - Mapper(Mybatis 사용시)
  • Mapper Interface > @Mapper..
  • Mapper Implementation(XML Mapper)
 - DAO
  • DAO Interface > @Repository, @Mapper, @RequiredArgsConstructor..
  • DAO Implementation
 - Entity > @Entity, @Table(name = "Identity"), @Getter, @Setter, @Id, @GeneratedValue, @Column..
 
 * JPA를 사용할 때는 일반적으로 Repository Interface, Repository Implementation, DAO를 사용하는것이 표준적인 방법입니다.
 * MyBatis를 사용할 때는 일반적으로 DAO개념과 같은 Mapper를 사용하는 것이 표준적인 방법입니다.
   하지만 Repository 패턴을 적용하고 싶다면 Mapper를 감싸는 Repository를 만들 수 있습니다.

[Infrastructure Layer] //애플리케이션 전반에서 필요한 환경 및 설정을 제공
 - Configuration(애플리케이션의 각종 설정을 담당) > @Configuration, @EnableWebMvc, @ComponentScan, @PropertySource, @Value, @Bean...
  • WebMvcConfig (Spring MVC 관련 설정 (인터셉터, 리소스 핸들러 등))
  • SecurityConfig (보안 관련 설정 (인증, 인가, 보안 필터 등))
  • DatabaseConfig (데이터베이스 연결, JPA 설정 등)
  • RedisConfig (Redis 캐시 서버 설정)
  • SwaggerConfig (API 문서 자동화 설정)
 - Security(애플리케이션의 보안 관련 기능 담당) > @EnableWebSecurity, @EnableGlobalMethodSecurity, @Secured, @PreAuthorize...
  • SecurityConfig (Spring Security 기본 설정)
  • JwtTokenProvider (JWT 토큰 생성, 검증, 관리)
  ...
 - Logging(애플리케이션의 로그 처리 담당) > @Aspect, @Pointcut, @Around, @Before, @After...
  • LoggingAspect (AOP를 사용한 메서드 실행 로깅)
  • CustomLogAppender (로그 저장 방식 커스터마이징)
  • LogInterceptor (HTTP 요청/응답 로깅)
  ...
 - Transaction(데이터베이스 트랜잭션 처리 담당) > @EnableTransactionManagement, @Transactional...
  • TransactionConfig (트랜잭션 매니저 설정)
  • CustomTransactionManager (트랜잭션 처리 방식 커스터마이징)
 - Utils/Helpers(애플리케이션 전반에서 사용되는 공통 유틸리티 기능 제공)  >  @Component, @Slf4j...
  • DateUtils (날짜 관련 유틸)
  • StringUtils (문자열 처리 유틸)
  • FileUtils (파일 처리 유틸)
  • EncryptionUtils (암호화 유틸)
  • ValidationUtils (데이터 검증 유틸)
  • Constants (상수)
  • Enums (열거형)
  • Response Wrapper (API 응답 형식 표준화)
  ...

3. 계층의 흐름도
[Client] → HTTP Request
    ↓
[Presentation Layer] (Controller)
    ↓
[Business Layer] (Service)
    ↓
[Data Access Layer] (Repository or DAO)
    ↓
[Database]
    ↓
[Persistence Layer] (Repository or DAO)
    ↓
[Business Layer] (Service)
    ↓
[Presentation Layer] (Controller)
    ↓
[Client] ← HTTP Response

4. SpringStarterMavenProject 패키지 구조
백엔드는 도메인형 아키텍처를 사용하고 Spring Boot, MyBatis를 사용해서 구현했다.
프론트엔드는 계층형 아키텍처를 사용하고 html, css, javascript, axios를 사용해서 구현했다.(차후에 Next.js로 바꿔볼 예정)
빌드 툴은 maven을 사용했다.(차후에 gradle로 바꿔볼 예정)
형상관리는 github을 사용했다.
    
SpringStarterMavenProject/
├── .git/                           # Git 저장소
├── .gitignore                      # Git 제외 설정
├── README.md                       # 프로젝트 설명
├── pom.xml                         # Maven 설정
└── src/
    ├── main/
    │   ├── java/								 # 자바 소스 코드파일. 빌드 시 .class 파일이 됨
    │   │   └── com/example/demo/
    │   │       ├── ProjectApplication.java      # 메인 클래스. 스프링 부트 시작점
    │   │       ├── global/                      # 전역 컴포넌트
    │   │       │   ├── config/                  # 프로젝트 설정 관련 클래스들(보안, 데이터베이스 등 각종 설정)
    │   │       │   │   ├── WebConfig.java       # 웹 설정
    │   │       │   │   ├── SecurityConfig.java  # 보안 설정
    │   │       │   │   └── MybatisConfig.java   # MyBatis 설정
    │   │       │   ├── error/                   # 예외 처리
    │   │       │   │   ├── GlobalExceptionHandler.java
    │   │       │   │   ├── ErrorCode.java       # 에러 코드 enum
    │   │       │   │   └── ErrorResponse.java   # 에러 응답 객체
    │   │       │   └── common/                  # 공통
    │   │       │       ├── util/                # 유틸리티
    │   │       │       │   ├── DateUtils.java
    │   │       │       │   └── StringUtils.java
    │   │       │       └── model/               # 공통 모델
    │   │       │           ├── BaseEntity.java  # 기본 엔티티
    │   │       │           └── BaseResponse.java # 기본 응답
    │   │       └── domain/                      # 도메인
    │   │           ├── member/                  # 회원 도메인
    │   │           │   ├── controller/          # 컨트롤러 계층
    │   │           │   │   ├── MemberController.java    # 뷰 컨트롤러
    │   │           │   │   └── MemberApiController.java # API 컨트롤러
    │   │           │   ├── service/             # 실제 비즈니스 로직 구현
    │   │           │   │   ├── MemberService.java       # 인터페이스
    │   │           │   │   └── impl/
    │   │           │   │       └── MemberServiceImpl.java
    │   │           │   ├── mapper/              # 매퍼 계층
    │   │           │   │   └── MemberMapper.java
    │   │           │   ├── dto/                 # API 요청/응답에 사용되는 데이터 객체
    │   │           │   │   ├── request/         # 요청 DTO
    │   │           │   │   │   └── MemberRequestDto.java
    │   │           │   │   └── response/        # 응답 DTO
    │   │           │   │       └── MemberResponseDto.java
    │   │           │   └── model/               # 데이터베이스 테이블과 매핑되는 클래스
    │   │           │       └── Member.java
    │   │           └── board/                   # 게시판 도메인
    │   └── resources/							# 자바 소스 외의 모든 리소스 파일(XML, properties, yml, html, css, js 등). 빌드 시 그대로 클래스패스에 복사됨
    │       ├── static/                          # 정적 리소스
    │       │   ├── assets/                      # 미디어 파일
    │       │   │   ├── images/                  # 이미지
    │       │   │   │   ├── common/             # 공통 이미지
    │       │   │   │   └── pages/              # 도메인별 이미지
    │       │   │   └── fonts/                   # 폰트
    │       │   ├── css/                         # 스타일시트
    │       │   │   ├── common/                  # 공통 CSS
    │       │   │   │   ├── reset.css           # 초기화
    │       │   │   │   └── layout.css          # 레이아웃
    │       │   │   └── pages/                   # 도메인별 CSS
    │       │   │       ├── member/
    │       │   │       │   └── member.css
    │       │   │       └── board/
    │       │   │           └── board.css
    │       │   └── js/                          # 자바스크립트
    │       │       ├── lib/                     # 외부 JS 라이브러리
    │       │       │   ├── jquery.min.js
    │       │       │   └── jquery-ui.min.js
    │       │       ├── common/                  # 공통 JS
    │       │       │   ├── common.js           # 공통 유틸
    │       │       │   └── api.js              # API 통신
    │       │       └── pages/                   # 도메인별 JS
    │       │           ├── member/
    │       │           │   └── member.js
    │       │           └── board/
    │       │               └── board.js
    │       ├── templates/                       # URL로 접근 불가능해야하는 리소스 파일들. HTML파일을 직접 접근하는것은 인증/인가를 건너뛰는 등 보안 취약점이 있으므로 반드시 서버사이드를 통한 후 접근하도록 해야한다.
    │       │   ├── layout/                      # 재사용 가능한 페이지 구조. 공통 레이아웃 (헤더, 푸터)
    │       │   │   ├── base.html              	 # 기본 템플릿
    │       │   │   ├── header.html
    │       │   │   └── footer.html
    │       │   └── pages/                      # 도메인별 HTML. 실제 서비스 페이지들
    │       │       ├── member/
    │       │       │   └── member.html
    │       │       └── board/
    │       │           └── board.html
    │       └── mybatis/                        # MyBatis
    │       │   ├── config/
    │       │   │   └── mybatis-config.xml      # MyBatis 설정
    │       │   └── mapper/                     # SQL 매퍼
    │       │       ├── member/
    │       │       │   └── MemberMapper.xml
    │       │       └── board/
    │       │           └── BoardMapper.xml
    │       └── application.yml				: 스프링 부트 설정 (DB, 서버 등)   
    └── test/                                   # 테스트
        └── java/
            └── com/example/project/
                └── domain/
                    ├── member/
                    │   ├── controller/
                    │   ├── service/
                    │   └── mapper/
                    └── board/


또한 Spring Boot 프로젝트에서는 전통적인 /WEB-INF 구조를 사용하지 않는다.
기존 웹 프로젝트 구조:
 src/main/webapp/WEB-INF
     ├── classes/
     ├── lib/
     ├── views/
     └── web.xml(설정 파일)

Spring Boot 구조:
 src/main/resources/
     ├── static/      (정적 리소스)
     └── templates/   (동적 뷰 템플릿)
     └── application.yml/properties(설정 파일)

5. RESTful API 규칙
5.1 기본 URL 구조
## 형식 : 도메인/api/API 버전/리소스
# 예시 : https://api.example.com/api/v1/resources

## API 버전은 다음과 같이 관리할 수 있다.
# URL(권장)
/v1/
/v2/

# HTTP 헤더
Accept: application/vnd.example.v1+json
Accept: application/vnd.example.v2+json

## 리소스는 다음과 같은 기준으로 유연하게 나눈다.
# 비즈니스 도메인(업무 영역) 기준
# 예시
/products          : 상품 관련
/orders            : 주문 관련
/users             : 회원 관련
/carts             : 장바구니 관련

# 기능/서비스 기준
# 예시
/auth              : 인증 관련
/profiles          : 프로필 관련
/preferences       : 설정 관련

# 데이터 관계 기준
# 예시
/posts/{id}/comments   : 게시글의 댓글
/posts/{id}/likes      : 게시글의 좋아요

5.2 리소스 명명 규칙
# 기본 규칙
/members                 							# 일관된 복수형 사용
/members/{id}            							# 식별자로 개별 리소스 접근
/members/{id}/orders     							# 관계 표현 시에는 계층 구조(ex:특정 사용자의 주문목록)
/members/{memberId}/orders/{orderId}/items 			# 중첩된 리소스 표현

# 정렬, 필터링, 검색 조건, 페이징 처리 처럼 선택적인 값 사용 시 Query Parameter 사용
/members?sort=id,desc&sort=name,asc																 # 정렬
/members?role=admin																				 # 필터링
/members/search?searchType=name&keyword=Yoo														 # 검색 조건
/members?page=1&size=10																			 # 페이징 처리
/members/search?searchType=name&keyword=Yoo&sort=id,desc&sort=name,asc&role=admin&page=1&size=10 # 복합 조건(정렬 + 필터링 + 검색 + 페이징)

# 나쁜 예
/member/{id}             			# 단수형 사용
/members/{id}/getOrders   			# URL에 동사 사용
/members/search/searchType/name		# Query Parameter를 경로에 포함

5.3 HTTP 메서드 사용
GET     /members         # 사용자 목록 조회
GET     /members/{id}    # 특정 사용자 조회
POST    /members         # 새로운 사용자 생성
PUT     /members/{id}    # 사용자 정보 전체 수정
PATCH   /members/{id}    # 사용자 정보 일부 수정
DELETE  /members/{id}    # 사용자 삭제

5.4 HTTP Header 사용
# 요청 헤더(필수)
Content-Type: application/json      # POST, PUT, PATCH 요청 시에만 필수
Accept: application/json            # 클라이언트가 받고자 하는 응답 형식 명시

# 응답 헤더(필수)
Content-Type: application/json; charset=UTF-8    # 응답 본문의 타입과 인코딩

# 요청 헤더(선택)
Authorization: Bearer {token}       # 클라이언트가 서버에 인증 정보 전달
Cache-Control: no-cache             # 클라이언트가 캐시 정책 요청
If-None-Match: "{etag}"             # 클라이언트가 가진 리소스의 ETag 전달
Origin: https://example.com         # 요청하는 출처 도메인 정보
X-Request-ID: abc-123               # 클라이언트가 생성한 요청 추적 ID
Accept-Language: ko-KR              # 선호 언어

# 응답 헤더(선택)
Cache-Control: no-cache           # 서버가 클라이언트에게 캐시 정책 전달
ETag: "{hash}"                    # 서버가 리소스의 버전 정보 전달
Access-Control-Allow-Origin: *    # 허용된 도메인 목록
Access-Control-Allow-Methods: GET, POST, PUT, DELETE  # 허용된 HTTP 메소드
X-Request-ID: abc-123            # 요청의 ID를 그대로 응답에 포함
X-API-Version: v1                # 서버의 API 버전 정보

5.5 응답 코드
# 성공 응답
200 OK           // 요청 성공
201 Created      // 리소스 생성 성공
204 No Content   // 성공했지만 응답 본문 없음 (DELETE 등)

# 클라이언트 오류
400 Bad Request  // 잘못된 요청
401 Unauthorized // 인증 필요
403 Forbidden    // 권한 없음
404 Not Found    // 리소스 없음
409 Conflict     // 리소스 충돌

# 서버 오류
500 Internal Server Error
503 Service Unavailable

5.6 응답 데이터 포맷
// 성공 응답
{
    "code": "201",
    "status": "SUCCESS",
    "message": "회원이 성공적으로 등록되었습니다",
    "data": {
        "id": 1,
        "name": "홍길동",
        "email": "hong@test.com",
        "createdAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

// 에러 응답
{
    "code": "400",
    "status": "ERROR",
    "message": "입력값 검증에 실패했습니다",
    "errors": [
        {
            "field": "email",
            "value": "invalid-email",
            "reason": "INVALID_FORMAT",
            "message": "이메일 형식이 올바르지 않습니다"
        }
    ],
    "timestamp": "2024-01-23T12:34:56"
}

5.7 API 문서화
Swagger나 Spring Rest Docs 등을 사용해서 API 스펙 문서 필수 작성

[[URL 예시]]
## 등록
# 등록
POST /api/v1/members
Content-Type: application/json
Accept: application/json

Request:
{
    "name": "홍길동",
    "email": "hong@test.com"
}

Response: (201 Created)
{
    "code": "201",
    "status": "SUCCESS",
    "message": "회원이 성공적으로 등록되었습니다",
    "data": {
        "id": 1,
        "name": "홍길동",
        "email": "hong@test.com",
        "createdAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

## 조회
# 조회 (목록)
GET /api/v1/members
Accept: application/json

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "정상 처리되었습니다",
    "data": {
        "content": [
            {
                "id": 1,
                "name": "홍길동",
                "email": "hong@test.com"
            },
            {
                "id": 2,
                "name": "김철수",
                "email": "kim@test.com"
            }
        ],
        "pagination": {
            "page": 0,
            "size": 10,
            "totalElements": 42,
            "totalPages": 5
        }
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 조회 (단건)
GET /api/v1/members/{id}
Accept: application/json

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "정상 처리되었습니다",
    "data": {
        "id": 1,
        "name": "홍길동",
        "email": "hong@test.com",
        "createdAt": "2024-01-23T12:34:56",
        "updatedAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 검색
GET /api/v1/members/search?keyword=홍길동
Accept: application/json

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "정상 처리되었습니다",
    "data": {
        "content": [
            {
                "id": 1,
                "name": "홍길동",
                "email": "hong@test.com"
            }
        ],
        "pagination": {
            "page": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
        }
    },
    "timestamp": "2024-01-23T12:34:56"
}

## 수정
# 수정 (전체)
PUT /api/v1/members/{id}
Content-Type: application/json
Accept: application/json

Request:
{
    "name": "홍길동2",
    "email": "hong2@test.com"
}

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "회원 정보가 수정되었습니다",
    "data": {
        "id": 1,
        "name": "홍길동2",
        "email": "hong2@test.com",
        "updatedAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 수정 (부분)
PATCH /api/v1/members/{id}
Content-Type: application/json
Accept: application/json

Request:
{
    "name": "홍길동3"
}

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "회원 정보가 수정되었습니다",
    "data": {
        "id": 1,
        "name": "홍길동3",
        "email": "hong2@test.com",
        "updatedAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

## 삭제
# 삭제
DELETE /api/v1/members/{id}
Accept: application/json

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "회원이 삭제되었습니다",
    "data": {
        "id": 1,
        "deletedAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

## 중첩된 리소스(예시:특정 사용자의 주문 목록)
# 주문 등록
POST /api/v1/members/{userId}/orders
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
    "items": [
        {
            "productId": 1,
            "quantity": 2
        }
    ],
    "deliveryAddress": {
        "zipCode": "12345",
        "address": "서울시 강남구",
        "detailAddress": "삼성동 123-45"
    },
    "paymentMethod": "CARD"
}

Response: (201 Created)
{
    "code": "201",
    "status": "SUCCESS",
    "message": "주문이 성공적으로 생성되었습니다",
    "data": {
        "orderId": 1,
        "orderNumber": "ORD-20240125-001",
        "totalAmount": 50000,
        "status": "PENDING",
        "createdAt": "2024-01-25T10:00:00"
    },
    "timestamp": "2024-01-25T10:00:00"
}

# 특정주문 조회
GET /api/v1/members/{userId}/orders/{orderId}
Accept: application/json
Authorization: Bearer {token}

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "정상 처리되었습니다",
    "data": {
        "orderId": 1,
        "orderNumber": "ORD-20240125-001",
        "totalAmount": 50000,
        "status": "PENDING",
        "items": [
            {
                "productId": 1,
                "productName": "상품A",
                "quantity": 2,
                "price": 20000
            }
        ],
        "deliveryAddress": {
            "zipCode": "12345",
            "address": "서울시 강남구",
            "detailAddress": "삼성동 123-45"
        },
        "createdAt": "2024-01-25T10:00:00"
    },
    "timestamp": "2024-01-25T10:00:00"
}

# 주문 수정
PATCH /api/v1/members/{userId}/orders/{orderId}
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
    "deliveryAddress": {
        "zipCode": "12345",
        "address": "서울시 강남구",
        "detailAddress": "삼성동 999-99"
    }
}

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "주문이 수정되었습니다",
    "data": {
        "orderId": 1,
        "orderNumber": "ORD-20240125-001",
        "deliveryAddress": {
            "zipCode": "12345",
            "address": "서울시 강남구",
            "detailAddress": "삼성동 999-99"
        },
        "updatedAt": "2024-01-25T11:00:00"
    },
    "timestamp": "2024-01-25T11:00:00"
}

# 주문 삭제
DELETE /api/v1/members/{userId}/orders/{orderId}
Authorization: Bearer {token}

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "주문이 취소되었습니다",
    "data": {
        "orderId": 1,
        "orderNumber": "ORD-20240125-001",
        "status": "CANCELLED",
        "cancelledAt": "2024-01-25T12:00:00"
    },
    "timestamp": "2024-01-25T12:00:00"
}

## 기타
# 활성화/비활성화
PUT /api/v1/members/{id}/activate
Content-Type: application/json
Accept: application/json

Request:
{
    "reason": "회원 복귀"
}

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "회원 상태가 변경되었습니다",
    "data": {
        "id": 1,
        "status": "ACTIVE",
        "updatedAt": "2024-01-23T12:34:56",
        "reason": "회원 복귀"
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 통계
GET /api/v1/members/stats
Accept: application/json

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "정상 처리되었습니다",
    "data": {
        "metrics": {
            "totalMembers": 1000,
            "activeMembers": 850,
            "newMembersToday": 10,
            "averageAge": 32.5
        },
        "trends": {
            "growthRate": "5.2%",
            "retentionRate": "85%"
        }
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 특정 기간 통계
GET /api/v1/members/stats?startDate=20240101&endDate=20241231
Accept: application/json

Response: (200 OK)
{
    "code": "200",
    "status": "SUCCESS",
    "message": "정상 처리되었습니다",
    "data": {
        "period": {
            "start": "2024-01-01",
            "end": "2024-12-31"
        },
        "metrics": {
            "totalMembers": 1000,
            "activeMembers": 850,
            "newMembers": 150,
            "averageAge": 32.5
        }
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 다중 생성
POST /api/v1/members/bulk
Content-Type: application/json
Accept: application/json

Request:
{
    "members": [
        {
            "name": "홍길동",
            "email": "hong@test.com"
        },
        {
            "name": "김철수",
            "email": "kim@test.com"
        }
    ]
}

Response: (201 Created)
{
    "code": "201",
    "status": "SUCCESS",
    "message": "회원이 일괄 등록되었습니다",
    "data": {
        "summary": {
            "total": 2,
            "successful": 2,
            "failed": 0,
            "processedAt": "2024-01-23T12:34:56"
        },
        "results": [
            {
                "id": 1,
                "name": "홍길동",
                "email": "hong@test.com"
            },
            {
                "id": 2,
                "name": "김철수",
                "email": "kim@test.com"
            }
        ]
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 이메일 전송
POST /api/v1/members/{memberId}/emails
Content-Type: application/json
Accept: application/json

Request:
{
    "subject": "메일 제목",
    "content": "메일 내용",
    "recipients": ["email@example.com"],
    "options": {
        "priority": "normal",
        "attachments": []
    }
}

Response: (202 Accepted)
{
    "code": "202",
    "status": "SUCCESS",
    "message": "이메일 전송이 요청되었습니다",
    "data": {
        "id": "email-123456",
        "status": "QUEUED",
        "queuedAt": "2024-01-23T12:34:56",
        "estimatedDeliveryTime": "2024-01-23T12:35:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 파일 업로드
POST /api/v1/members/{id}/image
Content-Type: multipart/form-data
Accept: application/json

Request:
Form-Data:
- file: (binary)
- type: "PROFILE"
- metadata: {
    "description": "프로필 이미지",
    "tags": ["profile", "avatar"]
  }

Response: (201 Created)
{
    "code": "201",
    "status": "SUCCESS",
    "message": "파일이 업로드되었습니다",
    "data": {
        "id": "file-123456",
        "url": "https://example.com/images/profile/1.jpg",
        "fileName": "profile.jpg",
        "fileSize": 1024000,
        "mimeType": "image/jpeg",
        "description": "프로필 이미지",
        "dimensions": {
            "width": 800,
            "height": 600
        },
        "uploadedAt": "2024-01-23T12:34:56"
    },
    "timestamp": "2024-01-23T12:34:56"
}

# 유효성 검사 실패
Response: (400 Bad Request)
{
    "code": "400",
    "status": "ERROR",
    "message": "입력값 검증에 실패했습니다",
    "errors": [
        {
            "field": "email",
            "value": "invalid-email",
            "reason": "INVALID_FORMAT",
            "message": "이메일 형식이 올바르지 않습니다"
        }
    ],
    "timestamp": "2024-01-23T12:34:56"
}

# 권한 없음
Response: (403 Forbidden)
{
    "code": "403",
    "status": "ERROR",
    "message": "해당 리소스에 대한 접근 권한이 없습니다",
    "errors": [
        {
            "reason": "ACCESS_DENIED",
            "message": "관리자 권한이 필요합니다",
            "data": {
                "requiredRole": "ADMIN",
                "currentRole": "USER"
            }
        }
    ],
    "timestamp": "2024-01-23T12:34:56"
}

# 리소스를 찾을 수 없음
Response: (404 Not Found)
{
    "code": "404",
    "status": "ERROR",
    "message": "요청한 리소스를 찾을 수 없습니다",
    "errors": [
        {
            "reason": "RESOURCE_NOT_FOUND",
            "message": "해당 ID의 회원이 존재하지 않습니다",
            "data": {
                "resourceId": "999",
                "resourceType": "member"
            }
        }
    ],
    "timestamp": "2024-01-23T12:34:56"
}

# 서버 에러
Response: (500 Internal Server Error)
{
    "code": "500",
    "status": "ERROR",
    "message": "서버 처리 중 오류가 발생했습니다",
    "errors": [
        {
            "reason": "INTERNAL_SERVER_ERROR",
            "message": "데이터베이스 연결 오류"
        }
    ],
    "timestamp": "2024-01-23T12:34:56"
}

* 주의 : RESTful API 규칙은 데이터 호출 API에만 적용함. 화면(View) 호출 API에는 다음과 같이 URL 규칙만 적용
GET  /members          # 사용자 목록 화면
GET  /members/new      # 사용자 등록 화면
GET  /members/{id}     # 사용자 상세 화면
GET  /members/{id}/edit # 사용자 수정 화면