# Estrategia CI/CD del proyecto Ticketing (TFG)

## 1. Propósito del documento
Este documento define la estrategia base de **Integración Continua (CI)** y **Entrega/Despliegue Continuo (CD)** para el monorepo del TFG (`ticketing-backend` + `ticketing-frontend`). Su objetivo es dejar criterios técnicos claros, reproducibles y auditables antes de implementar los workflows completos.

## 2. CI vs CD aplicado a este proyecto

### CI (Integración Continua)
En este TFG, CI valida automáticamente la calidad técnica de cada cambio antes de integrar código en `main`:
- ejecución de tests,
- control de calidad estática,
- verificación de build,
- y trazabilidad funcional mínima entre escenarios y pruebas.

### CD (Entrega/Despliegue Continuo)
CD automatizará el despliegue a un entorno ejecutable tras superar CI. En esta fase inicial se deja definida la estrategia, pero la automatización se implementará en un lote posterior.

## 3. Justificación de la fase DevOps después de la fase de testing
Se prioriza primero la fase de testing porque CI/CD solo aporta valor si existe una base de validación fiable.

En este proyecto ya existen pruebas en backend y frontend, por lo que la fase DevOps se aborda ahora para:
1. convertir validaciones manuales en validaciones automáticas,
2. mantener consistencia de calidad por PR,
3. y generar evidencia defendible en la memoria del TFG (qué se valida, cuándo y con qué herramientas).

## 4. Validaciones objetivo para CI
La CI del monorepo debe automatizar, como mínimo, las siguientes comprobaciones:

1. **Backend unit tests**
   - Ejecución de pruebas unitarias de Spring Boot/Maven.
   - Validan lógica de dominio y casos de uso.

2. **Backend integration tests**
   - Ejecución de pruebas de integración API/infra en backend.
   - Confirman contratos principales y comportamiento extremo a extremo dentro del servicio.

3. **Frontend lint / format**
   - Linting y validación de formato en React + TypeScript.
   - Previene deuda técnica y divergencias de estilo.

4. **Frontend unit/UI tests**
   - Ejecución de pruebas unitarias y de componentes.
   - Asegura comportamiento funcional de UI y flujos críticos.

5. **Frontend build**
   - Compilación de producción con Vite.
   - Detecta roturas de tipado, bundling o dependencias.

6. **Trazabilidad**
   - Comprobación automática de referencias a IDs funcionales (`AUTH-*`, `TICKET-*`, `ADMIN-*`) en pruebas backend/frontend/E2E.
   - No bloqueante en el primer lote (modo scaffold), pero con reporte persistente para auditoría.

7. **SonarCloud**
   - Análisis estático y calidad global del monorepo desde CI.
   - Integración de cobertura backend (JaCoCo fusionado) y extensión futura para cobertura frontend.

## 5. Automatizaciones objetivo para CD

### 5.1 Staging desde `main`
- Despliegue automático a `staging` tras merge en `main` y superación de validaciones previas.
- Entorno pensado para validar integración real de frontend + backend ya desplegados.

### 5.2 Smoke checks post-deploy
- Comprobaciones básicas de disponibilidad y sanidad funcional tras despliegue (por ejemplo, health endpoints y carga mínima de frontend).
- Objetivo: detectar fallos de despliegue antes de considerar el entorno estable.

### 5.3 Producción (fase posterior)
- La promoción a `production` se aplaza a una fase siguiente.
- Se recomienda enfoque progresivo: primero estabilizar staging y observabilidad, luego formalizar puertas de promoción a producción.

## 6. Filosofía de quality gates
Los quality gates del proyecto deben ser **pragmáticos pero exigentes**:

1. **Tests significativos**: no solo cantidad; priorizar cobertura de reglas de negocio y flujos críticos.
2. **Trazabilidad ligera**: enlazar escenarios funcionales con evidencia de test sin añadir burocracia excesiva.
3. **Análisis estático**: mantener estándares consistentes de mantenibilidad y detectar issues temprano.
4. **Seguridad básica**: análisis automatizado de dependencias y código para reducir riesgos evidentes.

## 7. Estrategia de ramas recomendada

- **`main` protegida**
  - Sin pushes directos.
  - Integración únicamente vía Pull Request.

- **Feature branches**
  - Trabajo aislado por cambio (`feature/*`, `fix/*`, etc.).
  - Permiten CI por rama antes de proponer merge.

- **PR obligatoria**
  - Revisión mínima + checks automáticos obligatorios para merge.
  - Garantiza trazabilidad técnica de decisiones y cambios.

## 8. Papel de cada herramienta

1. **GitHub Actions**
   - Orquestador de CI/CD.
   - Ejecuta jobs de tests, build, calidad y despliegue.

2. **SonarCloud**
   - Calidad estática centralizada y quality gate global.
   - Aporta métricas comparables a lo largo del TFG.

3. **Dependabot**
   - Actualización automatizada de dependencias.
   - Reduce riesgo de vulnerabilidades por librerías obsoletas.

4. **CodeQL**
   - Análisis de seguridad del código en repositorio.
   - Complementa SonarCloud con foco específico en patrones de vulnerabilidad.

5. **GitHub Environments (`staging`, `production`)**
   - Separación explícita de secretos, reglas y aprobaciones por entorno.
   - Base para despliegues controlados.

## 9. Por qué separar workflows en varios ficheros
Se recomienda dividir workflows por responsabilidad en lugar de concentrarlo todo en un único YAML:

- `ci-*`: validación de código y tests,
- `quality-*`: análisis estático/seguridad,
- `deploy-*`: despliegues por entorno.

Ventajas para el TFG:
- menor acoplamiento y mayor mantenibilidad,
- tiempos de ejecución más previsibles,
- diagnóstico de fallos más rápido,
- y explicación académica más clara de cada fase.

## 10. Enfoque pragmático para un TFG
Esta estrategia prioriza:
1. **Sencillez**: diseño entendible y operable por una sola persona.
2. **Reproducibilidad**: mismas validaciones en local y CI cuando sea posible.
3. **Valor académico**: evidencias objetivas de calidad, trazabilidad y control de cambios.

Con este enfoque, el TFG evita tanto la infra-automatización (manual, frágil) como la sobre-ingeniería (complejidad innecesaria para su alcance).
