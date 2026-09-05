# Categorías de comerciante y elegibilidad

## Historia

Como gerente de producto de recompensas de tarjeta, quiero que la tasa de
cashback de una compra dependa de la categoría de comerciante (MCC) de esa
transacción y que solo se acredite cashback en transacciones elegibles, para
que las recompensas reflejen la estrategia comercial por rubro y no se
otorguen sobre movimientos que no son compras reales.

> **Corrección de modelo (post-discovery):** la categoría **no es un
> atributo fijo del comercio** — es un dato de **cada transacción** (su MCC,
> Código de Categoría de Comercio). Un mismo comercio puede enviar compras
> con distinto MCC (p. ej. un hipermercado con surtidor de combustible:
> unas compras son Supermercado, otras Combustible). El borrador original de
> esta spec asumía "categoría del comercio" como si fuera una configuración
> por `comercioId`; esa lectura queda corregida aquí. Ver Regla 1.

### Categorías de comerciante

- Supermercado: 2% de cashback
- Combustible: 1% de cashback
- Por defecto: 0.5% de cashback

### Reglas de elegibilidad

- Solo transacciones contabilizadas (no pendientes)
- Solo tarjetas activas (no congeladas/canceladas)
- Solo compras (no reembolsos ni comisiones)

### Relación con features anteriores

- Esta feature **reemplaza** la forma en que se determina la tasa de
  cashback en "Cálculo básico de cashback" (Regla 1 y Regla 2 de esa spec):
  ya no existe una tasa configurada manualmente por comercio; la tasa
  depende siempre de la categoría del comercio. **Confirmado explícitamente
  por el negocio.**
- Esta feature agrega una verificación de **elegibilidad** que no existía
  antes: "Cálculo básico de cashback" asumía que toda compra recibida era,
  por definición, una compra válida ya contabilizada.
- El truncamiento a 2 decimales (`RoundingMode.DOWN`), el tope mensual de
  100.00, y el registro de trazabilidad incluso con cashback 0.00 (Reglas 3,
  4 y 5 de "Cálculo básico de cashback") siguen vigentes sin cambios sobre
  el monto final, una vez resueltas tasa y elegibilidad.

### Alcance

- Determinar la tasa de cashback aplicable según la categoría (MCC) de la
  transacción.
- Determinar si una transacción es elegible para generar cashback.
- Fuera de alcance: gestión de categorías (crear/editar categorías o sus
  tasas — son fijas en código), reverso de cashback por reembolso de una
  compra ya acreditada, consulta en tiempo real del estado de la tarjeta a
  un sistema externo, mapeo de MCCs numéricos reales de redes de tarjeta
  (Visa/Mastercard) a estas 3 categorías de negocio — el request recibe la
  categoría/MCC de la transacción directamente, ya resuelta por quien la
  emite (mismo criterio que `montoNeto` en "Cálculo básico de cashback": el
  dato llega ya calculado, esta feature no lo deriva).

## Example Mapping

### Regla 1: Debe calcular el cashback usando la tasa de la categoría (MCC) de la transacción: Supermercado 2%, Combustible 1%, Por defecto 0.5%.

| Categoría (MCC) de la transacción | Tasa de cashback |
|---|---|
| Supermercado | 2% |
| Combustible | 1% |
| Por defecto | 0.5% |

- Contraejemplo: El caso en que la transacción no trae categoría, o trae una
  categoría que el sistema no reconoce (p. ej. "Farmacia") → se le aplica la
  tasa "Por defecto" de 0.5%, no un error ni 0.00.
- Contraejemplo: El caso en que el mismo comercio realiza dos compras con
  categorías distintas (p. ej. un hipermercado con surtidor de combustible:
  una compra de víveres y otra de combustible) → cada compra usa la tasa de
  **su propia** categoría (2% y 1% respectivamente), no una tasa fija del
  comercio. Esto es lo que exige que la categoría viaje en la transacción y
  no se resuelva por `comercioId`.

**Decisiones (confirmadas explícitamente por el negocio):**
- La categoría **es un dato de la transacción, no del comercio**. Un mismo
  comercio puede enviar compras con distinto MCC. El `comercioId` deja de
  intervenir en el cálculo de la tasa de cashback.
- La categoría de la transacción **reemplaza por completo** la tasa
  configurada manualmente en "Cálculo básico de cashback"; no existe tasa
  personalizada por comercio que anule la de su categoría.
- "Sin categoría informada" y "categoría no reconocida" se tratan igual que
  la categoría "Por defecto" (0.5%): nunca resultan en cashback 0.00 por
  falta de categoría. El concepto de "comercio no asociado = 0.00" de la
  feature anterior queda superado por esta regla — toda transacción tiene
  una tasa aplicable, mínimo 0.5%.
- El conjunto de categorías es **fijo en código** (enum); agregar una
  categoría nueva requiere un cambio de código y despliegue.
- El mapeo de MCCs numéricos reales de la red de tarjetas a estas 3
  categorías queda **fuera de alcance**; el request recibe la categoría de
  la transacción ya resuelta.

