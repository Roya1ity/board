set -euo pipefail

echo ".env 생성"
cat > ~/board/.env << EOF
KAKAO_REST_API=${KAKAO_REST_API}
KAKAO_SECRET=${KAKAO_SECRET}
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
DB_NAME=${DB_NAME}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
EOF

echo "mysql8준비 및 도커 네트워크 생성"
docker network create board-db-net 2>/dev/null || true
docker start mysql8 2>/dev/null || docker run -d --name mysql8 \
  --network board-db-net \
  -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} \
  -e MYSQL_DATABASE=${DB_NAME} \
  -v mysql8-data:/var/lib/mysql \
  mysql:8.4.11 --default-time-zone=+09:00
docker network connect board-db-net mysql8 2>/dev/null || true

echo "DB 응답 대기"
timeout 60 bash -c \
  'until docker exec mysql8 mysqladmin ping -uroot -p"$DB_PASSWORD" --silent 2>/dev/null; do sleep 2; done'
echo "mysql8 ready"

echo "빌드 보장을 위한 메모리 스왑"
SWAP_FILE=/swapfile

if ! sudo swapon --show=NAME --noheadings | awk '{$1=$1};1' | grep -Fxq "$SWAP_FILE"; then
  echo "${SWAP_FILE} 생성 및 활성화"
  sudo -n true

  # 이전 배포에서 불완전하게 생성된 비활성 swapfile도 다시 구성한다.
  sudo rm -f "$SWAP_FILE"
  if ! sudo fallocate -l 2G "$SWAP_FILE"; then
    echo "fallocate 미지원: dd로 swapfile 생성"
    sudo dd if=/dev/zero of="$SWAP_FILE" bs=1M count=2048 status=progress
  fi

  sudo chmod 600 "$SWAP_FILE"
  sudo mkswap "$SWAP_FILE"
  sudo swapon "$SWAP_FILE"

  if ! grep -Fq "$SWAP_FILE none swap sw 0 0" /etc/fstab; then
    echo "$SWAP_FILE none swap sw 0 0" | sudo tee -a /etc/fstab >/dev/null
  fi

  echo "swap 2G 활성화 완료"
else
  echo "${SWAP_FILE}이 이미 활성화되어 있음"
fi
free -h
sudo swapon --show

echo "빌드"
docker compose build app
docker compose build frontend

echo "빌드 후 재기동 + 헬스체크까지"
docker compose up -d --wait

echo "이전 이미지 정리 및 최종 상태보고"
docker image prune -f
docker compose ps
