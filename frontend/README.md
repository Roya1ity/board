# Board React Frontend

## 로컬 개발

백엔드를 `localhost:8099`에서 실행한 후 다음 명령을 사용한다.

```bash
npm install
npm run dev
```

Vite 개발 서버가 `/api`, `/images`, OAuth 요청을 백엔드로 프록시한다.

## 운영 빌드

```bash
npm install
npm run build
```

`dist/`를 Nginx에서 제공하거나 포함된 `Dockerfile`을 빌드한다. `nginx.conf`에서 백엔드 upstream 이름은 `backend:8099`로 가정한다. 실제 배포 환경에 맞게 변경한다.
