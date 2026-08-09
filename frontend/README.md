# Board Nginx Frontend

This image serves the same browser UI as `src/main/resources/static`.

The following files must remain byte-for-byte synchronized with the Spring Boot static resources:

- `index.html`
- `styles.css`
- `app.js`

Build and run it through the repository-level Compose configuration:

```bash
docker compose up -d --build frontend2
```

Nginx proxies `/api`, `/images`, `/oauth2`, and `/login/oauth2` requests to the backend container.
