# Backend remodel plan (TFG)

## Objetivo
Aplicar de forma coherente una arquitectura por capas (*API → Application → Domain ← Infrastructure*) sin reescritura total, priorizando refactor incremental de alto impacto.

## Reglas arquitectónicas

### API (`api`)
- Responsabilidad: HTTP (request/response, códigos de estado, validación de entrada básica).
- No debe contener lógica de negocio.
- No debe acceder directamente a repositorios/puertos.

### Application (`application`)
- Responsabilidad: casos de uso, orquestación, autorización de negocio y coordinación de puertos.
- Define puertos (`application/ports`) consumidos por use cases.

### Domain (`domain`)
- Responsabilidad: reglas de negocio e invariantes del modelo.
- No depende de capas externas.

### Infrastructure (`infrastructure`)
- Responsabilidad: detalles técnicos (JPA, seguridad, configuración, adaptadores).
- Implementa puertos de `application`.

## Dependencias permitidas
- `api -> application`
- `application -> domain`
- `application -> application.ports`
- `infrastructure -> application.ports` (e indirectamente `domain` para mapping)
- `domain` no depende de `api/application/infrastructure`

## Convenciones de naming
- Casos de uso: `*UseCase`
- Entrada de casos de uso:
  - `*Command` para escritura
  - `*Query` para lectura
- Salida de casos de uso: `*Result`
- Puertos: `*Repository`
- Adaptadores de infraestructura: `Jpa*Repository`
- Mapeadores: `*Mapper`

## Criterios de autorización
- `SecurityConfig`: autorización por endpoint (coarse-grained).
- Use cases: autorización de negocio contextual (fine-grained).

## Criterio de refactor
- Priorizar coherencia y mantenibilidad sin romper contratos HTTP.
- Refactor por bloques pequeños (1 bloque = 1 commit).
