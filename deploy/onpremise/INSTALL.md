# Spring Starter Maven — 설치 가이드

> **버전**: 릴리즈 패키지 버전 참고  
> **지원 OS**: Linux (Ubuntu 20.04+, CentOS 7+), Windows Server 2016+, Windows 10/11

---

## 목차

1. [사전 요구사항](#1-사전-요구사항)
2. [JAR 패키지 설치 (onpremise-zip)](#2-jar-패키지-설치)
3. [Docker 패키지 설치 (onpremise-docker)](#3-docker-패키지-설치)
4. [데이터베이스 초기화](#4-데이터베이스-초기화)
5. [설정 파일 편집](#5-설정-파일-편집)
6. [시작 / 종료](#6-시작--종료)
7. [로그 확인](#7-로그-확인)
8. [트러블슈팅](#8-트러블슈팅)

---

## 1. 사전 요구사항

### JAR 패키지
| 항목 | 최소 요건 |
|---|---|
| Java | JDK 17 이상 (JRE 불가 — JDK 필요) |
| RAM | 512 MB 이상 권장 (JVM 힙 기본 1 GB 설정) |
| DB | MariaDB 10.6 이상 또는 MySQL 8.0 이상 |
| 포트 | 8080 (기본값, config/application.yml에서 변경 가능) |

#### Java 설치 확인
```bash
java -version
# 출력 예: openjdk version "17.0.12" ...
```

다운로드: [Eclipse Temurin JDK 17](https://adoptium.net/)

### Docker 패키지
| 항목 | 최소 요건 |
|---|---|
| Docker | Docker Engine 20.10 이상 |
| docker-compose | v2 이상 (`docker compose` 또는 `docker-compose`) |
| RAM | 512 MB 이상 |
| DB | MariaDB (docker-compose.yml의 db 서비스 사용 가능) |

---

## 2. JAR 패키지 설치

### 파일 구조
```
spring-starter-maven-{버전}/
├── app.jar                     ← 실행 파일 (수정 금지)
├── config/
│   └── application.yml         ← ★ DB 접속 정보 편집 필요
├── mapper/
│   └── *.xml                   ← SQL 매퍼 (필요 시 수정 가능)
├── bin/
│   ├── start.sh / start.bat    ← 시작 스크립트
│   └── stop.sh  / stop.bat     ← 종료 스크립트
└── INSTALL.md                  ← 이 파일
```

### Linux 설치

```bash
# 1. 압축 해제
unzip spring-starter-maven-{버전}-release.zip -d /opt/
cd /opt/spring-starter-maven-{버전}

# 2. 스크립트 실행 권한 부여
chmod +x bin/start.sh bin/stop.sh

# 3. 설정 파일 편집 (DB 접속 정보 입력)
vi config/application.yml
# 또는
nano config/application.yml

# 4. 설정 파일 권한 보호 (비밀번호 포함 파일)
chmod 600 config/application.yml

# 5. 앱 시작
bin/start.sh

# 6. 30~60초 후 헬스체크
curl http://localhost:8080/actuator/health
```

### Windows 설치

```cmd
:: 1. 압축 해제 (탐색기 또는 명령어)
:: 원하는 폴더에 ZIP을 해제하세요. 예: C:\app\spring-starter-maven-1.0.0\

:: 2. 설정 파일 편집
:: config\application.yml 을 메모장이나 VS Code로 열어 DB 정보 입력

:: 3. 앱 시작 (더블클릭 또는 CMD)
bin\start.bat
```

---

## 3. Docker 패키지 설치

### 파일 구조
```
spring-starter-maven-{버전}-docker-release.zip
├── image.tar.gz          ← Docker 이미지 (인터넷 없이 설치 가능)
├── docker-compose.yml    ← 실행 설정
├── INSTALL.md            ← 이 파일
└── load-and-run.sh       ← 원클릭 설치 스크립트 (Linux)
```

### Linux 설치

```bash
# 1. 압축 해제
unzip spring-starter-maven-{버전}-docker-release.zip
cd spring-starter-maven-{버전}-docker

# 2. docker-compose.yml 편집 (DB 접속 정보 입력)
vi docker-compose.yml

# 3. 원클릭 설치 (이미지 로드 + 컨테이너 시작)
chmod +x load-and-run.sh
./load-and-run.sh

# 또는 수동으로:
docker load < image.tar.gz
docker-compose up -d
```

### Windows 설치

```powershell
# 1. 압축 해제 (PowerShell 또는 탐색기)
Expand-Archive spring-starter-maven-*-docker-release.zip -DestinationPath .

# 2. docker-compose.yml 편집 (DB 접속 정보 입력)
notepad docker-compose.yml

# 3. 이미지 로드
docker load -i image.tar.gz

# 4. 컨테이너 시작
docker-compose up -d

# 5. 상태 확인
docker-compose ps
```

---

## 4. 데이터베이스 초기화

앱 실행 전 MariaDB에 DB 스키마와 계정을 생성해야 합니다.

```sql
-- MariaDB에 root로 접속 후 실행

-- 1. 데이터베이스(스키마) 생성
CREATE DATABASE IF NOT EXISTS SJSJSS
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. 전용 계정 생성 (root 사용 금지)
CREATE USER IF NOT EXISTS 'your_db_user'@'%' IDENTIFIED BY 'your_db_password';

-- 3. 권한 부여 (최소 권한 원칙: 필요한 권한만)
GRANT SELECT, INSERT, UPDATE, DELETE ON SJSJSS.* TO 'your_db_user'@'%';
FLUSH PRIVILEGES;
```

> **⚠ 주의**: `your_db_user`, `your_db_password`를 실제 값으로 변경하고  
> `config/application.yml` (또는 `docker-compose.yml`)의 DB 설정과 일치시키세요.

---

## 5. 설정 파일 편집

`config/application.yml`을 열어 아래 항목을 반드시 수정하세요.

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/SJSJSS   # ← DB 서버 IP/포트/이름 수정
    username: your_db_user                       # ← DB 계정명 수정
    password: your_db_password                   # ← DB 비밀번호 수정

server:
  port: 8080    # ← 사용할 포트 수정 (필요 시)
```

설정 변경 후에는 **반드시 앱을 재시작**해야 반영됩니다.

```bash
bin/stop.sh && bin/start.sh     # Linux
bin\stop.bat 실행 후 bin\start.bat 실행   # Windows
```

---

## 6. 시작 / 종료

### JAR 패키지

| 명령 | Linux | Windows |
|---|---|---|
| 시작 | `bin/start.sh` | `bin\start.bat` |
| 종료 | `bin/stop.sh` | `bin\stop.bat` |
| 강제 종료 | `bin/stop.sh --force` | `bin\stop.bat` |

### Docker 패키지

```bash
# 시작
docker-compose up -d

# 종료
docker-compose down

# 재시작
docker-compose restart

# 상태 확인
docker-compose ps
```

---

## 7. 로그 확인

### JAR 패키지
```bash
# 실시간 로그 확인
tail -f logs/application.log           # Linux
Get-Content logs\application.log -Wait  # Windows PowerShell
```

### Docker 패키지
```bash
docker-compose logs -f          # 실시간 로그
docker-compose logs --tail=100  # 최근 100줄
```

---

## 8. 트러블슈팅

### 앱이 시작되지 않는 경우

1. **포트 충돌**: `config/application.yml`에서 `server.port`를 다른 값으로 변경
   ```bash
   # Linux — 포트 사용 중 확인
   sudo lsof -i :8080
   # Windows
   netstat -ano | findstr :8080
   ```

2. **DB 연결 실패**: 로그에서 `Connection refused` 또는 `Access denied` 확인
   - `config/application.yml`의 `url`, `username`, `password` 확인
   - DB 서버 방화벽 설정 확인 (3306 포트 허용 여부)

3. **Java 버전 오류**: `UnsupportedClassVersionError` → JDK 17 이상 설치 필요

4. **메모리 부족**: `OutOfMemoryError` → `start.sh`(또는 `start.bat`)의 `-Xmx` 값 조정

### 서비스 자동 시작 설정 (Linux systemd)

```bash
# /etc/systemd/system/spring-app.service 파일 생성
sudo tee /etc/systemd/system/spring-app.service <<EOF
[Unit]
Description=Spring Starter Maven Application
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/spring-starter-maven
ExecStart=/opt/spring-starter-maven/bin/start.sh
ExecStop=/opt/spring-starter-maven/bin/stop.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable spring-app
sudo systemctl start spring-app
sudo systemctl status spring-app
```

---

문의: 개발팀 담당자에게 연락하세요.
