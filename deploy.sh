#!/bin/bash

MYSQL_HOST="192.168.100.71"
MYSQL_PORT="3306"
MYSQL_USER="webuser"
MYSQL_PASS="webuser"
MYSQL_DB="log4j_demo"

echo "🚀 Log4j Vulnerability Demo 배포 시작..."

# 0. MySQL 서버 연결 확인
echo "🔍 MySQL 서버 연결 확인 중..."
if ! nc -z $MYSQL_HOST $MYSQL_PORT; then
    echo "❌ MySQL 서버($MYSQL_HOST:$MYSQL_PORT)에 연결할 수 없습니다."
    echo "   MySQL 서버가 실행 중이고 방화벽 설정을 확인하세요."
    exit 1
fi

# MySQL 데이터베이스 확인/생성 (root 계정 사용)
echo "🗄️ MySQL 데이터베이스 설정 확인 중..."
mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASS <<EOF
CREATE DATABASE IF NOT EXISTS $MYSQL_DB;
SELECT 'MySQL 데이터베이스 설정 완료' AS status;
EOF

if [ $? -ne 0 ]; then
    echo "⚠️  MySQL 설정을 건너뜁니다. 수동으로 다음을 실행하세요:"
    echo "   CREATE DATABASE IF NOT EXISTS $MYSQL_DB;"
fi

# 1. 필수 파일 확인
echo "📋 필수 파일 확인 중..."
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ docker-compose.yml 파일이 없습니다."
    exit 1
fi

# 2. 프로젝트 빌드
echo "📦 애플리케이션 빌드 중..."
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패!"
    exit 1
fi

# 3. 기존 컨테이너 정리
echo "🧹 기존 컨테이너 정리 중..."
docker-compose down

# 4. 네트워크 정리
echo "🗑️  Docker 리소스 정리 중..."
docker network prune -f

# 5. 새 이미지 빌드
echo "🏗️ Docker 이미지 빌드 중..."
docker-compose build --no-cache

# 6. 애플리케이션 시작
echo "🚀 애플리케이션 서비스 시작 중..."
docker-compose up -d

# 7. 컨테이너 상태 확인
echo "🔍 컨테이너 상태 확인 중..."
docker-compose ps

# 8. MySQL 연결 테스트
echo "🔗 MySQL 연결 테스트 중..."
sleep 10
for i in {1..30}; do
    if mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASS -e "USE $MYSQL_DB; SELECT 1;" > /dev/null 2>&1; then
        echo "✅ MySQL 연결 성공!"
        break
    fi
    echo "MySQL 연결 대기 중... ($i/30)"
    sleep 2
done

# 9. 애플리케이션 헬스체크
echo "🏥 애플리케이션 헬스체크 대기 중..."
sleep 20

for i in {1..20}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 애플리케이션 헬스체크 성공!"
        break
    fi
    echo "애플리케이션 대기 중... ($i/20)"
    sleep 5
done

# 10. Nginx 헬스체크
if curl -f http://localhost/health > /dev/null 2>&1; then
    echo "✅ 배포 성공!"
    echo "🌐 애플리케이션 접속: http://localhost"
    echo "🔧 관리자 계정: admin / password123"
    echo "👤 일반 계정: user1 / password123"
    echo "🗄️ MySQL 서버: $MYSQL_HOST:$MYSQL_PORT"
    echo ""
    echo "📊 컨테이너 상태:"
    docker-compose ps
else
    echo "❌ 배포 실패! 로그를 확인하세요."
    echo "📋 컨테이너 상태:"
    docker-compose ps
    echo ""
    echo "📋 애플리케이션 로그:"
    docker-compose logs app
fi