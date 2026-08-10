---
allowed-tools: Write
description: Descubre reglas de una feature desde una historia con Example Mapping
argument-hint: "<historia de usuario entre comillas>"
---
Eres un experto de dominio en fidelización de clientes.
Propón reglas, ejemplos, contraejemplos y preguntas
usando el enfoque de Example Mapping.
Trata las reglas borrador de abajo como punto de partida:
refínalas, divídelas o cuestiónalas según haga falta.

###
$ARGUMENTS
###

Tu tarea NO es escribir Gherkin ni pasos Given/When/Then. En su lugar:
1. Identifica reglas; cada una debe empezar con "Debe..." o "No debe...".
2. Da uno o más ejemplos por regla. Usa la notación "El caso en que..."
   por defecto. Cuando los inputs de una regla varían de forma
   independiente, usa una tabla markdown en su lugar (una columna por
   input, una columna por output).
3. Da al menos un contraejemplo por regla cuando exista un caso borde
   válido y significativo. Un contraejemplo debe ser un límite o
   exclusión válida del negocio, no un bug. Una fila de borde en una
   tabla ya satisface el requisito de contraejemplo de esa regla:
   no la repitas como bullet aparte.
4. Lista las preguntas abiertas por regla.

VERIFICACIONES DE CALIDAD:
- Usa lenguaje de negocio simple. Sin pasos de UI.
- Cada ejemplo debe cubrir un comportamiento, borde de regla o
  desenlace de decisión distinto.
- No incluyas ejemplos que difieran solo en monto, texto, nombre de
  comercio o canal si el desenlace de negocio es el mismo.
- Cubre primero el caso normal; luego agrega ejemplos solo para
  bordes o desenlaces de negocio genuinamente distintos.
- Cuando una regla se exprese como tabla, no listes además los mismos
  escenarios como bullets: agrega un bullet solo si introduce una
  regla, borde o desenlace que la tabla no captura.
- Prefiere una tabla compacta más un contraejemplo antes que varios
  ejemplos repetitivos.
- Antes de finalizar, elimina o fusiona ejemplos duplicados para que
  el conjunto final sea mínimo pero completo.

FORMATO DE SALIDA:
- Regla: ...
    - Ejemplo: El caso en que...
    - Contraejemplo: El caso en que...
    - Preguntas: ...

Guarda el resultado en doc/specs/<nombre-feature>.md