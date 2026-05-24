pipeline {
    agent any

    // ── 배포 방법 선택 파라미터 ───────────────────────────────────────────────
    parameters {
        choice(
            name: 'DEPLOY_METHOD',
            choices: ['local-windows-folder', 'local-windows-docker', 'local-docker', 'remote-ssh', 'dockerhub-compose', 'kubernetes'],
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

  dockerhub-compose  ★ 현업 표준 (중소규모)
    Docker 이미지를 빌드해 Docker Hub에 올린 뒤 원격 서버에서 docker-compose로 실행
    결과 → 원격 서버에서 컨테이너 구동 (서버 IP:8081 접속)
    사전 조건 : Docker Hub Credentials(dockerhub-credentials) + SSH 키(deploy-server-ssh) 등록

  kubernetes  ★ 현업 표준 (대규모/클라우드)
    Docker 이미지를 빌드해 레지스트리에 올린 뒤 Kubernetes 클러스터에 자동 배포
    결과 → K8s 클러스터에서 롤링 업데이트 구동 (LoadBalancer IP:80 접속)
    사전 조건 : K8s 클러스터 + kubeconfig Credentials(kubeconfig) + Docker Hub Credentials 등록

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
                              -DsuppressionFile=owasp-suppressions.xml \
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
                    params.DEPLOY_METHOD in ['local-docker', 'local-windows-docker', 'dockerhub-compose', 'kubernetes']
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
                    params.DEPLOY_METHOD in ['local-docker', 'local-windows-docker', 'dockerhub-compose', 'kubernetes']
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
                          -e DB_USERNAME=root \\
                          -e DB_PASSWORD=admin \\
                          ${DOCKER_IMAGE}:latest

                        echo "✅ 컨테이너 실행 완료 → http://localhost:8081"
                        ls -lh /var/deploy/
                    """
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
                          -e DB_USERNAME=root \\
                          -e DB_PASSWORD=admin \\
                          ${DOCKER_IMAGE}:latest

                        echo "✅ 컨테이너 실행 완료 → http://localhost:8081"
                    """
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
        //     - k8s/secret.yaml의 DB 접속 정보 실제 값으로 교체 후 커밋
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
                                sed -i 's|IMAGE_PLACEHOLDER|${DOCKER_IMAGE}:${DOCKER_TAG}|g' k8s/deployment.yaml

                                # Secret / ConfigMap / Deployment / Service 순서로 적용
                                kubectl apply -f k8s/secret.yaml     --namespace=${K8S_NAMESPACE}
                                kubectl apply -f k8s/configmap.yaml  --namespace=${K8S_NAMESPACE}
                                kubectl apply -f k8s/deployment.yaml --namespace=${K8S_NAMESPACE}
                                kubectl apply -f k8s/service.yaml    --namespace=${K8S_NAMESPACE}

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
                        echo "  k8s/secret.yaml DB 접속 정보 실제 값으로 설정 확인"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("Kubernetes 배포 실패: ${e.message}")
                    }
                }
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
