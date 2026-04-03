# Backend cloud-ready packaging (Lote 4 DevOps/CI-CD)

## 1. Objetivo
Dejar `ticketing-backend` preparado para el siguiente lote de despliegue con una base cloud-ready sin desplegar todavía:
- empaquetado en imagen Docker,
- configuración runtime por variables de entorno,
- compatibilidad con PaaS (Render o similares),
- CORS configurable por entorno,
- sin secretos hardcodeados para ejecución cloud.

## 2. Estrategia de empaquetado Docker
Se utiliza un **Dockerfile multi-stage**:

1. **builder** (`maven:3.9.11-eclipse-temurin-17`)
   - resuelve dependencias con Maven Wrapper,
   - compila y empaqueta el `.jar` de Spring Boot.
2. **runtime** (`eclipse-temurin:17-jre-jammy`)
   - copia solo el artefacto final,
   - ejecuta como usuario no root (`spring`),
   - deja perfil `cloud` por defecto en contenedor.

Esto reduce tamaño de runtime y separa claramente build vs ejecución.

## 3. Archivos añadidos para empaquetado
- `ticketing-backend/Dockerfile`
- `ticketing-backend/.dockerignore`

`.dockerignore` excluye artefactos locales (`target`, `.git`, IDEs, logs, reportes) para mejorar tiempos de build y evitar ruido en el contexto Docker.

## 4. Perfiles y configuración runtime
Se organiza la configuración de Spring por perfiles:

- `application.yml` (base compartida)
  - `server.port` parametrizado con `PORT` (fallback `8080`),
  - CORS configurable por env var,
  - fallback de claves JWT de desarrollo (`classpath`) para compatibilidad local/tests sin perfil explícito.

- `application-local.yml` (perfil `local`)
  - datasource local (PostgreSQL local),
  - claves JWT desde `classpath` para desarrollo local,
  - pensado para desarrollo en máquina local.

- `application-cloud.yml` (perfil `cloud`)
  - datasource, public key y private key por variables de entorno,
  - sin rutas sensibles hardcodeadas de runtime cloud.

## 5. Variables de entorno clave

### Perfil y puerto
- `SPRING_PROFILES_ACTIVE=cloud` (en contenedor ya se define por defecto)
- `PORT` (inyectado por proveedor cloud; Spring lo usa como `server.port`)

### Base de datos (cloud)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Seguridad JWT (cloud)
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION`
  - En cloud debe apuntar a secreto externo. Ejemplo: `file:/etc/secrets/jwt-public.pem`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION`
  - En cloud debe apuntar a secreto externo. Ejemplo: `file:/etc/secrets/jwt-private.pem`
- `APP_SECURITY_JWT_EXPIRATION_SECONDS` (opcional, por defecto `3600`)

### CORS por entorno
- `APP_SECURITY_CORS_ALLOWED_ORIGINS`
  - Formato CSV (separado por comas)
  - Ejemplo: `https://staging-frontend.example.com,https://www.example.com`

> Nota: evitar `*` en producción salvo caso controlado y justificado.

## 6. Ejemplos de uso

### Build local de imagen
```bash
cd ticketing-backend
docker build -t ticketing-backend:cloud-ready .
```

### Ejecución local del contenedor simulando cloud
> Importante: si usas rutas `file:/run/secrets/...`, debes montar esos ficheros dentro del contenedor.

#### Linux/macOS (bash/zsh) desde `ticketing-backend/`
```bash
docker run --rm -p 8080:8080 \
  -v "$(pwd)/src/main/resources/keys:/run/secrets:ro" \
  -e SPRING_PROFILES_ACTIVE=cloud \
  -e PORT=8080 \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/ticketing' \
  -e SPRING_DATASOURCE_USERNAME='user' \
  -e SPRING_DATASOURCE_PASSWORD='password' \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION='file:/run/secrets/jwt-public.pem' \
  -e APP_SECURITY_JWT_PRIVATE_KEY_LOCATION='file:/run/secrets/jwt-private.pem' \
  -e APP_SECURITY_CORS_ALLOWED_ORIGINS='http://localhost:5173' \
  ticketing-backend:cloud-ready
```

#### PowerShell (Windows) desde `ticketing-backend/`
```powershell
docker run --rm -p 8080:8080 `
  -v "${PWD}/src/main/resources/keys:/run/secrets:ro" `
  -e SPRING_PROFILES_ACTIVE=cloud `
  -e PORT=8080 `
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/ticketing' `
  -e SPRING_DATASOURCE_USERNAME='user' `
  -e SPRING_DATASOURCE_PASSWORD='password' `
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION='file:/run/secrets/jwt-public.pem' `
  -e APP_SECURITY_JWT_PRIVATE_KEY_LOCATION='file:/run/secrets/jwt-private.pem' `
  -e APP_SECURITY_CORS_ALLOWED_ORIGINS='http://localhost:5173' `
  ticketing-backend:cloud-ready
```

#### CMD (Windows) desde la raíz del repo
Si ejecutas desde `C:\...\tfg-ticketing-devops>` en **CMD** (no PowerShell), usa `%cd%` y la ruta del monorepo.

> En CMD no uses comillas simples en variables (`'valor'`), porque se envían como parte del valor y rompen JDBC (por ejemplo en `SPRING_DATASOURCE_URL`).
```cmd
docker run --rm -p 8080:8080 ^
  -v "%cd%\ticketing-backend\src\main\resources\keys:/run/secrets:ro" ^
  -e SPRING_PROFILES_ACTIVE=cloud ^
  -e PORT=8080 ^
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/ticketing ^
  -e SPRING_DATASOURCE_USERNAME=user ^
  -e SPRING_DATASOURCE_PASSWORD=password ^
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION=file:/run/secrets/jwt-public.pem ^
  -e APP_SECURITY_JWT_PRIVATE_KEY_LOCATION=file:/run/secrets/jwt-private.pem ^
  -e APP_SECURITY_CORS_ALLOWED_ORIGINS=http://localhost:5173 ^
  ticketing-backend:cloud-ready
```

## 7. Ajuste menor en docker-compose local
Se corrige el `healthcheck` de PostgreSQL en `ticketing-backend/docker-compose.yml` para usar el usuario realmente configurado (`user`) y evitar falsos negativos en local.

## 8. Checklist previo al lote de despliegue (staging)
1. Confirmar que la plataforma cloud inyecta `PORT` y variables de entorno requeridas.
2. Cargar claves JWT en gestor de secretos de la plataforma (no en Git).
3. Definir `APP_SECURITY_CORS_ALLOWED_ORIGINS` con dominio real de frontend staging.
4. Verificar conectividad DB y migraciones Flyway en el entorno staging.
5. Validar `/actuator/health` tras arrancar en entorno cloud.

Con esto, el backend queda preparado para el siguiente lote de despliegue sin introducir todavía workflows de CD.
