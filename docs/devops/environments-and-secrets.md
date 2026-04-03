# Entornos y gestión de secretos (GitHub)

## 1. Objetivo del documento
Definir una estrategia operativa, simple y segura para gestionar **entornos** y **secrets** en la fase DevOps del TFG, minimizando errores de configuración y preparando la futura automatización de CI/CD.

## 2. Entornos propuestos

## `staging`
Entorno de validación continua:
- destino inicial de despliegues automáticos desde `main`,
- usado para comprobaciones funcionales básicas post-deploy,
- permite detectar problemas de integración antes de producción.

## `production`
Entorno final de servicio:
- reservado para una fase posterior,
- requiere mayor control (aprobaciones, ventanas de despliegue y smoke checks más estrictos).

## 3. Papel de cada entorno
- **staging**: validar que la versión integrada funciona fuera de local y recoger incidencias tempranas.
- **production**: servir versión estable con controles de riesgo superiores.

Separar ambos entornos evita mezclar secretos, URLs y políticas de despliegue.

## 4. Secretos a nivel repositorio vs nivel environment

## Secretos de repositorio (Repository secrets)
Usar este nivel para secretos realmente globales al repositorio y no dependientes del entorno.

Recomendado inicialmente:
- `SONAR_TOKEN` (si el mismo token aplica a todo el repositorio y no cambia por entorno).

## Secretos de entorno (Environment secrets)
Usar `staging` y `production` para todo valor que cambie según destino.

Recomendado:
- `BACKEND_DEPLOY_HOOK_URL`
- `FRONTEND_DEPLOY_HOOK_URL`
- `BACKEND_BASE_URL`
- `FRONTEND_BASE_URL`

## 5. Convención de nombres
Mantener **la misma clave en ambos entornos** y cambiar solo el valor por environment.

Ejemplo correcto:
- `staging.BACKEND_BASE_URL = https://staging-api...`
- `production.BACKEND_BASE_URL = https://api...`

Evitar sufijos como `_STAGING` / `_PROD` porque incrementan complejidad, duplican variables y dificultan el mantenimiento de workflows.

## 6. Lista inicial sugerida de secrets

### Base (arranque CI/CD)
- `SONAR_TOKEN`
- `BACKEND_DEPLOY_HOOK_URL`
- `FRONTEND_DEPLOY_HOOK_URL`
- `BACKEND_BASE_URL`
- `FRONTEND_BASE_URL`

### Opcionales futuros para E2E
Cuando se automaticen pruebas E2E contra entornos desplegados, se pueden añadir credenciales de usuarios de prueba por rol:
- `E2E_USER_EMAIL` / `E2E_USER_PASSWORD`
- `E2E_AGENT_EMAIL` / `E2E_AGENT_PASSWORD`
- `E2E_ADMIN_EMAIL` / `E2E_ADMIN_PASSWORD`

(Usar cuentas no personales, con permisos mínimos y datos de prueba.)

## 7. Buenas prácticas operativas
1. **No commitear secretos** en código, `.env` versionados o documentación pública.
2. **No duplicar valores sin necesidad**: si un valor es igual para ambos entornos, justificar su nivel (repo o environment).
3. **No almacenar credenciales de base de datos en GitHub** si ya están gestionadas por la plataforma de despliegue.
4. **Usar GitHub Environments** para separar claramente `staging` y `production` (secretos, protección y auditoría).
5. **Aplicar mínimo privilegio** a tokens y cuentas técnicas.
6. **Rotar secretos** cuando haya sospecha de exposición o cambios de equipo.

## 8. Checklist manual posterior
Antes de activar workflows de despliegue, verificar:

- [ ] Existe environment `staging`.
- [ ] Existe environment `production`.
- [ ] `SONAR_TOKEN` está configurado en el nivel acordado.
- [ ] En `staging` están definidos:
  - [ ] `BACKEND_DEPLOY_HOOK_URL`
  - [ ] `FRONTEND_DEPLOY_HOOK_URL`
  - [ ] `BACKEND_BASE_URL`
  - [ ] `FRONTEND_BASE_URL`
- [ ] En `production` están definidos los mismos nombres de secrets con valores de producción.
- [ ] No hay secretos sensibles hardcodeados en el repositorio.
- [ ] Se ha validado quién puede aprobar despliegues a `production`.

Este checklist permite arrancar la siguiente fase (workflows CI/CD y deploy) con una base consistente y sin bloqueos de configuración.