**Decisión (ya no aplica — se retira):** ~~si la categoría de un comercio
cambia, las compras futuras usan la tasa nueva y las pasadas conservan la
tasa histórica~~. No hay "categoría del comercio" que pueda cambiar: cada
compra trae su propia categoría, así que la tasa histórica de una compra ya
registrada nunca depende de nada externo a esa misma compra — el punto que
esta decisión intentaba resolver (consistencia del histórico) queda resuelto
trivialmente por el nuevo modelo.

### Regla 2: Debe acreditar cashback solo si la transacción está contabilizada (no pendiente).

- Ejemplo: El caso en que la transacción llega marcada como contabilizada →
  el cashback se calcula con normalidad según la categoría del comercio.
- Contraejemplo: El caso en que la transacción está pendiente (autorizada
  pero aún no contabilizada) → no se genera cashback (0.00) en este momento.

**Decisión (confirmada explícitamente por el negocio):** cada transacción
se evalúa **una sola vez**, con el estado que trae en ese momento. Esta
feature no modela un evento de "cambio de estado" sobre una transacción ya
registrada; si una transacción pendiente pasa después a contabilizada, se
asume que el sistema de origen la reenvía como una transacción nueva a
evaluar.

**Decisión (asumida por consistencia, pendiente de confirmación explícita
del negocio):** cualquier estado de transacción distinto de "contabilizada"
(p. ej. "rechazada", "en disputa", si existieran) se trata igual que
"pendiente": no elegible, cashback 0.00.

### Regla 3: Debe acreditar cashback solo si la tarjeta usada está activa.

| Estado de la tarjeta | Cashback |
|---|---|
| Activa | Se calcula con normalidad |
| Congelada | 0.00 |
| Cancelada | 0.00 |

La fila "Congelada"/"Cancelada" ya cubre el contraejemplo de esta regla: una
tarjeta no activa no genera cashback aunque la transacción sea, por lo
demás, elegible.

**Decisión (confirmada explícitamente por el negocio):** el estado de la
tarjeta llega como dato de entrada de la transacción (snapshot al momento de
la compra), igual que `montoNeto` en "Cálculo básico de cashback"; esta
feature no consulta ningún servicio de tarjetas en tiempo real.

**Decisión (asumida por consistencia, pendiente de confirmación explícita
del negocio):** cualquier estado de tarjeta distinto de "activa" (p. ej.
"vencida", "bloqueada por fraude", si existieran) se agrupa como "no
activa": no elegible, cashback 0.00.

### Regla 4: Debe acreditar cashback solo en transacciones de tipo compra (no en reembolsos ni comisiones).

| Tipo de transacción | Cashback |
|---|---|
| Compra | Se calcula con normalidad |
| Reembolso | 0.00 |
| Comisión | 0.00 |

**Decisiones (confirmadas explícitamente por el negocio):**
- Un reembolso o una comisión **no generan cashback nuevo**; no hay ningún
  reverso ni ajuste del cashback ya acreditado por la compra original que
  se está reembolsando. Reversar cashback por reembolso queda **fuera de
  alcance** de esta feature.

**Decisión (asumida por consistencia, pendiente de confirmación explícita
del negocio):** cualquier tipo de transacción distinto de "compra" (p. ej.
"ajuste", "contracargo", si existieran) se trata igual que "comisión": no
elegible, cashback 0.00.

### Regla 5: No debe lanzar una excepción cuando una transacción resulta inelegible; debe procesarse con cashback 0.00.

- Ejemplo: El caso en que una transacción es un reembolso en una tarjeta
  activa → se procesa con cashback 0.00, sin lanzar excepción.
- Contraejemplo: sería tratar la inelegibilidad como un error del sistema
  (p. ej. rechazar la transacción o lanzar una excepción); eso es el
  comportamiento que esta regla evita — no aporta un ejemplo de negocio
  adicional, ya está cubierto por los contraejemplos de las Reglas 2-4.

**Decisión (asumida por consistencia con "Cálculo básico de cashback"
Regla 3, pendiente de confirmación explícita del negocio):** se sigue
registrando la transacción de cashback en 0.00 por trazabilidad para
transacciones inelegibles, igual que para un comercio sin tasa. El detalle
mensual ("Detalle y total de cashback mensual") ya decidió que no se
distingue el motivo del 0.00 (sin tasa, truncamiento, tope, o ahora también
inelegibilidad); esto se mantiene sin cambios.

### Orden de aplicación propuesto

Por consistencia con el orden ya documentado en "Cálculo básico de
cashback":

1. Elegibilidad — tipo de transacción, estado de la transacción, estado de
   la tarjeta. Si alguna falla, cashback = 0.00 y se salta directo al
   registro (paso 5); no es necesario determinar la categoría/tasa.
2. Categoría del comercio → tasa de cashback aplicable.
3. Cashback exacto = monto neto × tasa (sin truncar).
4. Comparar contra el remanente del tope mensual (Regla 5 de "Cálculo
   básico de cashback").
5. Truncar a 2 decimales (`RoundingMode.DOWN`) y registrar la transacción,
   acreditando el monto (aunque sea 0.00).

Como el detalle mensual no distingue el motivo del 0.00 (Regla 5, arriba),
no importa si se calcula o no la tasa de categoría para una transacción
inelegible — el dato no se expone de todas formas.
