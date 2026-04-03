# Política de ramas (Branch Policy) — TFG Ticketing

## 1. Objetivo
Definir una política de ramas simple, estricta y operativa para proteger la calidad del monorepo (`ticketing-backend` + `ticketing-frontend`) y asegurar trazabilidad de cambios durante el TFG.

## 2. Alcance
Esta política aplica a todo el repositorio y a cualquier contribución que afecte backend, frontend, documentación o configuración DevOps.

## 3. Ramas oficiales

### `main`
- Rama estable y protegida.
- No se permiten pushes directos.
- Solo se aceptan cambios mediante Pull Request (PR).

### Ramas de trabajo
- `feature/<descripcion-corta>` para nuevas funcionalidades.
- `fix/<descripcion-corta>` para correcciones.
- `chore/<descripcion-corta>` para tareas técnicas/mantenimiento.
- `docs/<descripcion-corta>` para documentación.

Ejemplos:
- `feature/auth-refresh-token`
- `fix/tickets-filter-pagination`
- `chore/devops-sonar-baseline`

## 4. Reglas obligatorias para `main`
Estas reglas deben configurarse en GitHub (Rulesets o Branch Protection):

1. **Require pull request before merging**: habilitado.
2. **Required approvals**: mínimo 1.
3. **Dismiss stale approvals** cuando haya nuevos commits en la PR.
4. **Require conversation resolution** antes del merge.
5. **Require status checks to pass** (cuando existan workflows CI).
6. **Restrict direct pushes** a `main`.
7. **Block force pushes**.
8. **Block branch deletion**.

## 5. Flujo de trabajo obligatorio
1. Crear rama desde `main` actualizada.
2. Implementar cambios en la rama de trabajo.
3. Ejecutar validaciones locales mínimas.
4. Subir rama y abrir PR hacia `main`.
5. Esperar checks automáticos y revisión.
6. Corregir feedback si aplica.
7. Hacer merge cuando todo esté en verde.

## 6. Requisitos mínimos de Pull Request
Cada PR debe incluir:
- propósito del cambio,
- alcance técnico,
- evidencias de validación (tests/comandos),
- impacto esperado,
- riesgos conocidos (si los hubiera).

## 7. Estrategia de merge recomendada
- Recomendado: **Squash merge** para mantener historial limpio y trazable por PR.
- Activar borrado automático de la rama tras merge.

## 8. Gestión de hotfix
Si se requiere un hotfix urgente:
1. Crear `fix/<descripcion>` desde `main`.
2. Aplicar corrección mínima.
3. Abrir PR con prioridad alta.
4. Mantener igualmente revisión + checks obligatorios (salvo incidente crítico excepcional justificado).

## 9. Excepciones
Cualquier bypass de esta política debe quedar justificado en el hilo de la PR y documentado en el changelog técnico del proyecto.

## 10. Checklist de implantación en GitHub
- [ ] `main` protegida.
- [ ] Push directo bloqueado.
- [ ] PR obligatoria.
- [ ] 1 aprobación mínima.
- [ ] Resolución de conversaciones obligatoria.
- [ ] Force-push deshabilitado.
- [ ] Borrado de `main` deshabilitado.
- [ ] (Cuando aplique) checks CI marcados como requeridos.
