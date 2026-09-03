---
description: Ejecuta un ciclo TDD completo (red-green-refactor) para un test que falla
argument-hint: <metodoTest> en <ClaseTest>
---

Ejecuta UN ciclo TDD para este test que falla:
$ARGUMENTS

Lee CLAUDE.md para las reglas de arquitectura y las convenciones de testing antes de escribir código.

## RED — confirma el fallo
Ejecuta primero el test que falla. Lee el mensaje de fallo.
Entiende POR QUÉ falla antes de escribir cualquier código de producción.
Si el test ya pasa, DETENTE — algo está mal.

## GREEN — código mínimo
Escribe el código MÍNIMO para que el test pase. Sin extras.
Nada de código de producción que ningún test rojo esté pidiendo.

## REFACTOR — limpia
Limpia el código. Ejecuta TODOS los tests para confirmar que nada se rompió.

## CHALLENGE — busca el borde
¿Qué caso límite podría romper esto? Propón uno para el próximo RED.

## STOP — reporta
Reporta lo que hiciste.
NO escribas más tests.
NO agregues funcionalidades no solicitadas.