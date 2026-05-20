pipeline {
    agent any

    // ── 배포 방법 선택 파라미터 ───────────────────────────────────────────────
    parameters {
        choice(
            name: 'DEPLOY_METHOD',
            choices: ['local-docker', 'local-folder', 'local-folder-windows', 'remote-ssh'],
            description: '''배포 방법 선택
  local-docker         : 로컬 Docker 컨테이너로 실행
  local-folder         : Jenkins 컨테이너 내부 폴더에 JAR 복사
  local-folder-windows : Windows 실제 폴더(C:\\deploy)에 JAR 복사 (Jenkins 재생성 필요)
  remote-ssh           : 원격 서버에 SSH로 배포'''
        )
    }

    // ── 공통 환경변수 ─────────────────────────────────────────────────────────
    environment {
        APP_NAME      = 'spring-starter-maven'
        DOCKER_IMAGE  = "your-dockerhub-id/${APP_NAME}"   // ← DockerHub ID로 변경
        DOCKER_TAG    = "${env.BUILD_NUMBER}"

        // [방법 1] 로컬 Docker - 컨테이너 이름
        LOCAL_CONTAINER = 'spring-app'

        // [방법 2] 로컬 폴더 - Jenkins 홈 하위 경로 (별도 마운트 불필요)
        LOCAL_DEPLOY_PATH = '/var/jenkins_home/deploy'

        // [방법 3] 원격 SSH
        REMOTE_HOST   = '원격서버IP'                       // ← 원격 서버 IP로 변경
        REMOTE_USER   = 'ubuntu'                           // ← 원격 서버 계정으로 변경
        REMOTE_PATH   = '/home/ubuntu/app'
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
                sh 'mvn clean package -DskipTests -q'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
                success {
                    // 빌드 산출물(JAR) 보관
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [방법 1] 로컬 Docker 컨테이너로 실행
        //   - Dockerfile로 이미지 빌드 후 로컬에서 바로 컨테이너 실행
        //   - Spring Boot 앱이 localhost:8080 으로 즉시 뜸
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Docker') {
            when {
                expression { params.DEPLOY_METHOD == 'local-docker' }
            }
            steps {
                script {
                    echo "▶ [방법 1] 로컬 Docker 컨테이너 배포"

                    // 이미지 빌드
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker tag  ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"

                    // 기존 컨테이너 제거 후 새 컨테이너 실행
                    sh """
                        docker stop ${LOCAL_CONTAINER} || true
                        docker rm   ${LOCAL_CONTAINER} || true

                        docker run -d \\
                          --name ${LOCAL_CONTAINER} \\
                          -p 8081:8081 \\
                          -e SPRING_PROFILES_ACTIVE=local \\
                          -e DB_HOST=host.docker.internal \\
                          -e DB_PORT=3306 \\
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
        // [방법 2] 로컬 특정 폴더에 JAR 복사
        //   - Jenkins 컨테이너 실행 시 -v C:\deploy:/var/deploy 마운트 필요
        //   - 빌드된 JAR가 Windows C:\deploy 폴더에 복사됨
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Folder') {
            when {
                expression { params.DEPLOY_METHOD == 'local-folder' }
            }
            steps {
                echo "▶ [방법 2] 로컬 폴더 배포 → ${LOCAL_DEPLOY_PATH}"

                sh """
                    mkdir -p ${LOCAL_DEPLOY_PATH}

                    # JAR 복사 (타임스탬프 백업 포함)
                    cp target/*.jar ${LOCAL_DEPLOY_PATH}/app.jar
                    cp target/*.jar ${LOCAL_DEPLOY_PATH}/app-${DOCKER_TAG}.jar

                    echo "✅ JAR 복사 완료"
                    ls -lh ${LOCAL_DEPLOY_PATH}/
                """
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [방법 2-B] Windows 실제 폴더에 JAR 복사
        //   - Jenkins 컨테이너 실행 시 -v C:\deploy:/var/deploy 마운트 필요
        //   - 빌드된 JAR가 Windows C:\deploy 폴더에 복사됨
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Folder (Windows)') {
            when {
                expression { params.DEPLOY_METHOD == 'local-folder-windows' }
            }
            steps {
                echo "▶ [방법 2-B] Windows 폴더 배포 → C:\\deploy"

                sh """
                    mkdir -p /var/deploy

                    cp target/*.jar /var/deploy/app.jar
                    cp target/*.jar /var/deploy/app-${DOCKER_TAG}.jar

                    echo "✅ C:\\\\deploy\\\\app.jar 복사 완료"
                    ls -lh /var/deploy/
                """
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [방법 3] 원격 서버에 SSH로 JAR 전송 후 실행
        //   - Jenkins Credentials에 'deploy-server-ssh' 등록 필요
        //   - 원격 서버에 Java 17 설치 필요
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Remote SSH') {
            when {
                expression { params.DEPLOY_METHOD == 'remote-ssh' }
            }
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'deploy-server-ssh',    // Jenkins Credentials ID
                    keyFileVariable:  'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )]) {
                    sh """
                        echo "▶ [방법 3] 원격 서버 SSH 배포 → ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}"

                        # 원격 서버에 배포 폴더 생성
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                            ${REMOTE_USER}@${REMOTE_HOST} \\
                            "mkdir -p ${REMOTE_PATH}"

                        # JAR 파일 전송 (SCP)
                        scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                            target/*.jar \\
                            ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/app.jar

                        # 원격 서버에서 기존 프로세스 종료 후 재시작
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                            ${REMOTE_USER}@${REMOTE_HOST} "
                                # 기존 프로세스 종료
                                pkill -f 'java -jar' || true
                                sleep 2

                                # 백그라운드로 새 프로세스 실행
                                nohup java -jar ${REMOTE_PATH}/app.jar \\
                                  --spring.profiles.active=prod \\
                                  > ${REMOTE_PATH}/app.log 2>&1 &

                                echo '✅ 원격 서버 배포 완료'
                                sleep 3
                                # 프로세스 확인
                                ps aux | grep 'java -jar'
                            "
                    """
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
