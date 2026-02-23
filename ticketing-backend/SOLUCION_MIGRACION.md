# Solución al Error de Migración de Base de Datos

## Problema Resuelto
El error `ERROR: column "password_hash" of relation "users" contains null values` ocurría porque:

1. **Hibernate estaba configurado con `ddl-auto: update`** - Intentaba agregar la columna directamente con `NOT NULL`
2. **Flyway no estaba configurado** - No había gestor de migraciones instalado
3. **Faltaba la migración inicial** - Solo existía V1 para agregar password_hash, pero no la creación del esquema

## Cambios Realizados

### 1. Agregado Flyway al `pom.xml`
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### 2. Modificado `application-local.yml`
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Cambiado de "update" a "validate"
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### 3. Creadas las Migraciones SQL

#### `V1__create_initial_schema.sql`
- Crea todas las tablas: `users`, `categories`, `tickets`, `ticket_comments`
- **NO incluye la columna `password_hash` todavía**
- Crea índices para mejorar el rendimiento
- Inserta datos iniciales de usuarios y categorías

#### `V2__add_password_hash_to_users.sql` (renombrado de V1)
- Agrega la columna `password_hash` como nullable
- Actualiza todos los registros existentes con hashes BCrypt
- Establece la columna como `NOT NULL`

## Instrucciones para Ejecutar

### Opción 1: Base de Datos Nueva (Recomendado)

```powershell
# 1. Limpiar la base de datos existente
cd C:\Users\alvar\Desktop\protube\tfg-ticketing-devops\ticketing-backend
docker compose down -v

# 2. Iniciar PostgreSQL
docker compose up -d

# 3. Esperar a que PostgreSQL esté listo
Start-Sleep -Seconds 10

# 4. Compilar el proyecto
.\mvnw.cmd clean install -DskipTests

# 5. Ejecutar la aplicación
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

### Opción 2: Base de Datos Existente

Si ya tienes datos en la base de datos:

```sql
-- Conectarse a PostgreSQL y ejecutar:
-- 1. Eliminar la tabla flyway_schema_history si existe
DROP TABLE IF EXISTS flyway_schema_history;

-- 2. Ejecutar manualmente V2__add_password_hash_to_users.sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

UPDATE users
SET password_hash = CASE
    WHEN email = 'user@local.test' THEN '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    WHEN email = 'agent@local.test' THEN '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    ELSE '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
END
WHERE password_hash IS NULL OR BTRIM(password_hash) = '';

ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
```

Luego en `application-local.yml`, configura:
```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 2
```

## Verificación

La aplicación debería iniciar sin errores y verás en los logs:

```
Flyway Community Edition ... by Redgate
Database: jdbc:postgresql://localhost:5432/ticketing (PostgreSQL 16)
Successfully validated 2 migrations
Current version of schema "public": 2
Schema "public" is up to date. No migration necessary.
```

## Usuarios de Prueba

Después de la migración, estos usuarios estarán disponibles:

- **Usuario Normal**: `user@local.test` / password: `password`
- **Agente**: `agent@local.test` / password: `password`

(Hash BCrypt: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`)

## Notas Importantes

1. **Nunca usar `ddl-auto: update` en producción** - Usar siempre Flyway o Liquibase
2. **Las migraciones deben ser incrementales** - Nunca modificar migraciones ya aplicadas
3. **Siempre probar en local primero** - Antes de aplicar en otros entornos

