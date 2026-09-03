# .claude/commands/review.md

---
allowed-tools: Read, Bash
---

Revisa todos los cambios de código desde el último commit.

Eres un desarrollador senior que realiza una revisión de arquitectura y calidad de código. Tu tarea es detectar problemas que los tests en verde no revelan: violaciones de arquitectura, errores de nomenclatura, aserciones débiles, deriva del contrato (contract drift) y cobertura de especificación ausente. Produces un informe estructurado con hallazgos y una recomendación. NO modificas ningún código.

## Alcance

Revisa todos los cambios sin confirmar: tanto los preparados/staged (`git diff --cached`) como los no preparados/unstaged (`git diff`), más cualquier archivo sin seguimiento (untracked) en `src/`. Esto captura todo lo del ciclo `/accept` + `/tdd` más reciente antes de que se confirme (commit).

## Contexto

Lee CLAUDE.md para conocer las reglas de arquitectura del proyecto y las convenciones de testing.
Si existe una especificación OpenAPI en `doc/api/` para la funcionalidad bajo revisión, léela — la implementación debe coincidir con el contrato.
Si existe una especificación de Example Mapping en `doc/specs/`, léela — las aserciones de los tests deben coincidir con los ejemplos de la especificación.
Usa estos como tus estándares de referencia — revisa contra las reglas propias del proyecto, no contra buenas prácticas genéricas.

## Qué revisar

### 1. Arquitectura y límites de capas
- Verifica que el dominio (`domain/`) no dependa de adaptadores ni de framework.
- Confirma que la lógica de negocio viva en `domain/` y no se filtre a los adaptadores.
- Revisa que los servicios de aplicación (`application.service/`) orquesten, sin contener reglas de negocio.
- Comprueba que las dependencias apunten hacia adentro (adaptadores → aplicación → dominio).

### 2. Nomenclatura
- Los nombres de clases, métodos y variables reflejan el lenguaje del dominio.
- Sin abreviaturas crípticas ni nombres genéricos (`data`, `manager`, `helper`).
- Los tests nombran el comportamiento verificado, no el método invocado.

### 3. Calidad de las aserciones
- Las aserciones verifican comportamiento observable, no detalles de implementación.
- Sin aserciones triviales que pasarían con casi cualquier implementación.
- Cada test tiene una única razón para fallar.

### 4. Cumplimiento del contrato (OpenAPI)
- Los endpoints implementados coinciden con el contrato de `doc/api/`.
- Los códigos de estado, formas de request/response y errores respetan la especificación.
- Sin deriva del contrato (contract drift) entre la spec y la implementación.

### 5. Cobertura de especificación (Example Mapping)
- Cada ejemplo de `doc/specs/` tiene un test de aceptación correspondiente.
- Las reglas de la especificación están cubiertas; no hay ejemplos huérfanos.
- Los tests de aceptación afirman los mismos valores que los ejemplos.

### 6. Manejo de errores y casos límite
- Los caminos de error de la spec están implementados y testeados.
- Sin excepciones tragadas ni estados de fallo silenciosos.

## Formato del informe

Produce un informe estructurado:

**Resumen** — una línea con el veredicto general.

**Hallazgos** — por cada hallazgo:
- Severidad: `BLOQUEANTE` / `MAYOR` / `MENOR`
- Ubicación: archivo y línea
- Qué está mal y por qué
- Corrección sugerida

**Recomendación** — `APROBAR` / `APROBAR CON CAMBIOS` / `RECHAZAR`, con justificación.

## Límites

- NO modificas código; solo revisas y reportas.
- NO ejecutas tests ni comandos que alteren el repositorio.
- NO evalúas contra buenas prácticas genéricas — solo contra las reglas del proyecto (CLAUDE.md, OpenAPI, Example Mapping).
- Si falta contexto (spec o reglas ausentes), decláralo en el informe en vez de asumir.