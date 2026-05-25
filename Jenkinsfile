pipeline {
    agent any

    // ── 배포 방법 선택 파라미터 ───────────────────────────────────────────────
    parameters {
        choice(
            name: 'DEPLOY_METHOD',
            choices: ['local-windows-folder', 'local-windows-docker', 'local-docker', 'remote-ssh', 'dockerhub-compose', 'kubernetes', 'onpremise-zip', 'onpremise-docker'],
            description: '''── 배포 방법을 선택하세요 ──────────────────────────────────────────

  local-windows-folder
    빌드된 JAR 파일을 이 PC의 Windows 폴더에 복사 (앱 실행 X, 파일만 저장)
    결과 → C:\\SJSJSS\\Project\\01.File\\StarterMavenProject\\app.jar
    사전 조건 : Jenkins 컨테이너에 해당 폴더 볼륨 마운트 설정 필요

  local-windows-docker
    JAR를 Windows 폴더에 저장하고 Docker 이미지로 만들어 컨테이너로 즉시 실행
    결과 1 → C:\\SJSJSS\\Project\\01.File\\StarterMavenProject\\app.jar (파일 저장)
    결과 2 → http://localhost:8081 (컨테이너 실행)
    사전 조건 : Jenkins 볼륨 마운트 설정 + Docker Desktop 실행 중

  local-docker
    빌드한 앱을 Docker 이미지로 만들어 현재 PC에서 컨테이너로 즉시 실행
    결과 → 브라우저에서 http://localhost:8081 로 바로 접속 가능
    사전 조건 : Docker Desktop 실행 중

  remote-ssh
    빌드된 JAR 파일을 외부 리눅스 서버에 전송한 뒤 서버에서 앱 자동 실행
    결과 → 원격 서버에서 앱 구동 (서버 IP:8081 접속)
    사전 조건 : Jenkinsfile 내 REMOTE_HOST/USER/PATH 설정 + SSH 키 등록

  dockerhub-compose  ★ 현업 표준 (중소규모/SaaS)
    Docker 이미지를 빌드해 Docker Hub에 올린 뒤 원격 서버에서 docker-compose로 실행
    결과 → 원격 서버에서 컨테이너 구동 (서버 IP:8081 접속)
    사전 조건 : Docker Hub Credentials(dockerhub-credentials) + SSH 키(deploy-server-ssh) 등록

  kubernetes  ★ 현업 표준 (대규모/클라우드/SaaS)
    Docker 이미지를 빌드해 레지스트리에 올린 뒤 Kubernetes 클러스터에 자동 배포
    결과 → K8s 클러스터에서 롤링 업데이트 구동 (LoadBalancer IP:80 접속)
    사전 조건 : K8s 클러스터 + kubeconfig Credentials(kubeconfig) + Docker Hub Credentials 등록

  onpremise-zip  ★ B2B 고객사 설치형 (JAR 패키지)
    JAR + 설정 파일 템플릿 + SQL 매퍼 + 시작/종료 스크립트를 ZIP으로 패키징
    결과 → {앱명}-{버전}-release.zip (Jenkins Artifacts에서 다운로드)
    내용 : app.jar / config/application.yml / mapper/*.xml / bin/start.sh 등
    사용 : 고객사 서버에 JDK 17만 있으면 ZIP 해제 후 config 수정 → 실행
    사전 조건 : 없음 (빌드만 되면 즉시 사용 가능)

  onpremise-docker  ★ B2B 고객사 설치형 (Docker 패키지)
    Docker 이미지를 tar.gz로 저장하고 docker-compose와 함께 ZIP으로 패키징
    결과 → {앱명}-{버전}-docker-release.zip (Jenkins Artifacts에서 다운로드)
    내용 : image.tar.gz / docker-compose.yml / INSTALL.md / load-and-run.sh
    사용 : 인터넷 없는 폐쇄망 고객사 서버에서도 Docker만 있으면 즉시 설치 가능
    사전 조건 : Docker Desktop 또는 Docker Engine 실행 중

────────────────────────────────────────────────────────────────'''
        )
    }

    // ── 공통 환경변수 ─────────────────────────────────────────────────────────
    environment {
        APP_NAME      = 'spring-starter-maven'
        DOCKER_IMAGE  = "your-dockerhub-id/${APP_NAME}"   // ← DockerHub ID로 변경
        DOCKER_TAG    = "${env.BUILD_NUMBER}"

        // [local-docker / local-windows-docker] 컨테이너 이름
        LOCAL_CONTAINER = 'spring-app'

        // [remote-ssh / dockerhub-compose] 원격 서버 정보
        REMOTE_HOST   = '원격서버IP'                       // ← 원격 서버 IP로 변경
        REMOTE_USER   = 'ubuntu'                           // ← 원격 서버 계정으로 변경
        REMOTE_PATH   = '/home/ubuntu/app'

        // [local-docker / local-windows-docker] DB Credentials ID
        DB_CREDENTIALS = 'db-credentials'

        // [dockerhub-compose / kubernetes] Docker Hub Credentials ID
        DOCKERHUB_CREDENTIALS = 'dockerhub-credentials'

        // [kubernetes] kubeconfig Credentials ID & 네임스페이스
        KUBECONFIG_CREDENTIALS = 'kubeconfig'
        K8S_NAMESPACE          = 'default'
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
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
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
                    params.DEPLOY_METHOD in ['local-docker', 'local-windows-docker', 'dockerhub-compose', 'kubernetes', 'onpremise-docker']
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
                    params.DEPLOY_METHOD in ['local-docker', 'local-windows-docker', 'dockerhub-compose', 'kubernetes', 'onpremise-docker']
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

        // ⑦ Docker Hub Push (dockerhub-compose / kubernetes 공통)
        stage('Docker Hub Push') {
            when {
                expression {
                    params.DEPLOY_METHOD in ['dockerhub-compose', 'kubernetes']
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
                        echo "    Password: Docker Hub 비밀번호 또는 Access Token"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("Docker Hub Credentials 'dockerhub-credentials' 미등록 또는 Push 실패")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [local-windows-folder] 빌드된 JAR를 Windows 폴더에 복사 (실행 X)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Windows Folder') {
            when {
                expression { params.DEPLOY_METHOD == 'local-windows-folder' }
            }
            steps {
                sh """
                    mkdir -p /var/deploy

                    APP_JAR=\$(ls target/*.jar | grep -v plugin | head -1)
                    echo "배포 대상 JAR: \$APP_JAR"

                    cp "\$APP_JAR" /var/deploy/app.jar
                    cp "\$APP_JAR" /var/deploy/app-${DOCKER_TAG}.jar

                    # ── 수동 실행 스크립트 생성 (더블클릭으로 실행 가능) ──────────
                    cat > /var/deploy/run.bat << 'BATEOF'
@echo off
echo Starting Spring Boot app...
"C:\\SJSJSS\\Project\\0.jdk\\jdk-17.0.12\\bin\\java.exe" ^
  -Dspring.profiles.active=local ^
  -jar "C:\\SJSJSS\\Project\\01.File\\StarterMavenProject\\app.jar"
pause
BATEOF

                    cat > /var/deploy/run.ps1 << 'PS1EOF'
& "C:\\SJSJSS\\Project\\0.jdk\\jdk-17.0.12\\bin\\java.exe" `
  -Dspring.profiles.active=local `
  -jar "C:\\SJSJSS\\Project\\01.File\\StarterMavenProject\\app.jar"
PS1EOF

                    echo "✅ 배포 완료 → C:\\\\SJSJSS\\\\Project\\\\01.File\\\\StarterMavenProject\\\\app.jar"
                    ls -lh /var/deploy/
                """
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [local-windows-docker] JAR를 Windows 폴더에 저장 + Docker 컨테이너로 즉시 실행
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Windows Docker') {
            when {
                expression { params.DEPLOY_METHOD == 'local-windows-docker' }
            }
            steps {
                script {
                    try {
                        withCredentials([usernamePassword(
                            credentialsId: "${DB_CREDENTIALS}",
                            usernameVariable: 'DB_USER',
                            passwordVariable: 'DB_PASS'
                        )]) {
                            sh """
                                mkdir -p /var/deploy

                                APP_JAR=\$(ls target/*.jar | grep -v plugin | head -1)
                                echo "배포 대상 JAR: \$APP_JAR"

                                # ── Windows 폴더에 JAR 복사 ────────────────────────────
                                cp "\$APP_JAR" /var/deploy/app.jar
                                cp "\$APP_JAR" /var/deploy/app-${DOCKER_TAG}.jar
                                echo "✅ JAR 복사 완료 → C:\\\\SJSJSS\\\\Project\\\\01.File\\\\StarterMavenProject\\\\app.jar"

                                # ── Docker 컨테이너 실행 ───────────────────────────────
                                docker stop ${LOCAL_CONTAINER} || true
                                docker rm   ${LOCAL_CONTAINER} || true

                                docker run -d \\
                                  --name ${LOCAL_CONTAINER} \\
                                  -p 8081:8081 \\
                                  -e SPRING_PROFILES_ACTIVE=local \\
                                  -e DB_HOST=host.docker.internal \\
                                  -e DB_PORT=50002 \\
                                  -e DB_NAME=SJSJSS \\
                                  -e DB_USERNAME=\$DB_USER \\
                                  -e DB_PASSWORD=\$DB_PASS \\
                                  ${DOCKER_IMAGE}:latest

                                echo "✅ 컨테이너 실행 완료 → http://localhost:8081"
                                ls -lh /var/deploy/
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ DB Credentials 미등록 - 배포를 건너뜁니다"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : Username with password"
                        echo "    ID      : db-credentials"
                        echo "    Username: DB 계정 (예: root)"
                        echo "    Password: DB 비밀번호"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("DB Credentials 'db-credentials' 미등록")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [local-docker] Docker 이미지 빌드 후 현재 PC에서 컨테이너로 즉시 실행
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Docker') {
            when {
                expression { params.DEPLOY_METHOD == 'local-docker' }
            }
            steps {
                script {
                    try {
                        withCredentials([usernamePassword(
                            credentialsId: "${DB_CREDENTIALS}",
                            usernameVariable: 'DB_USER',
                            passwordVariable: 'DB_PASS'
                        )]) {
                            sh """
                                docker stop ${LOCAL_CONTAINER} || true
                                docker rm   ${LOCAL_CONTAINER} || true

                                docker run -d \\
                                  --name ${LOCAL_CONTAINER} \\
                                  -p 8081:8081 \\
                                  -e SPRING_PROFILES_ACTIVE=local \\
                                  -e DB_HOST=host.docker.internal \\
                                  -e DB_PORT=50002 \\
                                  -e DB_NAME=SJSJSS \\
                                  -e DB_USERNAME=\$DB_USER \\
                                  -e DB_PASSWORD=\$DB_PASS \\
                                  ${DOCKER_IMAGE}:latest

                                echo "✅ 컨테이너 실행 완료 → http://localhost:8081"
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ DB Credentials 미등록 - 배포를 건너뜁니다"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : Username with password"
                        echo "    ID      : db-credentials"
                        echo "    Username: DB 계정 (예: root)"
                        echo "    Password: DB 비밀번호"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("DB Credentials 'db-credentials' 미등록")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [remote-ssh] 원격 서버에 JAR 전송 후 자동 실행
        //   사전 조건 : Jenkins Credentials에 'deploy-server-ssh' 등록
        //             Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 설정
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Remote SSH') {
            when {
                expression { params.DEPLOY_METHOD == 'remote-ssh' }
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

                                APP_JAR=\$(ls target/*.jar | grep -v plugin | head -1)
                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    "\$APP_JAR" \\
                                    ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/app.jar

                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} "
                                        pkill -f 'java -jar' || true
                                        sleep 2
                                        nohup java -jar ${REMOTE_PATH}/app.jar \\
                                          --spring.profiles.active=prod \\
                                          > ${REMOTE_PATH}/app.log 2>&1 &
                                        sleep 3
                                        ps aux | grep 'java -jar'
                                        echo '✅ 원격 서버 배포 완료'
                                    "
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ SSH 배포 사전 조건 미충족 - 배포를 건너뜁니다"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : SSH Username with private key"
                        echo "    ID      : deploy-server-ssh"
                        echo "  Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 값 설정"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("SSH credentials 'deploy-server-ssh' 미등록 또는 서버 연결 실패")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [dockerhub-compose] Docker Hub 이미지를 원격 서버에서 docker-compose로 실행
        //   사전 조건 :
        //     - Jenkins Credentials에 'dockerhub-credentials' (Username/Password) 등록
        //     - Jenkins Credentials에 'deploy-server-ssh' (SSH Key) 등록
        //     - Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 설정
        //     - Jenkinsfile 상단 DOCKER_IMAGE에 실제 DockerHub ID 입력
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: DockerHub + Compose') {
            when {
                expression { params.DEPLOY_METHOD == 'dockerhub-compose' }
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
                                # docker-compose.prod.yml을 원격 서버로 전송
                                ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    ${REMOTE_USER}@${REMOTE_HOST} \\
                                    "mkdir -p ${REMOTE_PATH}"

                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    docker-compose.prod.yml \\
                                    ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/docker-compose.yml

                                # 원격 서버에서 이미지 Pull 후 컨테이너 재시작
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
                        echo "⚠ DockerHub+Compose 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  1. Jenkins Credentials 'dockerhub-credentials' 등록 확인"
                        echo "  2. Jenkins Credentials 'deploy-server-ssh' 등록 확인"
                        echo "  3. 원격 서버에 Docker + docker-compose 설치 확인"
                        echo "  4. Jenkinsfile 상단 REMOTE_HOST / DOCKER_IMAGE 값 설정"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("DockerHub+Compose 배포 실패: ${e.message}")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [kubernetes] Kubernetes 클러스터에 롤링 배포
        //   사전 조건 :
        //     - Jenkins Credentials에 'dockerhub-credentials' (Username/Password) 등록
        //     - Jenkins Credentials에 'kubeconfig' (Secret file: ~/.kube/config) 등록
        //     - deploy/k8s/secret.yaml의 DB 접속 정보 실제 값으로 교체 후 커밋
        //     - Jenkinsfile 상단 DOCKER_IMAGE에 실제 DockerHub ID 입력
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Kubernetes') {
            when {
                expression { params.DEPLOY_METHOD == 'kubernetes' }
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

                                # deployment.yaml의 IMAGE_PLACEHOLDER를 실제 이미지 태그로 치환
                                sed -i 's|IMAGE_PLACEHOLDER|${DOCKER_IMAGE}:${DOCKER_TAG}|g' deploy/k8s/deployment.yaml

                                # Secret / ConfigMap / Deployment / Service 순서로 적용
                                kubectl apply -f deploy/k8s/secret.yaml     --namespace=${K8S_NAMESPACE}
                                kubectl apply -f deploy/k8s/configmap.yaml  --namespace=${K8S_NAMESPACE}
                                kubectl apply -f deploy/k8s/deployment.yaml --namespace=${K8S_NAMESPACE}
                                kubectl apply -f deploy/k8s/service.yaml    --namespace=${K8S_NAMESPACE}

                                # 롤링 배포 완료 대기 (최대 3분)
                                kubectl rollout status deployment/${APP_NAME} \\
                                    --namespace=${K8S_NAMESPACE} \\
                                    --timeout=180s

                                echo "✅ Kubernetes 배포 완료"
                                kubectl get pods --namespace=${K8S_NAMESPACE} -l app=${APP_NAME}
                                kubectl get svc  --namespace=${K8S_NAMESPACE} ${APP_NAME}
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠ Kubernetes 배포 실패"
                        echo "  원인: ${e.message}"
                        echo "  ── 확인 사항 ──────────────────────────────────────"
                        echo "  Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "    Kind    : Secret file"
                        echo "    ID      : kubeconfig"
                        echo "    File    : ~/.kube/config 파일 업로드"
                        echo "  deploy/k8s/secret.yaml DB 접속 정보 실제 값으로 설정 확인"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("Kubernetes 배포 실패: ${e.message}")
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [onpremise-zip] 고객사 설치형 JAR 릴리즈 패키지 생성
        //   내용: app.jar + config/application.yml + mapper/*.xml + bin 스크립트
        //   결과: {앱명}-{버전}-release.zip → Jenkins Artifacts에서 다운로드
        //   사전 조건: 없음 (빌드 성공 후 바로 사용 가능)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: On-Premise ZIP') {
            when {
                expression { params.DEPLOY_METHOD == 'onpremise-zip' }
            }
            steps {
                sh """
                    # 프로젝트 버전을 pom.xml에서 추출
                    APP_VER=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                    PKG_NAME="${APP_NAME}-\${APP_VER}"
                    PKG_DIR="dist/\${PKG_NAME}"

                    echo "── 패키지 디렉터리 생성: \$PKG_DIR"
                    rm -rf dist/
                    mkdir -p "\$PKG_DIR/config"
                    mkdir -p "\$PKG_DIR/mapper"
                    mkdir -p "\$PKG_DIR/bin"
                    mkdir -p "\$PKG_DIR/logs"

                    # ── JAR 파일 복사 ─────────────────────────────────────────
                    APP_JAR=\$(ls target/*.jar | grep -v plugin | head -1)
                    echo "── JAR 복사: \$APP_JAR"
                    cp "\$APP_JAR" "\$PKG_DIR/app.jar"

                    # ── 고객용 설정 파일 복사 ─────────────────────────────────
                    # 이 파일이 JAR 옆 config/ 디렉터리에 위치하면
                    # Spring Boot가 자동으로 JAR 내부 설정보다 우선 적용
                    echo "── 설정 파일 복사: deploy/onpremise/config/application.yml"
                    cp deploy/onpremise/config/application.yml "\$PKG_DIR/config/"

                    # ── MyBatis XML 매퍼 복사 ────────────────────────────────
                    # 고객이 SQL을 수정하고 싶을 때 이 폴더의 파일을 편집 후 재시작
                    echo "── SQL 매퍼 파일 복사"
                    find src/main/resources/static/mybatis/mapper -name "*.xml" \\
                        -exec cp {} "\$PKG_DIR/mapper/" \\; 2>/dev/null || true

                    # ── 실행 스크립트 복사 ────────────────────────────────────
                    echo "── 시작/종료/설치 스크립트 복사"
                    cp deploy/onpremise/bin/start.sh    "\$PKG_DIR/bin/"
                    cp deploy/onpremise/bin/stop.sh     "\$PKG_DIR/bin/"
                    cp deploy/onpremise/bin/install.sh  "\$PKG_DIR/bin/"
                    cp deploy/onpremise/bin/start.bat   "\$PKG_DIR/bin/"
                    cp deploy/onpremise/bin/stop.bat    "\$PKG_DIR/bin/"
                    cp deploy/onpremise/bin/install.bat "\$PKG_DIR/bin/"
                    chmod +x "\$PKG_DIR/bin/"*.sh

                    # ── 설치 가이드 복사 ──────────────────────────────────────
                    cp deploy/onpremise/INSTALL.md "\$PKG_DIR/"

                    # ── ZIP 패키지 생성 ───────────────────────────────────────
                    ZIP_FILE="\${PKG_NAME}-release.zip"
                    echo "── ZIP 생성: \$ZIP_FILE"
                    (cd dist && zip -r "../\$ZIP_FILE" "\${PKG_NAME}/")

                    echo ""
                    echo "✅ 온프레미스 JAR 패키지 생성 완료"
                    echo "   파일명 : \$ZIP_FILE"
                    echo "   크기   : \$(du -sh "\$ZIP_FILE" | cut -f1)"
                    echo "   Jenkins Artifacts 탭에서 다운로드 가능"
                    echo ""
                    echo "── 패키지 내용 ─────────────────────────────"
                    find dist/\${PKG_NAME} -type f | sort
                """
                archiveArtifacts artifacts: '*-release.zip', fingerprint: true
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [onpremise-docker] 고객사 설치형 Docker 릴리즈 패키지 생성
        //   내용: Docker 이미지 tar.gz + docker-compose.yml + 설치 스크립트
        //   결과: {앱명}-{버전}-docker-release.zip → Jenkins Artifacts에서 다운로드
        //   특징: 인터넷이 없는 폐쇄망 고객사에서도 Docker만 있으면 즉시 설치 가능
        //   사전 조건: Docker Build 스테이지 성공 후 실행 (자동으로 포함됨)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: On-Premise Docker') {
            when {
                expression { params.DEPLOY_METHOD == 'onpremise-docker' }
            }
            steps {
                sh """
                    # 프로젝트 버전을 pom.xml에서 추출
                    APP_VER=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                    PKG_NAME="${APP_NAME}-\${APP_VER}-docker"
                    PKG_DIR="dist/\${PKG_NAME}"

                    echo "── 패키지 디렉터리 생성: \$PKG_DIR"
                    rm -rf dist/
                    mkdir -p "\$PKG_DIR"

                    # ── Docker 이미지를 tar.gz로 저장 ────────────────────────
                    # docker save: 이미지를 파일로 내보냄 (모든 레이어 포함)
                    # gzip: 압축해서 파일 크기 줄임 (30~60% 압축률)
                    # → 인터넷 없는 고객사 서버에서 'docker load'로 즉시 설치 가능
                    echo "── Docker 이미지 저장 중... (수 분 소요될 수 있음)"
                    docker save ${DOCKER_IMAGE}:${DOCKER_TAG} | gzip > "\$PKG_DIR/image.tar.gz"
                    echo "   이미지 크기: \$(du -sh "\$PKG_DIR/image.tar.gz" | cut -f1)"

                    # ── docker-compose 파일 복사 ──────────────────────────────
                    echo "── docker-compose.yml 복사"
                    cp deploy/onpremise/docker-compose.yml "\$PKG_DIR/"

                    # ── 설치 가이드 복사 ──────────────────────────────────────
                    cp deploy/onpremise/INSTALL.md "\$PKG_DIR/"

                    # ── Linux 원클릭 설치 스크립트 생성 ─────────────────────
                    # 고객이 복잡한 명령어 없이 한 번의 실행으로 설치 완료
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
echo "      수정: vi docker-compose.yml 또는 nano docker-compose.yml"
echo ""
read -p "      설정이 완료되었으면 Enter를 누르세요..." _
echo ""
echo "[3/3] 컨테이너 시작 중..."
docker-compose up -d
echo ""
echo "=========================================="
echo "  설치 완료!"
echo "  브라우저에서 접속: http://localhost:8080"
echo "  로그 확인: docker-compose logs -f"
echo "=========================================="
SHEOF
                    chmod +x "\$PKG_DIR/load-and-run.sh"

                    # ── ZIP 패키지 생성 ───────────────────────────────────────
                    ZIP_FILE="\${PKG_NAME}-release.zip"
                    echo "── ZIP 생성: \$ZIP_FILE"
                    (cd dist && zip -r "../\$ZIP_FILE" "\${PKG_NAME}/")

                    echo ""
                    echo "✅ 온프레미스 Docker 패키지 생성 완료"
                    echo "   파일명 : \$ZIP_FILE"
                    echo "   크기   : \$(du -sh "\$ZIP_FILE" | cut -f1)"
                    echo "   Jenkins Artifacts 탭에서 다운로드 가능"
                    echo ""
                    echo "── 패키지 내용 ─────────────────────────────"
                    ls -lh "\$PKG_DIR/"
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
