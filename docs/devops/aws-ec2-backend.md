# Backend en AWS EC2 con Docker Compose + PostgreSQL + Caddy

Este documento describe cómo ejecutar el backend en una instancia EC2 (Ubuntu) con HTTPS automático usando Caddy.

## 1) Arquitectura

Servicios en `ticketing-backend/docker-compose.prod.yml`:

- **backend**: aplicación Spring Boot (Java 17), construida con el `Dockerfile` del backend.
- **postgres**: base de datos PostgreSQL (contenedor oficial) con volumen persistente.
- **caddy**: reverse proxy (contenedor oficial) que publica 80/443 y gestiona certificados TLS automáticamente.

Flujo de red:

1. Cliente llama a `https://api.example.com`.
2. **Caddy** recibe la petición en 443.
3. Caddy hace `reverse_proxy` al servicio **backend** en `backend:8080`.
4. **backend** conecta con **postgres** por red interna Docker.

> `backend` y `postgres` no se exponen directamente a Internet.

## 2) Explicación de cada servicio

### backend
- Usa el `Dockerfile` existente (`build: .`).
- Escucha internamente en `8080` (`expose`, sin `ports`).
- Usa variables de entorno para datasource y perfil activo.
- Incluye soporte de headers de proxy (`X-Forwarded-*`) con:
  - `server.forward-headers-strategy: framework` en `application-cloud.yml`.

### postgres
- Imagen oficial `postgres:16`.
- Variables:
  - `POSTGRES_DB`
  - `POSTGRES_USER`
  - `POSTGRES_PASSWORD`
- Volumen persistente:
  - `ticketing_pgdata:/var/lib/postgresql/data`

### caddy
- Imagen oficial `caddy:2`.
- Puertos publicados:
  - `80:80`
  - `443:443`
- Monta `ticketing-backend/Caddyfile`.
- Certificados automáticos (Let's Encrypt) para el dominio configurado en `DOMAIN`.

## 3) Variables de entorno necesarias

Base mínima (ver `ticketing-backend/.env.example`):

- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `DOMAIN`

También necesarias para seguridad/JWT en perfil cloud:

- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION`

Opcional recomendado:

- `APP_SECURITY_CORS_ALLOWED_ORIGINS` (lista CSV con el frontend desplegado en Cloudflare Pages).

## 4) Puesta en marcha en EC2

### 4.1 Preparar `.env`

```bash
cd /ruta/al/repo/ticketing-backend
cp .env.example .env
# editar .env con valores reales (sin commitearlos)
```

### 4.2 Levantar servicios

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

### 4.3 Ver estado

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

## 5) Prueba de salud

Con dominio y DNS ya apuntando a la EC2:

```bash
curl https://api.example.com/actuator/health
```

También puedes probar localmente en la instancia:

```bash
curl http://localhost/actuator/health
```

## 6) Cómo funciona el HTTPS automático con Caddy

- Caddy detecta el host del `Caddyfile` (valor de `{$DOMAIN}`).
- Solicita/renueva certificados automáticamente.
- Guarda estado y certificados en volúmenes Docker (`caddy_data`, `caddy_config`).
- Redirige y sirve tráfico HTTPS sin configuración manual de certbot/nginx.

Requisitos para que funcione:

- DNS del dominio apuntando a la IP pública de EC2.
- Puertos 80 y 443 abiertos en Security Group/NACL/firewall.

## 7) Qué falta para automatizar (siguiente lote)

Este lote deja el entorno listo para operación manual. En el siguiente lote quedaría:

- Workflow CI/CD para build + push de imagen backend.
- Workflow CD para despliegue automático en EC2.
- Gestión automatizada de secretos y rotación.
- Estrategia de rollback y health checks post-deploy.
