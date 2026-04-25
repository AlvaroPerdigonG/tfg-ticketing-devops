# ADR 0001 — Arquitectura backend por capas y fronteras de autorización

- **Estado:** Aceptada
- **Fecha:** 2026-04-25
- **Ámbito:** `ticketing-backend`

## Contexto
El backend evolucionó por iteraciones con estilos mixtos: algunos endpoints usaban casos de uso en `application`, mientras otros accedían a repositorios desde controllers.

## Decisión
1. Adoptar como arquitectura objetivo **API → Application → Domain ← Infrastructure**.
2. Controllers (`api`) solo manejan HTTP (request/response/validación superficial) y delegan en use cases.
3. La lógica de negocio y orquestación vive en `application`.
4. `domain` mantiene invariantes y reglas del modelo.
5. `infrastructure` implementa puertos de `application`.
6. Patrón único de actor actual: `CurrentUserProvider` en API + `CurrentUser` como input de use cases.
7. Autorización en dos niveles:
   - **Coarse-grained** en `SecurityConfig` (acceso a endpoint).
   - **Fine-grained** en use cases (reglas contextuales de negocio).
8. Política de timeline/eventos: `TicketEventType` final = `TICKET_CREATED`, `STATUS_CHANGED`, `ASSIGNED_TO_ME`.
   Los comentarios se representan como mensajes del agregado `Ticket`, no como evento técnico separado.

## Consecuencias
- Más coherencia arquitectónica y mejor defendibilidad para TFG.
- Menor acoplamiento controller-repositorio.
- Reglas de autorización más explícitas y testeables.
- Se requiere disciplina para mantener naming y límites por capa en nuevas funcionalidades.
