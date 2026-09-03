# Cálculo básico de cashback

## Historia

Como cliente, quiero ganar cashback en mis compras para ser recompensado por
comprar con comercios asociados.

### Criterios de aceptación originales

- El cashback se calcula como un porcentaje del monto de la compra.
- Cada comercio tiene una tasa de cashback configurada.
- El cashback se acredita al saldo de recompensas del cliente.

### Alcance

- Esta feature calcula y acredita cashback. Consultar o canjear el saldo de recompensas queda **fuera de alcance**, para una feature futura.
- La validación del monto neto de la compra (que sea mayor a cero) y el cálculo de descuentos/impuestos quedan **fuera de alcance**; se asume que el monto neto llega ya calculado y validado desde el servicio de compras.
- El rango válido de la tasa de cashback de un comercio (p. ej. 0%–100%) se valida en la gestión de comercios, **fuera de alcance** de esta feature.

### Orden de aplicación de las reglas

Para cada compra, las reglas se aplican en este orden:

1. **Regla 2** — ¿el comercio tiene tasa configurada? Si no, cashback = 0.00 y se salta directo al paso 5.
2. **Regla 1** — calcular el cashback exacto (monto × tasa), sin truncar.
3. **Regla 5** — comparar el cashback exacto contra el remanente del tope mensual del cliente; el resultado es el menor entre ambos (crédito completo, parcial o 0.00 si ya se alcanzó el tope).
4. **Regla 3** — truncar a 2 decimales (RoundingMode.DOWN) el valor resultante del paso anterior.
5. **Regla 4** — registrar la transacción de cashback y acreditar el monto (aunque sea 0.00, por cualquiera de las causas anteriores) al saldo del cliente, de forma atómica con la compra.

## Example Mapping

### Regla 1: Debe calcular el cashback como un porcentaje del monto **neto** de la compra, usando la tasa de cashback del comercio.

| Monto neto de compra | Tasa de cashback del comercio | Cashback calculado |
|---|---|---|
| 100.00 | 2% | 2.00 |
| 200.00 | 2.34% | 4.68 |
| 100.00 | 100% | 100.00 |

La fila de tasa 100% cubre el contraejemplo de esta regla: el límite superior teórico, donde el cashback iguala el monto neto total de la compra.

**Decisiones** (no está en los 3 criterios originales de la HU; ampliación confirmada explícitamente por el negocio):
- La tasa se representa como porcentaje con hasta 2 decimales (p. ej. 2.34 = 2.34%), no como fracción decimal (0.0234). Se almacena y opera con `BigDecimal`, escala 2.
- **Monto neto** = monto de la compra **menos descuentos**, **antes de impuestos**. El cashback se calcula sobre este valor, no sobre el precio de lista ni sobre el total con impuestos.
- El monto neto llega ya calculado como dato de entrada desde el servicio/módulo de compras (externo a esta feature); esta feature no calcula descuentos ni impuestos, solo consume el monto neto recibido.

### Regla 2: No debe generar cashback en compras de comercios sin una tasa de cashback configurada. Un comercio se considera "asociado" si y solo si tiene una tasa configurada.

- Ejemplo: El caso en que el comercio "Comercio A" tiene una tasa configurada de 3% y el cliente compra por 100.00 → se calcula un cashback de 3.00.
- Contraejemplo: El caso en que el comercio no tiene ninguna tasa de cashback configurada (no está asociado) → la compra no genera cashback, se calcula 0.00, **sin lanzar excepción**.

**Decisiones** (no están en los 3 criterios originales de la HU; interpretación confirmada explícitamente por el negocio):
- Sin tasa configurada → cashback 0.00, la compra se procesa con normalidad (no es un error de dominio).
- "Comercio asociado al programa" y "comercio con tasa de cashback configurada" son el mismo concepto; no existe un estado de afiliación independiente.
- Una tasa de 0% configurada explícitamente se trata igual que "sin tasa": cashback 0.00 en ambos casos. El dominio no necesita distinguirlos.

