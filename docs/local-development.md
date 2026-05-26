# Guía de ejecución local

## Objetivo

Este documento describe cómo preparar un entorno local para ejecutar la plataforma de ticketing completa: backend Spring Boot, frontend React/Vite y base de datos PostgreSQL en Docker.

## Requisitos previos

Instalar las siguientes herramientas:

| Herramienta | Uso en el proyecto | Versión recomendada |
|---|---|---|
| Git | Clonado y control de versiones | Última estable |
| Docker Desktop | PostgreSQL local y servicios contenerizados | Con Docker Compose v2 |
| JDK | Compilación y ejecución del backend | 17 LTS |
| Node.js | Toolchain frontend | 22 LTS |
| npm | Dependencias y scripts frontend | Incluido con Node.js |

El frontend usa Vite 7, por lo que Node debe ser compatible con `^20.19.0 || >=22.12.0`. Para simplificar la instalación, se recomienda Node 22 LTS.

Maven no es obligatorio como instalación global porque el backend incluye Maven Wrapper.

## Preparación inicial

```bash
git clone https://github.com/AlvaroPerdigonG/tfg-ticketing-devops.git
cd tfg-ticketing-devops
```

## Base de datos local

El backend utiliza PostgreSQL mediante Docker Compose. La definición está en:

```text
ticketing-backend/docker-compose.yml
```

Servicio local:

```bash
cd ticketing-backend
docker compose up -d
```

Configuración:

| Parámetro | Valor |
|---|---|
| Imagen | `postgres:16-alpine` |
| Contenedor | `ticketing-postgres` |
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `ticketing` |
| Usuario | `user` |
| Contraseña | `password` |
| Volumen | `ticketing_pgdata` |

Comandos útiles:

```bash
docker compose ps
docker compose logs -f postgres
docker compose down
docker compose down -v
```

`docker compose down` detiene los servicios manteniendo los datos. `docker compose down -v` elimina también el volumen y deja la base de datos limpia.

## Backend

### Configuración

El backend está en `ticketing-backend` y utiliza Spring Boot con perfiles.

Archivos relevantes:

| Archivo | Función |
|---|---|
| `src/main/resources/application.yml` | Configuración común, puerto, CORS y JWT |
| `src/main/resources/application-local.yml` | Configuración local PostgreSQL/Flyway |
| `src/main/resources/application-cloud.yml` | Configuración para despliegue cloud |
| `src/main/resources/db/migration/` | Migraciones Flyway |
| `src/main/resources/keys/` | Claves JWT locales |

Para desarrollo local debe activarse el perfil `local`.

### Ejecución

Windows PowerShell:

```powershell
cd ticketing-backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Linux/macOS/Git Bash:

```bash
cd ticketing-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Script auxiliar en Windows:

```powershell
cd ticketing-backend
.\run.ps1
```

El script:

1. Detiene contenedores existentes.
2. Levanta PostgreSQL.
3. Espera a que el contenedor esté disponible.
4. Compila el backend.
5. Arranca Spring Boot con `SPRING_PROFILES_ACTIVE=local`.

Para reiniciar completamente los datos:

```powershell
.\run.ps1 -ResetDatabase
```

### Endpoints locales útiles

| Recurso | URL |
|---|---|
| API backend | `http://localhost:8080` |
| Health check | `http://localhost:8080/actuator/health` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

### Datos semilla locales

Con el perfil `local`, `LocalSeedRunner` crea registros mínimos si no existen:

| Rol | Email | Contraseña |
|---|---|---|
| Usuario | `user@local.test` | `password` |
| Agente | `agent@local.test` | `password` |

También se crea una categoría `General`.

## Frontend

### Configuración

El frontend está en `ticketing-frontend`.

Crear el archivo:

```text
ticketing-frontend/.env.local
```

Contenido:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Esta variable indica al cliente frontend dónde está la API del backend. Si no se define, la aplicación muestra un aviso en consola y las llamadas a `/api/...` se hacen contra el mismo origen del frontend, lo que no es adecuado para el desarrollo local sin proxy.

### Ejecución

```bash
cd ticketing-frontend
npm install
npm run dev
```

La aplicación queda disponible en:

```text
http://localhost:5173
```

## Configuración de IDE

### IntelliJ IDEA para backend

Recomendación:

1. Abrir `ticketing-backend` como proyecto Maven.
2. Seleccionar JDK 17 como Project SDK.
3. Importar dependencias Maven desde `pom.xml`.
4. Activar annotation processing si Lombok lo requiere.
5. Crear una configuración Spring Boot con la clase `TicketingBackendApplication`.
6. Definir perfil activo `local`.
7. Verificar que Docker Desktop está arrancado y PostgreSQL se ha levantado con `docker compose up -d`.

Configuración equivalente:

```text
Main class: com.aperdigon.ticketing_backend.TicketingBackendApplication
Active profiles: local
Working directory: ticketing-backend
```

### Visual Studio Code para frontend

Recomendación:

