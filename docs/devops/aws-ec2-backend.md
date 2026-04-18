# Backend on AWS EC2 with Docker Compose + PostgreSQL + Caddy

This document describes how to run the backend on an EC2 instance (Ubuntu) with automatic HTTPS using Caddy.

## 1) Architecture

Services in `ticketing-backend/docker-compose.prod.yml`:

- **backend**: Spring Boot application (Java 17), built from the backend `Dockerfile`.
- **postgres**: PostgreSQL database (official container) with persistent volume.
- **caddy**: reverse proxy (official container) exposing 80/443 and managing TLS certificates automatically.

Network flow:

1. Client calls `https://api.example.com`.
2. **Caddy** receives the request on 443.
3. Caddy performs `reverse_proxy` to the **backend** service at `backend:8080`.
4. **backend** connects to **postgres** through the internal Docker network.

> `backend` and `postgres` are not exposed directly to the Internet.

## 2) Service explanation

### backend
- Uses the existing `Dockerfile` (`build: .`).
- Listens internally on `8080` (`expose`, without `ports`).
- Uses environment variables for datasource and active profile.
- Includes proxy header (`X-Forwarded-*`) support with:
  - `server.forward-headers-strategy: framework` in `application-cloud.yml`.

### postgres
- Official `postgres:16` image.
- Variables:
  - `POSTGRES_DB`
  - `POSTGRES_USER`
  - `POSTGRES_PASSWORD`
- Persistent volume:
  - `ticketing_pgdata:/var/lib/postgresql/data`

### caddy
- Official `caddy:2` image.
- Published ports:
  - `80:80`
  - `443:443`
- Mounts `ticketing-backend/Caddyfile`.
- Automatic certificates (Let's Encrypt) for the domain configured in `DOMAIN`.

## 3) Required environment variables

Minimum base (see `ticketing-backend/.env.example`):

- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `DOMAIN`

Also required for security/JWT in cloud profile:

- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION`

Recommended optional:

- `APP_SECURITY_CORS_ALLOWED_ORIGINS` (CSV list with the frontend deployed on Cloudflare Pages).

## 4) Startup on EC2

### 4.1 Prepare `.env`

```bash
cd /path/to/repo/ticketing-backend
cp .env.example .env
# edit .env with real values (do not commit them)
```

### 4.2 Start services

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

### 4.3 Check status

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

## 5) Health check test

With domain and DNS already pointing to EC2:

```bash
curl https://api.example.com/actuator/health
```

You can also test locally on the instance:

```bash
curl http://localhost/actuator/health
```

## 6) How automatic HTTPS works with Caddy

- Caddy detects the host in `Caddyfile` (value of `{$DOMAIN}`).
- It requests/renews certificates automatically.
- It stores state and certificates in Docker volumes (`caddy_data`, `caddy_config`).
- It redirects and serves HTTPS traffic without manual certbot/nginx setup.

Requirements for this to work:

- Domain DNS pointing to the EC2 public IP.
- Ports 80 and 443 open in Security Group/NACL/firewall.

## 7) What is missing for automation (next batch)

This batch leaves the environment ready for manual operation. In the next batch, what remains is:

- CI/CD workflow for backend image build + push.
- CD workflow for automatic deployment on EC2.
- Automated secrets management and rotation.
- Rollback strategy and post-deploy health checks.
