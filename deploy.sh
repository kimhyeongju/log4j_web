#!/bin/bash

echo "🚀 Log4j Vulnerability Demo 배포 시작..."

# 1. 프로젝트 빌드
echo "📦 애플리케이션 빌드 중..."
./gradlew clean build -x test

# 2. 기존 컨테이너 정리
echo "🧹 기존 컨테이너 정리 중..."
docker-compose down

# 3. 새 이미지 빌드
echo "🏗️ Docker 이미지 빌드 중..."
docker-compose build --no-cache

# 4. 컨테이너 시작
echo "🚀 컨테이너 시작 중..."
docker-compose up -d

# 5. 헬스체크
echo "🏥 헬스체크 대기 중..."
sleep 30

if curl -f http://localhost/health > /dev/null 2>&1; then
    echo "✅ 배포 성공!"
    echo "🌐 애플리케이션 접속: http://localhost"
    echo "🔧 관리자 계정: admin / password123"
    echo "👤 일반 계정: user1 / password123"
else
    echo "❌ 배포 실패! 로그를 확인하세요."
    docker-compose logs
fi
