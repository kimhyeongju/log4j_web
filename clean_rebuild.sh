#!/bin/bash

echo "🧹 완전 정리 및 재빌드 시작..."

# 1. Docker Compose 서비스 중지
echo "🛑 Docker Compose 서비스 중지 중..."
docker-compose down -v --remove-orphans 2>/dev/null || true

# 2. 모든 Docker 컨테이너 중지 및 제거
echo "📦 모든 Docker 컨테이너 정리 중..."
docker stop $(docker ps -aq) 2>/dev/null || true
docker rm $(docker ps -aq) 2>/dev/null || true

# 3. 프로젝트 관련 Docker 이미지 제거
echo "🖼️ 프로젝트 Docker 이미지 제거 중..."
docker rmi $(docker images | grep -E "(log4j|demo)" | awk '{print $3}') 2>/dev/null || true

# 4. 사용하지 않는 Docker 리소스 정리
echo "🗑️ 사용하지 않는 Docker 리소스 정리 중..."
docker system prune -a --volumes -f

# 5. Docker 빌드 캐시 정리
echo "💾 Docker 빌드 캐시 정리 중..."
docker builder prune -a -f

# 6. Gradle 빌드 디렉토리 정리
echo "📁 Gradle 빌드 디렉토리 정리 중..."
rm -rf build/
rm -rf .gradle/
rm -rf logs/

# 7. Gradle wrapper 권한 확인
echo "🔧 Gradle wrapper 권한 설정 중..."
chmod +x gradlew

# 8. Gradle 의존성 다운로드 및 빌드
echo "📦 Gradle 빌드 시작..."
./gradlew clean build -x test --refresh-dependencies

if [ $? -ne 0 ]; then
    echo "❌ Gradle 빌드 실패!"
    exit 1
fi

# 9. 빌드된 JAR 파일 확인
echo "✅ 빌드된 JAR 파일 확인:"
ls -la build/libs/

# 10. 필요한 디렉토리 생성
echo "📂 필요한 디렉토리 생성 중..."
mkdir -p logs/nginx
mkdir -p nginx

# 11. Docker 이미지 빌드
echo "🏗️ Docker 이미지 빌드 중..."
docker-compose build --no-cache

if [ $? -eq 0 ]; then
    echo "✅ 완전 정리 및 재빌드 완료!"
    echo "🚀 이제 'docker-compose up -d'로 애플리케이션을 시작할 수 있습니다."
else
    echo "❌ Docker 이미지 빌드 실패!"
    exit 1
fi