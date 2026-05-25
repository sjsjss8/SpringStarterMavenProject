pipeline {
    agent any

    // ── 배포 방법 선택 파라미터 ───────────────────────────────────────────────
    parameters {
        choice(
            name: 'DEPLOY_METHOD',
            choices: [
                'package-zip',
                'package-docker',
                'server-jar',
                'server-jar-zip',
                'server-blue-green',
                'server-docker',
                'server-k8s'
            ],
            description: '''── 배포 방법을 선택하세요 ──────────────────────────────────────────

  [ 고객사 납품용 패키지 생성 ]

  package-zip  ★ B2B 고객사 설치형 (JAR 패키지)
    JAR + 설정 파일 템플릿 + SQL 매퍼 + 시작/종료 스크립트를 ZIP으로 패키징
    결과 → {앱명}-{버전}-release.zip (Jenkins Artifacts에서 다운로드)
    사용 : 고객사 서버에 JDK 17만 있으면 ZIP 해제 후 config 수정 → 실행
    사전 조건 : 없음 (빌드만 되면 즉시 사용 가능)

  package-docker  ★ B2B 고객사 설치형 (Docker 패키지 / 폐쇄망)
    Docker 이미지를 tar.gz로 저장하고 docker-compose와 함께 ZIP으로 패키징
    결과 → {앱명}-{버전}-docker-release.zip (Jenkins Artifacts에서 다운로드)
    사용 : 인터넷 없는 폐쇄망 고객사 서버에서도 Docker만 있으면 즉시 설치 가능
    사전 조건 : Docker Engine 실행 중

  [ 원격 서버 배포 — 전통 방식 → 현대화 순 ]

  server-jar                          ① 전통 방식
    빌드된 JAR 파일을 SSH로 원격 서버에 전송 후 앱 재시작
    결과 → 원격 서버에서 앱 구동 (서버 IP:8080 접속)
    특징 : 가장 단순한 배포. 설정 파일은 서버에서 별도 관리
    사전 조건 : deploy-server-ssh 등록 + REMOTE_HOST/USER/PATH 설정

  server-jar-zip  ★ B2B 고객사 서버 자동 배포  ② 전통 방식 + 패키지 자동화
    온프레미스 ZIP 패키지(JAR + 설정 + 스크립트)를 SSH로 전송 후 자동 설치/업데이트
    결과 → 원격 서버에서 app.jar + bin/ + mapper/ 업데이트, 앱 재시작
    특징 : config/application.yml 은 최초 1회만 복사 (이후 업데이트 시 기존 설정 보존)
    사전 조건 : deploy-server-ssh 등록 + REMOTE_HOST/USER/PATH 설정

  server-blue-green  ★ 무중단 배포 (Zero-downtime)  ③ 전통 방식 + 무중단
    Blue/Green 두 인스턴스를 번갈아 배포하고 Nginx가 트래픽을 순간 전환
    결과 → 서비스 중단 없이 신규 버전으로 교체 (헬스체크 실패 시 자동 롤백)
    특징 : Blue(8081) ↔ Green(8082) 포트 전환 방식. 현업 무중단 배포 표준
    사전 조건 : deploy-server-ssh 등록 + Nginx 설치 + sudo 권한 설정

  server-docker  ★ 현업 표준 (중소규모/SaaS)  ④ 컨테이너화
    Docker 이미지를 빌드해 Docker Hub에 올린 뒤 원격 서버에서 docker-compose로 실행
    결과 → 원격 서버에서 컨테이너 구동 (서버 IP:8080 접속)
    사전 조건 : dockerhub-credentials + deploy-server-ssh 등록

  server-k8s  ★ 현업 표준 (대규모/클라우드/SaaS)  ⑤ 컨테이너 오케스트레이션
    Docker 이미지를 빌드해 레지스트리에 올린 뒤 Kubernetes 클러스터에 자동 배포
    결과 → K8s 클러스터에서 롤링 업데이트 구동 (LoadBalancer IP:80 접속)
    사전 조건 : dockerhub-credentials + kubeconfig Secret file 등록

────────────────────────────────────────────────────────────────'''
        )
    }

    // ── 공통 환경변수 ─────────────────────────────────────────────────────────
    environment {
        APP_NAME      = 'spring-starter-maven'
        DOCKER_IMAGE  = "your-dockerhub-id/${APP_NAME}"   // ← DockerHub ID로 변경
        DOCKER_TAG    = "${env.BUILD_NUMBER}"

        // [server-jar / server-jar-zip / server-blue-green / server-docker] 원격 서버 정보
        REMOTE_HOST   = '원격서버IP'                       // ← 원격 서버 IP로 변경
        REMOTE_USER   = 'ubuntu'                           // ← 원격 서버 계정으로 변경
        REMOTE_PATH   = '/home/ubuntu/app'

        // [server-docker / server-k8s] Docker Hub Credentials ID
        DOCKERHUB_CREDENTIALS = 'dockerhub-credentials'

        // [server-k8s] kubeconfig Credentials ID & 네임스페이스
        KUBECONFIG_CREDENTIALS = 'kubeconfig'
        K8S_NAMESPACE          = 'default'

        // [server-blue-green] Blue / Green 포트
        BLUE_PORT  = '8081'
        GREEN_PORT = '8082'
    }

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    stages {

        // ① 소스 체크아웃
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.GIT_BRANCH} | Commit: ${env.GIT_COMMIT[0..7]}"
            }
        }

        // ② 빌드 & 테스트
        stage('Build & Test') {
            steps {
                sh 'mvn clean package -q'
            }
            post {
                always {
                    junit allowEmptyResults: false,
                          testResults: '**/target/surefire-reports/*.xml'
                }
                success {
                    archiveArtifacts artifacts: 'target/app.jar', fingerprint: true
                }
                failure {
                    echo "❌ 빌드 실패 - 테스트 또는 컴파일 오류 확인 필요"
                }
            }
        }

        // ③ 코드 품질 검사
        stage('Code Quality') {
            steps {
                sh 'mvn checkstyle:check -q || true'
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: '**/target/checkstyle-result.xml',
                        allowEmptyArchive: true,
                        fingerprint: false
                    )
                }
            }
        }

        // ④ 보안 취약점 스캔
        stage('Security Scan') {
            parallel {

                // ── 보안 1: 라이브러리 CVE 취약점 검사 (OWASP Dependency Check)
                stage('OWASP Dependency Check') {
                    steps {
                        sh '''
                            mvn org.owasp:dependency-check-maven:check \
                              -DfailBuildOnCVSS=7 \
                              -DsuppressionFile=config/owasp-suppressions.xml \
                              || true
                        '''
                    }
                    post {
                        always {
                            archiveArtifacts(
                                artifacts: 'target/dependency-check-report.html',
                                allowEmptyArchive: true,
                                fingerprint: false
                            )
                        }
                    }
                }

                // ── 보안 2: 소스코드 보안 정적 분석 (SpotBugs + FindSecBugs)
                stage('SpotBugs Security') {
                    steps {
                        sh 'mvn spotbugs:check -q || true'
                    }
                    post {
                        always {
                            archiveArtifacts(
                                artifacts: '**/target/spotbugsXml.xml',
                                allowEmptyArchive: true,
                                fingerprint: false
                            )
                        }
                    }
                }
            }
        }

        // ⑤ Docker 이미지 빌드 (Docker 기반 배포 방식 공통)
        stage('Docker Build') {
            when {
                expression {
                    params.DEPLOY_METHOD in ['server-docker', 'server-k8s', 'package-docker']
                }
            }
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag  ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                echo "✅ Docker 이미지 빌드 완료 → ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

        // ⑥ Docker 이미지 보안 스캔 (Docker 기반 배포 방식 공통)
        stage('Image Security Scan') {
            when {
                expression {
                    params.DEPLOY_METHOD in ['server-docker', 'server-k8s', 'package-docker']
                }
            }
            steps {
                sh """
                    which trivy || apt-get install -y trivy 2>/dev/null || true
                    trivy image \
                      --severity HIGH,CRITICAL \
                      --exit-code 0 \
                      ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                """
            }
        }

        // ⑦ Docker Hub Push (server-docker / server-k8s 공통)
        stage('Docker Hub Push') {
            when {
                expression {
                    params.DEPLOY_METHOD in ['server-docker', 'server-k8s']
                }
            }
            steps {
                script {
                    try {
                        withCredentials([usernamePassword(
                            credentialsId: "${DOCKERHUB_CREDENTIALS}",
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASS'
                        )]) {
                            sh """
                                echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                                docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                                docker push ${DOCKER_IMAGE}:latest
                                docker logout
                                echo "✅ Docker Hub Push 완료 → ${DOCKER_IMAGE}:${DOCKER_TAG}"
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ Docker Hub Push 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : Username with password"
                        echo "    ID      : dockerhub-credentials"
                        echo "    Username: Docker Hub 아이디"
                        echo "    Password: Docker Hub Access Token"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("Docker Hub Credentials 'dockerhub-credentials' 미등록 또는 Push 실패")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [server-jar] 원격 서버에 JAR만 SSH 전송 후 앱 재시작
        //   사전 조건 : Jenkins Credentials에 'deploy-server-ssh' 등록
        //             Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 설정
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: server-jar') {
            when {
                expression { params.DEPLOY_METHOD == 'server-jar' }
            }
            steps {
                script {
                    try {
                        withCredentials([sshUserPrivateKey(
                            credentialsId: 'deploy-server-ssh',
                            keyFileVariable:  'SSH_KEY',
                            usernameVariable: 'SSH_USER'
                        )]) {
                            sh """
                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} \\
                                    "mkdir -p ${REMOTE_PATH}"

                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    target/app.jar \\
                                    ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/app-new.jar

                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} "
                                        # PID 파일로 정확히 이 앱만 종료 (pkill 미사용)
                                        if [ -f ${REMOTE_PATH}/app.pid ]; then
                                            APP_PID=\\\$(cat ${REMOTE_PATH}/app.pid)
                                            kill -TERM \\\$APP_PID 2>/dev/null || true
                                            sleep 5
                                            kill -9 \\\$APP_PID 2>/dev/null || true
                                            rm -f ${REMOTE_PATH}/app.pid
                                        fi

                                        mv ${REMOTE_PATH}/app-new.jar ${REMOTE_PATH}/app.jar

                                        nohup java -Xms256m -Xmx1g \\
                                            -jar ${REMOTE_PATH}/app.jar \\
                                            --spring.profiles.active=prod \\
                                            > ${REMOTE_PATH}/app.log 2>&1 &
                                        echo \\\$! > ${REMOTE_PATH}/app.pid
                                        echo '✅ 배포 완료 (PID: '\\\$(cat ${REMOTE_PATH}/app.pid)')'
                                    "
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ server-jar 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : SSH Username with private key"
                        echo "    ID      : deploy-server-ssh"
                        echo "  Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 값 설정"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("deploy-server-ssh 미등록 또는 서버 연결 실패")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [server-jar-zip] 온프레미스 ZIP 패키지를 원격 서버에 SSH 전송 후 설치/업데이트
        //   최초 설치 : config/application.yml 복사 후 수동 DB 설정 안내 (앱 미시작)
        //   재배포    : app.jar + bin/ + mapper/ 만 교체, config/ 기존 설정 보존
        //   사전 조건 : Jenkins Credentials에 'deploy-server-ssh' 등록
        //             Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 설정
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: server-jar-zip') {
            when {
                expression { params.DEPLOY_METHOD == 'server-jar-zip' }
            }
            steps {
                script {
                    try {
                        withCredentials([sshUserPrivateKey(
                            credentialsId: 'deploy-server-ssh',
                            keyFileVariable:  'SSH_KEY',
                            usernameVariable: 'SSH_USER'
                        )]) {
                            sh """
                                # ── 1. 온프레미스 ZIP 빌드 ──────────────────────────────
                                echo "── 온프레미스 패키지 빌드 중..."
                                mvn package -Ponpremise -q

                                ZIP_FILE=\$(ls target/*-release.zip | head -1)
                                ZIP_NAME=\$(basename "\$ZIP_FILE")
                                echo "   ZIP: \$ZIP_FILE (\$(du -sh "\$ZIP_FILE" | cut -f1))"

                                # ── 2. 원격 서버에 전송 ──────────────────────────────────
                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} \\
                                    "mkdir -p ${REMOTE_PATH}/releases"

                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    "\$ZIP_FILE" \\
                                    ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/releases/

                                # ── 3. 원격 서버에서 설치/업데이트 ─────────────────────
                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} "
                                        set -e
                                        APP_DIR='${REMOTE_PATH}/current'
                                        ZIP_PATH='${REMOTE_PATH}/releases/${ZIP_NAME}'

                                        # 기존 앱 종료
                                        if [ -f \\\"\\\$APP_DIR/app.pid\\\" ]; then
                                            APP_PID=\\\$(cat \\\"\\\$APP_DIR/app.pid\\\")
                                            kill -TERM \\\$APP_PID 2>/dev/null || true
                                            sleep 5
                                            kill -9 \\\$APP_PID 2>/dev/null || true
                                            rm -f \\\"\\\$APP_DIR/app.pid\\\"
                                        fi

                                        # ZIP 압축 해제 (임시 디렉터리)
                                        TEMP_DIR=\\\$(mktemp -d)
                                        unzip -q \\\"\\\$ZIP_PATH\\\" -d \\\"\\\$TEMP_DIR\\\"
                                        SRC_DIR=\\\"\\\$TEMP_DIR/\\\$(ls \\\"\\\$TEMP_DIR\\\")/\\\"

                                        mkdir -p \\\"\\\$APP_DIR\\\"

                                        # app.jar + bin/ + mapper/ 업데이트
                                        cp \\\"\\\${SRC_DIR}app.jar\\\" \\\"\\\$APP_DIR/app.jar\\\"
                                        cp -r \\\"\\\${SRC_DIR}bin\\\"  \\\"\\\$APP_DIR/\\\"
                                        chmod +x \\\"\\\$APP_DIR/bin/\\\"*.sh
                                        [ -d \\\"\\\${SRC_DIR}mapper\\\" ] && cp -r \\\"\\\${SRC_DIR}mapper\\\" \\\"\\\$APP_DIR/\\\"

                                        # config/는 최초 설치 시에만 복사
                                        if [ ! -f \\\"\\\$APP_DIR/config/application.yml\\\" ]; then
                                            cp -r \\\"\\\${SRC_DIR}config\\\" \\\"\\\$APP_DIR/\\\"
                                            rm -rf \\\"\\\$TEMP_DIR\\\"
                                            echo ''
                                            echo '========================================'
                                            echo '  ⚠ 최초 설치 완료 — DB 설정 필요'
                                            echo '  1. 설정 편집: nano \\\$APP_DIR/config/application.yml'
                                            echo '  2. 앱 시작  : \\\$APP_DIR/bin/start.sh'
                                            echo '========================================'
                                            exit 0
                                        fi

                                        rm -rf \\\"\\\$TEMP_DIR\\\"

                                        # 앱 시작
                                        \\\"\\\$APP_DIR/bin/start.sh\\\"
                                        echo '✅ 업데이트 완료'
                                    "

                                echo "✅ server-jar-zip 배포 완료 → ${REMOTE_HOST}:${REMOTE_PATH}/current"
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ server-jar-zip 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : SSH Username with private key"
                        echo "    ID      : deploy-server-ssh"
                        echo "  Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 값 설정"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("server-jar-zip 배포 실패: ${e.message}")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [server-blue-green] 무중단 블루-그린 배포
        //   Blue(8081) ↔ Green(8082) 두 인스턴스를 번갈아 배포
        //   Nginx upstream을 순간 전환해 서비스 중단 없이 버전 교체
        //   헬스체크 실패 시 신규 인스턴스를 자동 롤백 (구 버전 유지)
        //
        //   서버 사전 설정:
        //     1. Nginx 설치 + /etc/nginx/sites-enabled/spring-app 파일 생성:
        //          upstream spring_app { server 127.0.0.1:8081; }
        //          server { listen 80; location / { proxy_pass http://spring_app; } }
        //     2. sudo 권한 (NOPASSWD):
        //          echo "ubuntu ALL=(ALL) NOPASSWD: /usr/sbin/nginx" | sudo tee /etc/sudoers.d/nginx
        //     3. Java 17+ 설치
        //   사전 조건 : deploy-server-ssh 등록 + REMOTE_HOST/USER/PATH 설정
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: server-blue-green') {
            when {
                expression { params.DEPLOY_METHOD == 'server-blue-green' }
            }
            steps {
                script {
                    try {
                        withCredentials([sshUserPrivateKey(
                            credentialsId: 'deploy-server-ssh',
                            keyFileVariable:  'SSH_KEY',
                            usernameVariable: 'SSH_USER'
                        )]) {
                            sh """
                                # JAR 전송
                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} "mkdir -p ${REMOTE_PATH}"
                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    target/app.jar \\
                                    ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/app-new.jar

                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} "
                                        set -e
                                        APP_DIR='${REMOTE_PATH}'
                                        BLUE_PORT=${BLUE_PORT}
                                        GREEN_PORT=${GREEN_PORT}
                                        NGINX_CONF='/etc/nginx/sites-enabled/spring-app'

                                        # ── 현재 활성 색상 판단 ─────────────────────────
                                        ACTIVE='none'
                                        if [ -f \\\"\\\$APP_DIR/app-blue.pid\\\" ]; then
                                            B_PID=\\\$(cat \\\"\\\$APP_DIR/app-blue.pid\\\")
                                            kill -0 \\\$B_PID 2>/dev/null && ACTIVE='blue'
                                        fi
                                        if [ '\\\$ACTIVE' = 'none' ] && [ -f \\\"\\\$APP_DIR/app-green.pid\\\" ]; then
                                            G_PID=\\\$(cat \\\"\\\$APP_DIR/app-green.pid\\\")
                                            kill -0 \\\$G_PID 2>/dev/null && ACTIVE='green'
                                        fi

                                        if [ '\\\$ACTIVE' = 'blue' ]; then
                                            INACTIVE='green'; NEW_PORT=\\\$GREEN_PORT
                                            OLD_PID_FILE=\\\"\\\$APP_DIR/app-blue.pid\\\"
                                            NEW_PID_FILE=\\\"\\\$APP_DIR/app-green.pid\\\"
                                        else
                                            INACTIVE='blue';  NEW_PORT=\\\$BLUE_PORT
                                            OLD_PID_FILE=\\\"\\\$APP_DIR/app-green.pid\\\"
                                            NEW_PID_FILE=\\\"\\\$APP_DIR/app-blue.pid\\\"
                                        fi

                                        echo \\\"[INFO] 현재 활성: \\\$ACTIVE → 신규 배포 대상: \\\$INACTIVE (포트: \\\$NEW_PORT)\\\"

                                        # ── inactive 인스턴스 정리 ──────────────────────
                                        if [ -f \\\"\\\$NEW_PID_FILE\\\" ]; then
                                            OLD_INACTIVE_PID=\\\$(cat \\\"\\\$NEW_PID_FILE\\\")
                                            kill -9 \\\$OLD_INACTIVE_PID 2>/dev/null || true
                                            rm -f \\\"\\\$NEW_PID_FILE\\\"
                                        fi

                                        # ── 신규 인스턴스 시작 ──────────────────────────
                                        cp \\\"\\\$APP_DIR/app-new.jar\\\" \\\"\\\$APP_DIR/app.jar\\\"
                                        nohup java -Xms256m -Xmx1g \\\\
                                            -jar \\\"\\\$APP_DIR/app.jar\\\" \\\\
                                            --server.port=\\\$NEW_PORT \\\\
                                            --spring.profiles.active=prod \\\\
                                            > \\\"\\\$APP_DIR/app-\\\$INACTIVE.log\\\" 2>&1 &
                                        echo \\\$! > \\\"\\\$NEW_PID_FILE\\\"
                                        echo \\\"[INFO] 신규 인스턴스 시작 (PID: \\\$(cat \\\$NEW_PID_FILE), 포트: \\\$NEW_PORT)\\\"

                                        # ── 헬스체크 (최대 60초) ────────────────────────
                                        echo '[INFO] 헬스체크 대기 중... (최대 60초)'
                                        HEALTH_OK=false
                                        for i in \\\$(seq 1 12); do
                                            sleep 5
                                            HTTP_STATUS=\\\$(curl -s -o /dev/null -w '%{http_code}' \\\\
                                                \\\"http://localhost:\\\$NEW_PORT/actuator/health\\\" || echo '000')
                                            echo \\\"  [\\\$i/12] HTTP \\\$HTTP_STATUS\\\"
                                            if [ \\\"\\\$HTTP_STATUS\\\" = '200' ]; then
                                                HEALTH_OK=true
                                                break
                                            fi
                                        done

                                        # ── 헬스체크 실패 → 롤백 ────────────────────────
                                        if [ \\\"\\\$HEALTH_OK\\\" = 'false' ]; then
                                            echo '[ERROR] 헬스체크 실패 — 롤백: 구 버전 유지'
                                            kill -9 \\\$(cat \\\"\\\$NEW_PID_FILE\\\") 2>/dev/null || true
                                            rm -f \\\"\\\$NEW_PID_FILE\\\"
                                            exit 1
                                        fi

                                        # ── Nginx upstream 전환 ──────────────────────────
                                        if [ -f \\\"\\\$NGINX_CONF\\\" ]; then
                                            sudo sed -i \\\"s|server 127.0.0.1:[0-9]*;|server 127.0.0.1:\\\$NEW_PORT;|\\\" \\\"\\\$NGINX_CONF\\\"
                                            sudo nginx -s reload
                                            echo \\\"[OK] Nginx → 포트 \\\$NEW_PORT 전환 완료\\\"
                                        else
                                            echo '[WARN] Nginx 설정 파일 없음 — 트래픽 전환 생략'
                                            echo \\\"       \\\$NGINX_CONF 파일을 확인하세요\\\"
                                        fi

                                        # ── 구 인스턴스 Graceful Shutdown ───────────────
                                        if [ -f \\\"\\\$OLD_PID_FILE\\\" ]; then
                                            OLD_PID=\\\$(cat \\\"\\\$OLD_PID_FILE\\\")
                                            echo \\\"[INFO] 구 인스턴스 종료 (PID: \\\$OLD_PID)...\\\"
                                            kill -TERM \\\$OLD_PID 2>/dev/null || true
                                            sleep 10
                                            kill -9 \\\$OLD_PID 2>/dev/null || true
                                            rm -f \\\"\\\$OLD_PID_FILE\\\"
                                        fi

                                        echo ''
                                        echo '✅ 블루-그린 배포 완료'
                                        echo \\\"   이전: \\\$ACTIVE → 현재: \\\$INACTIVE (포트: \\\$NEW_PORT)\\\"
                                    "
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ server-blue-green 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 서버 사전 설정 확인 ────────────────────────────"
                        echo "  1. Nginx 설치 및 설정 파일 확인:"
                        echo "       /etc/nginx/sites-enabled/spring-app"
                        echo "       내용: upstream spring_app { server 127.0.0.1:8081; }"
                        echo "  2. sudo 권한 (NOPASSWD) 설정:"
                        echo "       echo 'ubuntu ALL=(ALL) NOPASSWD: /usr/sbin/nginx'"
                        echo "       | sudo tee /etc/sudoers.d/nginx"
                        echo "  3. Jenkins Credentials 'deploy-server-ssh' 등록 확인"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("server-blue-green 배포 실패: ${e.message}")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [server-docker] Docker Hub 이미지를 원격 서버에서 docker-compose로 실행
        //   사전 조건 :
        //     - Jenkins Credentials에 'dockerhub-credentials' (Username/Password) 등록
        //     - Jenkins Credentials에 'deploy-server-ssh' (SSH Key) 등록
        //     - Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 설정
        //     - Jenkinsfile 상단 DOCKER_IMAGE에 실제 DockerHub ID 입력
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: server-docker') {
            when {
                expression { params.DEPLOY_METHOD == 'server-docker' }
            }
            steps {
                script {
                    try {
                        withCredentials([sshUserPrivateKey(
                            credentialsId: 'deploy-server-ssh',
                            keyFileVariable:  'SSH_KEY',
                            usernameVariable: 'SSH_USER'
                        )]) {
                            sh """
                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} \\
                                    "mkdir -p ${REMOTE_PATH}"

                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    docker-compose.prod.yml \\
                                    ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/docker-compose.yml

                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} "
                                        cd ${REMOTE_PATH}
                                        export DOCKER_IMAGE=${DOCKER_IMAGE}
                                        export DOCKER_TAG=${DOCKER_TAG}
                                        docker-compose pull
                                        docker-compose up -d --remove-orphans
                                        docker-compose ps
                                        echo '✅ docker-compose 배포 완료'
                                    "
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ server-docker 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  1. Jenkins Credentials 'dockerhub-credentials' 등록 확인"
                        echo "  2. Jenkins Credentials 'deploy-server-ssh' 등록 확인"
                        echo "  3. 원격 서버에 Docker + docker-compose 설치 확인"
                        echo "  4. Jenkinsfile 상단 REMOTE_HOST / DOCKER_IMAGE 값 설정"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("server-docker 배포 실패: ${e.message}")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [server-k8s] Kubernetes 클러스터에 롤링 배포
        //   사전 조건 :
        //     - Jenkins Credentials에 'dockerhub-credentials' (Username/Password) 등록
        //     - Jenkins Credentials에 'kubeconfig' (Secret file: ~/.kube/config) 등록
        //     - deploy/k8s/secret.yaml의 DB 접속 정보 실제 값으로 교체 후 커밋
        //     - Jenkinsfile 상단 DOCKER_IMAGE에 실제 DockerHub ID 입력
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: server-k8s') {
            when {
                expression { params.DEPLOY_METHOD == 'server-k8s' }
            }
            steps {
                script {
                    try {
                        withCredentials([file(
                            credentialsId: "${KUBECONFIG_CREDENTIALS}",
                            variable: 'KUBECONFIG_FILE'
                        )]) {
                            sh """
                                export KUBECONFIG=\$KUBECONFIG_FILE

                                sed -i 's|IMAGE_PLACEHOLDER|${DOCKER_IMAGE}:${DOCKER_TAG}|g' deploy/k8s/deployment.yaml

                                kubectl apply -f deploy/k8s/secret.yaml     --namespace=${K8S_NAMESPACE}
                                kubectl apply -f deploy/k8s/configmap.yaml  --namespace=${K8S_NAMESPACE}
                                kubectl apply -f deploy/k8s/deployment.yaml --namespace=${K8S_NAMESPACE}
                                kubectl apply -f deploy/k8s/service.yaml    --namespace=${K8S_NAMESPACE}

                                kubectl rollout status deployment/${APP_NAME} \\
                                    --namespace=${K8S_NAMESPACE} \\
                                    --timeout=180s

                                echo "✅ Kubernetes 배포 완료"
                                kubectl get pods --namespace=${K8S_NAMESPACE} -l app=${APP_NAME}
                                kubectl get svc  --namespace=${K8S_NAMESPACE} ${APP_NAME}
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ server-k8s 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : Secret file"
                        echo "    ID      : kubeconfig"
                        echo "    File    : ~/.kube/config 파일 업로드"
                        echo "  deploy/k8s/secret.yaml DB 접속 정보 설정 확인"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("server-k8s 배포 실패: ${e.message}")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [package-zip] 고객사 설치형 JAR 릴리즈 패키지 생성
        //   내용: app.jar + config/application.yml + mapper/*.xml + bin 스크립트
        //   결과: {앱명}-{버전}-release.zip → Jenkins Artifacts에서 다운로드
        //   사전 조건: 없음
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: package-zip') {
            when {
                expression { params.DEPLOY_METHOD == 'package-zip' }
            }
            steps {
                sh """
                    mvn package -Ponpremise -q

                    ZIP_FILE=\$(ls target/*-release.zip | head -1)
                    echo ""
                    echo "✅ 고객사 JAR 패키지 생성 완료"
                    echo "   파일명 : \$ZIP_FILE"
                    echo "   크기   : \$(du -sh "\$ZIP_FILE" | cut -f1)"
                    echo "   Jenkins Artifacts 탭에서 다운로드 가능"
                """
                archiveArtifacts artifacts: 'target/*-release.zip', fingerprint: true
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [package-docker] 고객사 설치형 Docker 릴리즈 패키지 생성 (폐쇄망 지원)
        //   내용: Docker 이미지 tar.gz + docker-compose.yml + 설치 스크립트
        //   결과: {앱명}-{버전}-docker-release.zip → Jenkins Artifacts에서 다운로드
        //   사전 조건: Docker Engine 실행 중 (이미지 빌드용)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: package-docker') {
            when {
                expression { params.DEPLOY_METHOD == 'package-docker' }
            }
            steps {
                sh """
                    APP_VER=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                    PKG_NAME="${APP_NAME}-\${APP_VER}-docker"
                    PKG_DIR="dist/\${PKG_NAME}"

                    rm -rf dist/
                    mkdir -p "\$PKG_DIR"

                    echo "── Docker 이미지 저장 중... (수 분 소요)"
                    docker save ${DOCKER_IMAGE}:${DOCKER_TAG} | gzip > "\$PKG_DIR/image.tar.gz"
                    echo "   크기: \$(du -sh "\$PKG_DIR/image.tar.gz" | cut -f1)"

                    cp deploy/onpremise/docker-compose.yml "\$PKG_DIR/"
                    cp deploy/onpremise/INSTALL.md "\$PKG_DIR/"

                    cat > "\$PKG_DIR/load-and-run.sh" << 'SHEOF'
#!/bin/bash
set -e
echo "=========================================="
echo "  Spring Starter Maven 설치 시작"
echo "=========================================="
echo ""
echo "[1/3] Docker 이미지 로드 중..."
docker load < image.tar.gz
echo ""
echo "[2/3] docker-compose.yml 설정 확인..."
echo "      ★ DB 접속 정보를 수정했는지 확인하세요!"
echo "      수정: vi docker-compose.yml"
echo ""
read -p "      설정이 완료되었으면 Enter를 누르세요..." _
echo ""
echo "[3/3] 컨테이너 시작 중..."
docker-compose up -d
echo ""
echo "=========================================="
echo "  설치 완료! 브라우저에서 접속: http://localhost:8080"
echo "  로그 확인: docker-compose logs -f"
echo "=========================================="
SHEOF
                    chmod +x "\$PKG_DIR/load-and-run.sh"

                    ZIP_FILE="\${PKG_NAME}-release.zip"
                    (cd dist && zip -r "../\$ZIP_FILE" "\${PKG_NAME}/")

                    echo ""
                    echo "✅ 고객사 Docker 패키지 생성 완료"
                    echo "   파일명 : \$ZIP_FILE"
                    echo "   크기   : \$(du -sh "\$ZIP_FILE" | cut -f1)"
                    echo "   Jenkins Artifacts 탭에서 다운로드 가능"
                """
                archiveArtifacts artifacts: '*-docker-release.zip', fingerprint: true
            }
        }

    }

    // ── 빌드 후 처리 ─────────────────────────────────────────────────────────
    post {
        success {
            echo """
            ✅ 빌드 성공
            ────────────────────────────
            Job    : ${env.JOB_NAME}
            Build  : #${env.BUILD_NUMBER}
            Method : ${params.DEPLOY_METHOD}
            ────────────────────────────
            """
        }
        failure {
            echo "❌ 빌드 실패 | Job: ${env.JOB_NAME} | Build: #${env.BUILD_NUMBER}"
        }
        always {
            cleanWs()
        }
    }
}