### Regla 3: Debe truncar el cashback calculado a 2 decimales sin redondear hacia arriba (RoundingMode.DOWN).

| Monto neto de compra | Tasa de cashback | Cashback exacto | Cashback acreditado |
|---|---|---|---|
| 33.33 | 3% | 0.9999 | 0.99 |
| 10.00 | 7.5% | 0.75 | 0.75 |
| 0.01 | 1% | 0.0001 | 0.00 |

La fila de 0.01 / 1% ya cubre el contraejemplo de esta regla: una compra válida cuyo cashback trunca a cero.

**Decisiones:**
- La HU no especifica método de redondeo; se usa `RoundingMode.DOWN` por ser la convención de negocio ya definida en CLAUDE.md para cashback. **Confirmado explícitamente por el negocio para esta feature**: el cliente no recibe la fracción de centavo truncada.
- Cuando el cashback resultante es 0.00 (por truncamiento, por comercio sin tasa configurada, o por haber alcanzado el tope mensual), igual se registra la transacción de cashback (por trazabilidad); no se omite el registro en ningún caso. Ver "Orden de aplicación de las reglas".

### Regla 4: Debe acreditar el cashback calculado al saldo de recompensas del cliente.

- Ejemplo: El caso en que el cliente tiene un saldo de recompensas actual de 50.00 y su compra genera un cashback de 5.00 → el nuevo saldo es 55.00.
- Contraejemplo: El caso en que es la primera compra del cliente y no existe saldo previo → se crea el saldo de recompensas con el valor del cashback generado.

**Decisiones** (no están en los 3 criterios originales de la HU; confirmadas explícitamente por el negocio):
- El saldo de recompensas se identifica **solo por cliente** (sin distinción de moneda).
- La acreditación del cashback debe ser **atómica** con el registro de la compra: si algo falla, no queda cashback acreditado sin compra, ni compra sin su cashback.

**Decisión técnica** (tomada durante la implementación, pendiente de confirmación explícita del negocio): como "consultar el saldo de recompensas" queda fuera de alcance de esta feature, no existe ningún endpoint para observarlo. Se expone `saldoRecompensas` como campo adicional en la respuesta de `POST /api/compras` — es el resultado de la mutación (cuánto quedó acreditado tras esta compra), no una consulta separada del saldo.

### Regla 5: Debe limitar el cashback acumulado del cliente a un tope máximo mensual fijo de 100.00; una compra que exceda el tope solo acredita el remanente disponible hasta completarlo.

> Nota de alcance: este tope no está en los 3 criterios de aceptación originales de la HU. Es una ampliación de alcance **confirmada explícitamente por el negocio** durante el descubrimiento, no una inferencia de la HU.

| Cashback acumulado en el mes (antes de la compra) | Cashback exacto calculado de la compra | Cashback acreditado |
|---|---|---|
| 20.00 | 10.00 | 10.00 (no llega al tope) |
| 97.00 | 8.00 | 3.00 (parcial: solo el remanente hasta 100.00) |
| 100.00 | 5.00 | 0.00 (ya alcanzó el tope del mes) |

La fila de acumulado 100.00 ya cubre el contraejemplo: una compra que en otras circunstancias generaría cashback pero no acredita nada por haberse alcanzado el tope.

**Decisiones:**
- El tope es un **valor fijo global** de **100.00**, igual para todos los comercios y clientes (no se configura por comercio).
- El período de acumulación es **mensual** (mes calendario).
- La comparación contra el tope usa el cashback **exacto, sin truncar** (antes de aplicar la Regla 3). El monto que efectivamente se acredita (el remanente o el total, lo que sea menor) es el que luego se trunca a 2 decimales.
- Al superar el tope, se acredita cashback **parcial** (el remanente hasta completar el tope), no cero de forma abrupta.
- El acumulado se reinicia según el mes calendario del sistema (confirmado).