1. Abrir `ticketing-frontend`.
2. Ejecutar `npm install`.
3. Crear `.env.local`.
4. Ejecutar `npm run dev` desde el terminal integrado.
5. Usar las extensiones ESLint y Prettier para recibir el mismo feedback que en CI.

## Pruebas locales

### Backend

Tests unitarios y fitness tests de arquitectura:

```bash
cd ticketing-backend
./mvnw test
```

Suite completa con integración y cobertura:

```bash
cd ticketing-backend
./mvnw verify -DskipITs=false
```

Las pruebas de integración usan Testcontainers con PostgreSQL, por lo que Docker debe estar disponible.

### Frontend

```bash
cd ticketing-frontend
npm run format:check
npm run lint
npm run test:run
npm run build
```

### E2E con Playwright

Los E2E no forman parte del workflow principal de CI. Se ejecutan manualmente.

Preparación recomendada:

```bash
cd ticketing-frontend
npm run build
npm run preview
```

Ejecución:

```bash
npm run test:e2e
```

Si Playwright necesita instalar navegadores:

```bash
npx playwright install
```

## Documentacion del repositorio

La documentacion viva del proyecto esta centralizada en `docs/` y se organiza por responsabilidad:

| Archivo o directorio | Contenido |
|---|---|
| `README.md` | Resumen en ingles del proyecto, tecnologias, ejecucion local, estructura del repositorio, pruebas, documentacion y CI/CD. |
| `docs/local-development.md` | Guia principal para preparar el entorno local, configurar IDEs, levantar PostgreSQL, ejecutar backend/frontend y lanzar pruebas. |
| `docs/tickets-endpoints-proposal.md` | Referencia actual de endpoints HTTP del backend: autenticacion, tickets, categorias, administracion, health checks y paginacion. |
| `docs/auth-review.md` | Revision del flujo de autenticacion/autorizacion entre frontend y backend: JWT, roles, rutas protegidas y guardas de UI. |
| `docs/features/*.feature` | Especificacion funcional canonica en Gherkin. No se ejecuta con Cucumber; sirve como ancla de requisitos y trazabilidad. |
| `docs/testing/testing-strategy.md` | Estrategia general de pruebas, piramide de testing, criterios de cobertura y reglas de mantenimiento. |
| `docs/testing/testing-types-deep-dive.md` | Explicacion detallada de cada tipo de prueba: unitarias backend, integracion backend, UI/componentes frontend y E2E. |
| `docs/testing/traceability-matrix.md` | Matriz de trazabilidad que relaciona escenarios funcionales con evidencias automatizadas y estado de cobertura. |
| `docs/devops/cicd-strategy.md` | Estrategia CI/CD del monorepo y responsabilidades de GitHub Actions, SonarCloud y controles de calidad. |
| `docs/devops/branch-policy.md` | Politica de ramas, pull requests, proteccion de `main` y estrategia de merge. |
| `docs/devops/backend-cloud-packaging.md` | Modelo de empaquetado cloud del backend: JAR precompilado, Dockerfile runtime-only y variables de entorno. |
| `docs/devops/aws-ec2-backend.md` | Arquitectura manual de despliegue del backend en AWS EC2 con Docker Compose, PostgreSQL y Caddy. |
| `docs/devops/deploy-backend-production.md` | Funcionamiento del workflow automatizado de despliegue backend a produccion mediante GitHub Actions y SSH. |
| `ticketing-backend/SEED_Y_PERSISTENCIA.md` | Nota tecnica sobre datos semilla locales, persistencia del volumen PostgreSQL y uso de `run.ps1`. |
| `ticketing-backend/SOLUCION_MIGRACION.md` | Nota tecnica sobre el problema de migracion resuelto al adoptar Flyway como fuente de verdad del esquema. |
| `ticketing-backend/HELP.md` | Documento generado por Spring Initializr con referencias generales del modulo backend. Tiene valor auxiliar, no funcional. |
| `ticketing-frontend/e2e/PLAYWRIGHT-E2E-EXECUTION.md` | Guia operativa para ejecutar manualmente la suite E2E con Playwright. |

## Resolución de problemas frecuentes

### El backend no conecta con PostgreSQL

Comprobar:

```bash
cd ticketing-backend
docker compose ps
docker compose logs postgres
```

Verificar que el perfil activo sea `local`. Sin ese perfil, el backend no usa la configuración local de `application-local.yml`.

### El frontend no llega al backend

Comprobar que existe:

```text
ticketing-frontend/.env.local
```

y que contiene:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Reiniciar `npm run dev` después de cambiar variables de entorno de Vite.

### Los usuarios locales no funcionan

Si la base de datos viene de una versión anterior o tiene datos inconsistentes, reiniciar el volumen:

```bash
cd ticketing-backend
docker compose down -v
docker compose up -d
```

Después volver a arrancar el backend con perfil `local`.

### Fallan las pruebas de integración

Comprobar que Docker Desktop está arrancado. Testcontainers necesita poder crear contenedores PostgreSQL durante la ejecución de `mvn verify`.
