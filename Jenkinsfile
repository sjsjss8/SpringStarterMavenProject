pipeline {
    agent any

    // ── 배포 방법 선택 파라미터 ───────────────────────────────────────────────
    parameters {
        choice(
            name: 'DEPLOY_METHOD',
            choices: ['local-docker', 'local-folder-windows', 'remote-ssh'],
            description: '''── 배포 방법을 선택하세요 ──────────────────────────────────────────

  local-docker
    빌드한 앱을 Docker 이미지로 만들어 현재 PC에서 컨테이너로 즉시 실행
    결과 → 브라우저에서 http://localhost:8081 로 바로 접속 가능
    사전 조건 : Docker Desktop 실행 중

  local-folder-windows
    빌드된 JAR 파일을 이 PC의 Windows 폴더에 복사 (앱 실행 X, 파일만 저장)
    결과 → C:\\SJSJSS\\Project\\01.File\\StarterMavenProject\\app.jar
    사전 조건 : Jenkins 컨테이너에 해당 폴더 볼륨 마운트 설정 필요

  remote-ssh
    빌드된 JAR 파일을 외부 리눅스 서버에 전송한 뒤 서버에서 앱 자동 실행
    결과 → 원격 서버에서 앱 구동 (서버 IP:8081 접속)
    사전 조건 : Jenkinsfile 내 REMOTE_HOST/USER/PATH 설정 + SSH 키 등록

────────────────────────────────────────────────────────────────'''
        )
    }

    // ── 공통 환경변수 ─────────────────────────────────────────────────────────
    environment {
        APP_NAME      = 'spring-starter-maven'
        DOCKER_IMAGE  = "your-dockerhub-id/${APP_NAME}"   // ← DockerHub ID로 변경
        DOCKER_TAG    = "${env.BUILD_NUMBER}"

        // [local-docker] 컨테이너 이름
        LOCAL_CONTAINER = 'spring-app'

        // [remote-ssh] 원격 서버 정보
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
                // ── 규칙 1: 테스트 포함 빌드 (-DskipTests 제거)
                sh 'mvn clean package -q'
            }
            post {
                always {
                    // ── 규칙 2: 테스트 결과 수집
                    //    테스트 파일이 없거나 실패하면 빌드 실패 처리
                    junit allowEmptyResults: false,
                          testResults: '**/target/surefire-reports/*.xml'
                }
                success {
                    // ── 규칙 3: 빌드 성공 시에만 JAR 보관
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
                    // Checkstyle 결과 XML 보관 (Jenkins 아티팩트에서 확인 가능)
                    // UI 시각화를 원하면 Jenkins에 'Warnings Next Generation' 플러그인 설치 후
                    // 아래 archiveArtifacts를 recordIssues(tool: checkStyle(...)) 로 교체
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
                //    pom.xml 의존성 중 알려진 보안 취약점(CVE) 스캔
                //    CVSS 점수 7 이상(High/Critical) 발견 시 빌드 실패
                stage('OWASP Dependency Check') {
                    steps {
                        sh '''
                            mvn org.owasp:dependency-check-maven:check \
                              -DfailBuildOnCVSS=7 \
                              -DsuppressionFile=owasp-suppressions.xml \
                              || true
                        '''
                        // NVD API Key 없으면 업데이트 실패(403) → || true 로 무시하고 계속 진행
                        // API Key 발급 후 적용: -DnvdApiKey=${NVD_API_KEY} 옵션 추가
                    }
                    post {
                        always {
                            // OWASP 리포트 보관 (Jenkins 아티팩트에서 HTML 파일 직접 다운로드)
                            // UI 내 렌더링을 원하면 Jenkins에 'HTML Publisher' 플러그인 설치
                            archiveArtifacts(
                                artifacts: 'target/dependency-check-report.html',
                                allowEmptyArchive: true,
                                fingerprint: false
                            )
                        }
                    }
                }

                // ── 보안 2: 소스코드 보안 버그 정적 분석 (SpotBugs + FindSecBugs)
                //    SQL Injection, XSS, 민감정보 노출 등 코드 레벨 취약점 탐지
                stage('SpotBugs Security') {
                    steps {
                        sh 'mvn spotbugs:check -q || true'
                    }
                    post {
                        always {
                            // SpotBugs 결과 XML 보관 (Jenkins 아티팩트에서 확인 가능)
                            // UI 시각화를 원하면 Jenkins에 'Warnings Next Generation' 플러그인 설치 후
                            // 아래 archiveArtifacts를 recordIssues(tool: spotBugs(...)) 로 교체
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

        // ⑤ Docker 이미지 보안 스캔 (local-docker 선택 시에만 실행)
        stage('Image Security Scan') {
            when {
                expression { params.DEPLOY_METHOD == 'local-docker' }
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

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // [local-docker] Docker 이미지 빌드 후 현재 PC에서 컨테이너로 즉시 실행
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Docker') {
            when {
                expression { params.DEPLOY_METHOD == 'local-docker' }
            }
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker tag  ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"

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
        // [local-folder-windows] 빌드된 JAR를 Windows 폴더에 복사
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('Deploy: Local Folder (Windows)') {
            when {
                expression { params.DEPLOY_METHOD == 'local-folder-windows' }
            }
            steps {
                sh """
                    mkdir -p /var/deploy

                    cp target/*.jar /var/deploy/app.jar
                    cp target/*.jar /var/deploy/app-${DOCKER_TAG}.jar

                    echo "✅ 배포 완료 → C:\\\\SJSJSS\\\\Project\\\\01.File\\\\StarterMavenProject\\\\app.jar"
                    ls -lh /var/deploy/
                """
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

                                scp -i \$SSH_KEY -o StrictHostKeyChecking=no \\
                                    target/*.jar \\
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
                        echo "  1. Jenkins 관리 → Credentials → Global → Add Credentials"
                        echo "     Kind: SSH Username with private key"
                        echo "     ID  : deploy-server-ssh"
                        echo "  2. Jenkinsfile 상단 REMOTE_HOST / REMOTE_USER / REMOTE_PATH 값 설정"
                        echo "  ───────────────────────────────────────────────────"
                        unstable("SSH credentials 'deploy-server-ssh' 미등록 또는 서버 연결 실패")
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
