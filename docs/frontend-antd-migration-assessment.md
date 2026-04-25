# Evaluación: uso de `antd` real vs shim local y plan de migración

## Conclusión ejecutiva

Sí existe un **shim local de `antd`** en el repositorio, pero **no parece ser el que se usa en runtime (build de Vite)**.

Estado observado:

1. `antd` está declarado como dependencia real del frontend.
2. En TypeScript hay un `paths` que redirige `antd` a `src/vendor/antd`.
3. En Vite no existe un alias equivalente para `antd`.
4. El bundle de producción incluye código interno de `antd` real.

Interpretación práctica:

- **Type-checking/editor**: está apuntando al shim local.
- **Ejecución (dev/build/test sobre Vite)**: está resolviendo el paquete real de `node_modules/antd`.

## Evidencias en código

### 1) El frontend declara `antd` real

En `ticketing-frontend/package.json` aparece `antd` en `dependencies`.

### 2) Existe shim local que imita componentes

`ticketing-frontend/src/vendor/antd/index.tsx` exporta componentes como `Button`, `Table`, `Modal`, `Tabs`, `message`, etc., implementados con React base e inline styles.

Además hay archivos locales de locale/reset dentro de `src/vendor/antd`.

### 3) TypeScript redirige `antd` al shim

En `ticketing-frontend/tsconfig.app.json`:

- `"antd": ["src/vendor/antd/index.tsx"]`
- `"antd/*": ["src/vendor/antd/*"]`

Esto hace que TS resuelva imports de `antd` al shim durante chequeo de tipos.

### 4) Vite no redirige `antd` al shim

En `ticketing-frontend/vite.config.ts` solo hay alias para `src`; no existe alias para `antd`.

Con esa configuración, en runtime Vite resuelve el paquete instalado en `node_modules`.

## Viabilidad y riesgo del cambio

Dado el estado actual, la migración a “antd real” es **muy viable** porque probablemente ya se está usando en runtime. El trabajo principal sería **alinear TypeScript/tests con runtime** y eliminar deuda técnica.

### Impacto esperado en aspecto visual

- **Si hoy runtime ya usa antd real**: el aspecto visual no debería cambiar significativamente tras quitar el shim de paths.
- Puede haber **ajustes menores** si en algún lugar se estaba confiando en comportamientos simplificados del shim (sobre todo en tests).

### Impacto esperado en tests

Riesgo **medio** de fallos en unit tests por:

- Selectores/accesibilidad distintos entre shim y componentes reales (`role`, estructura del DOM, textos auxiliares, wrappers de Ant).
- Componentes controlados de Ant (`Select`, `Table`, `Modal`, `Tabs`, `Form`) tienen markup/eventos más complejos.

No se espera un “rompimiento masivo” si la app ya corre con Ant real, pero sí es razonable esperar algunos tests frágiles.

## Plan paso a paso para migrar correctamente a antd real

### Fase 0 — Línea base y seguridad

1. Ejecutar baseline de calidad:
   - `npm run build`
   - `npm run test:run`
   - (si aplica) `npm run test:e2e`
2. Guardar snapshot visual de pantallas clave (login, dashboard, tablas de tickets, admin).
3. Crear rama de migración para aislar cambios.

### Fase 1 — Confirmar/normalizar resolución de módulos

4. Eliminar del `tsconfig.app.json` los `paths` de `antd` y `antd/*`.
5. Verificar que los imports siguen siendo `from "antd"` y `from "antd/..."` en la app.
6. Mantener `import "antd/dist/reset.css"` desde `main.tsx`.

### Fase 2 — Alinear tipado y APIs reales

7. Correr TypeScript (`npm run build` o `npx tsc -b`) y corregir incompatibilidades de tipos.
8. Revisar especialmente usos de:
   - `TableProps`
   - `Form`/`Form.Item`
   - `Modal`, `Tabs`, `Select`, `message`
9. Añadir tipos explícitos donde antes el shim “permitía todo”.

### Fase 3 — Tests unitarios/integración

10. Ejecutar `npm run test:run`.
11. Arreglar tests por semántica/markup de Ant real:
    - Preferir queries por rol accesible estable y texto funcional.
    - Evitar asserts ultra acoplados a estructura interna.
12. Si hay fragilidad repetida, encapsular helpers de test para componentes Ant.

### Fase 4 — Validación visual y UX

13. Ejecutar la app (`npm run dev`) y revisar páginas principales.
14. Validar:
    - spacing/layout
    - estados loading/empty/error
    - formularios, modales, tablas y tabs
15. Ajustar tokens/tema en `ConfigProvider` para mantener identidad visual.

### Fase 5 — Limpieza

16. Eliminar `src/vendor/antd/index.tsx`, `src/vendor/antd/locale/es_ES.ts`, `src/vendor/antd/reset.css` si ya no se usan.
17. Buscar referencias residuales y borrar código muerto.
18. Ejecutar suite completa y preparar PR con checklist de regresión.

## Recomendación final

Hacer el cambio es **aconsejable**: reduce deuda técnica, elimina una fuente de confusión (TS vs runtime) y deja el stack coherente con dependencias reales. Riesgo general **bajo-medio**, centrado principalmente en tests y algunos ajustes de UI menores.

## Aclaración importante: ¿“se interpreta distinto al escribir que al ejecutar”?

Sí, en este proyecto puede pasar algo muy parecido a eso:

- **Mientras escribes (TypeScript/IDE)**, el import `from "antd"` se resuelve usando `paths` hacia `src/vendor/antd`.
- **Cuando ejecutas con Vite (dev/build/test)**, el import `from "antd"` se resuelve desde `node_modules/antd` porque Vite no tiene ese alias al shim.

Técnicamente no son “dos JavaScript distintos en el navegador”, sino **dos resoluciones de módulo en fases distintas del toolchain**:

1. **Fase de type-check/editor** → manda `tsconfig` (`paths`).
2. **Fase de bundling/runtime** → manda Vite/Rollup (sin alias de `antd`, por lo que usa paquete real).

Consecuencia práctica: puedes tener una situación engañosa donde el editor/TS “cree” unas props o comportamientos (shim), pero en ejecución manda otra implementación (Ant real). Esa desalineación es precisamente la deuda técnica que conviene eliminar.
