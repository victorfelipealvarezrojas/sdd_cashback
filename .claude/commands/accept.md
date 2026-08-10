---
description: Escribe un test de aceptación que falle para la siguiente regla de la spec
argument-hint: "<nombre de la regla> @doc/specs/<feature>.md"
---

Escribe un test de aceptación que falle para: $ARGUMENTS

Lee CLAUDE.md para conocer las convenciones del proyecto antes de escribir nada.
Vuelve a leer el archivo de la spec para entender la regla completa, sus ejemplos
y sus contraejemplos.

## Estructura

Una clase externa por feature, nombrada <Feature>AcceptanceIT.
Una clase interna @Nested por regla — nómbrala según la regla.
Un @Test por cada ejemplo de la spec.

Usa @DisplayName con el lenguaje de negocio exacto de la spec:
- Clase: el nombre de la regla
- Método: el texto "The one where..." de la spec

## Cómo testear

Testea a través de la REST API usando @SpringBootTest + MockMvc.
Envía peticiones HTTP reales. Asevera respuestas HTTP reales.
NUNCA llames a servicios ni a objetos de dominio directamente —
esto es un test de aceptación, no un test unitario.

Asevera los valores exactos de los ejemplos de la spec.
Para dinero: .andExpect(jsonPath("$.amount").value("1.60"))
o usa isEqualByComparingTo con BigDecimal.

## Qué NO hacer

NO escribas código de producción. El test DEBE FALLAR.
Un test que pasa significa que no testeaste nada.
NO escribas tests para todas las reglas de una vez.
Solo una regla — la especificada en los argumentos.
NO inventes ejemplos más allá de lo que provee la spec.
La spec es el contrato.
NO uses mocks en tests de aceptación.
Conecta el stack completo: controller → service → domain → persistence.

## Cuando termines

Corre el test. Confirma que falla por la razón CORRECTA:
- Endpoint faltante → 404 o error de compilación (bien)
- Valor incorrecto → todavía no, el endpoint no debería existir
- El test pasa → algo está mal, investiga

Reporta: qué regla testeaste, cuántos ejemplos, y
la razón de la falla.

DETENTE. No avances a la implementación.