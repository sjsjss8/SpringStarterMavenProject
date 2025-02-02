# 현대적인 개발 아키텍처

## Frontend
- SPA + REST API
  - React/Vue.js/Angular
  - API 통신
  - 클라이언트 사이드 렌더링
  
* JSP 레거시 취급 이유:
  - JSP가 처음 요청되거나 변경되면 서블릿으로 변환되고 컴파일 된 후 실행되는 과정을 거쳐야 하기 때문에 레이턴시가 발생하는 문제
  - JSP를 사용하면 스파게티 코드가 될 수 있는 상황에 놓여진다
  - 스프링 부트의 내장 톰캣을 사용하는 실행가능한 JAR파일 빌드할 경우 JSP를 인식할 수 없다
  
## Backend
- Spring Boot
  - REST API 제공
  - JSON 응답
  - 비즈니스 로직 처리