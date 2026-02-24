# Seed local y persistencia de datos

## Objetivo
Tener datos base al arrancar en local (usuario, agente y categoría), pero que **los datos persistan** entre reinicios.

## Cómo funciona ahora
- `LocalSeedRunner` solo se ejecuta en perfil `local`.
- Inserta datos base **solo si no existen**:
  - categoría `General`
  - usuario `user@local.test`
  - agente `agent@local.test`
- La comprobación de usuarios se hace por email (`findByEmailIgnoreCase`) para no chocar con el índice único de email.

## Persistencia entre reinicios
`docker-compose.yml` ya monta un volumen nombrado (`ticketing_pgdata`) para PostgreSQL.
Si no eliminas ese volumen, los datos permanecen.

## Script `run.ps1`
El script ahora conserva la base de datos por defecto.

- Arranque normal (conserva datos):
  ```powershell
  .\run.ps1
  ```

- Arranque con limpieza total de base de datos:
  ```powershell
  .\run.ps1 -ResetDatabase
  ```

## Insertar usuarios manualmente y poder hacer login
Para que un usuario creado manualmente pueda iniciar sesión, su `password_hash` debe ser BCrypt.

Ejemplo SQL (password en claro: `secret123`):

```sql
INSERT INTO users (id, email, display_name, role, is_active, password_hash)
VALUES (
  gen_random_uuid(),
  'nuevo@local.test',
  'Nuevo Usuario',
  'USER',
  true,
  '$2a$10$8E7Mzy8M2Q0dQkN4kD7a3eN9WQfN3st9sSk6cZy4xkS9t2C5K8E4i'
);
```

> Nota: el hash de ejemplo es ilustrativo. Genera un hash BCrypt real para la contraseña que quieras usar.
