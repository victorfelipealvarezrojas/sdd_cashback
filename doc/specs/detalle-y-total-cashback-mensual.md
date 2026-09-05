# Detalle y total de cashback mensual

## Historia

Como cliente, quiero ver el detalle y el total de mis ganancias de cashback
cada mes, para poder entender y confiar en cómo se calculan mis recompensas.

### Relación con features anteriores

- Esta feature es la consulta que "Cálculo básico de cashback" dejó fuera de
  alcance explícitamente: *"Consultar o canjear el saldo de recompensas queda
  fuera de alcance, para una feature futura"*. Esta es esa feature futura,
  acotada a la consulta (no incluye canje).
- Reutiliza los datos ya registrados por esa feature: cada compra, tenga o no
  cashback, queda registrada como una transacción de cashback (Regla 3 y 4 de
  esa spec). Esta feature no recalcula nada, solo consulta y presenta lo ya
  acreditado.
- El **total mensual** de esta feature es distinto del **saldo de
  recompensas** (Regla 4 de la spec anterior), que es acumulado histórico sin
  distinción de mes. Aquí el total está acotado a un mes calendario específico.

### Alcance

- Solo lectura: mostrar detalle y total. Canjear o ajustar el saldo queda
  fuera de alcance.
- Un mes calendario a la vez por consulta (no rangos de varios meses).
- La restricción de acceso (que un cliente solo pueda consultar su propio
  detalle/total) **queda fuera de alcance** de esta feature, igual que en
  "Cálculo básico de cashback": el `clienteId` se recibe como parámetro de la
  consulta y se asume ya validado externamente. No hay infraestructura de
  autenticación en este microservicio (sin `spring-security` en el pom.xml).

## Example Mapping

### Regla 1: Debe mostrar el detalle de cashback del cliente para un mes dado, con una línea por cada compra que participó en el cálculo de cashback, incluyendo la información suficiente para que el cliente pueda verificar el cálculo (monto neto, tasa del comercio aplicada en el momento de la compra, cashback acreditado).

- Ejemplo: El caso en que el cliente tiene 2 compras en el mes, en comercios con tasas distintas → el detalle muestra 2 líneas, cada una con su monto neto, tasa aplicada y cashback acreditado, de forma que el cliente puede verificar `monto neto × tasa = cashback`, ordenadas de la más antigua a la más reciente.
- Contraejemplo: El caso en que una compra alcanzó el tope mensual y su cashback acreditado fue menor al que resultaría de `monto neto × tasa` → el detalle debe marcar explícitamente esa línea como limitada por el tope (p. ej. un indicador booleano), no debe verse como si el cálculo no cuadrara sin explicación.

**Decisiones (confirmadas explícitamente por el negocio):**
- Se muestra únicamente el cashback **acreditado** (ya truncado y limitado por el tope); no se expone el cashback exacto sin truncar/sin tope como campo aparte.
- Cuando una compra fue limitada por el tope mensual, se agrega un indicador explícito (p. ej. `limitadoPorTope: true`) en su línea del detalle.
- La tasa mostrada es la que efectivamente se aplicó en el momento de la compra, no la tasa actual del comercio (evita que el detalle histórico cambie si el comercio ajusta su tasa después). Con `categorias-comerciante-y-elegibilidad.md`, esto aplica igual si cambia la categoría del comercio: se conserva la tasa histórica de la categoría vigente al momento de la compra.
- Orden del detalle: cronológico ascendente (de la compra más antigua a la más reciente dentro del mes).

### Regla 2: Debe incluir en el detalle las compras cuyo cashback acreditado fue 0.00 (truncamiento a cero, tope mensual ya alcanzado, o transacción inelegible), sin omitirlas del listado.

- Ejemplo: El caso en que el cliente realiza una compra cuyo cashback exacto trunca a cero → la compra aparece en el detalle con cashback 0.00, no se excluye del listado.

**Decisión (confirmada explícitamente por el negocio):** no se distingue el motivo del 0.00 con un campo textual separado; el detalle solo muestra el valor final (0.00), sin desglosar la causa (truncamiento, tope, o inelegibilidad).

> **Nota de actualización:** el ejemplo original de esta regla era "comercio
> sin tasa de cashback configurada". Ese motivo de 0.00 ya no existe —
> `categorias-comerciante-y-elegibilidad.md` establece que todo comercio
> tiene una tasa aplicable (mínimo 0.5%, categoría "Por defecto"). Los
> motivos vigentes de un 0.00 son: truncamiento a cero, tope mensual
> alcanzado, o transacción inelegible (esta última, nueva en esa feature).
> La decisión de esta regla (no distinguir el motivo) sigue vigente y ahora
> también cubre la inelegibilidad.

### Regla 3: Debe calcular el total de cashback del mes como la suma de los montos de cashback acreditados (ya truncados y limitados por el tope mensual) de todas las compras del mes, incluidas las de 0.00.

| Cashback acreditado compra 1 | Cashback acreditado compra 2 | Cashback acreditado compra 3 | Total del mes |
|---|---|---|---|
| 2.00 | 4.68 | 0.00 | 6.68 |
| 90.00 | 8.00 | 2.00 (exacto hubiera sido 7.00, limitado por el tope) | 100.00 |

La segunda fila cubre el contraejemplo de esta regla: el total del mes nunca supera el tope de 100.00, porque cada compra ya fue limitada individualmente antes de sumarse (Regla 5 de "Cálculo básico de cashback").

**Decisión (confirmada explícitamente por el negocio):** el total mostrado coincide siempre con el acumulado mensual que rige el tope; no hay ajustes ni reversos en el alcance actual del dominio que puedan generar una diferencia.

### Regla 4: No debe incluir en el detalle ni en el total compras de meses distintos al mes consultado.

- Ejemplo: El caso en que el cliente tiene compras en enero y febrero, y se consulta el detalle de enero → solo aparecen las compras de enero; el total no incluye las de febrero.
- Contraejemplo: El caso en que una compra se registra en el último instante del mes (p. ej. 31 de enero 23:59:59) → debe aparecer en el detalle de enero, no en el de febrero, a pesar de estar en el límite.

**Decisión (confirmada explícitamente por el negocio):** el mes de una compra se determina con la zona horaria del servidor/sistema, el mismo criterio que ya rige el reinicio del tope mensual en "Cálculo básico de cashback" (consistencia entre ambas features).

### Regla 5: Debe responder con detalle vacío y total 0.00 cuando el cliente no tiene compras registradas en el mes consultado, sin tratarlo como un error.

- Ejemplo: El caso en que el cliente no realizó ninguna compra en el mes actual → detalle vacío, total 0.00.
- Contraejemplo: El caso en que se consulta un mes anterior sin compras del cliente → mismo comportamiento (detalle vacío, total 0.00); no se distingue de un `clienteId` que nunca existió, porque el sistema no tiene un registro de clientes independiente de sus compras.

### Regla 5b: No debe permitir consultar un mes futuro (posterior al mes calendario actual).

- Ejemplo: El caso en que se consulta un mes que aún no ha ocurrido → la consulta se rechaza como inválida (400), no se responde con detalle vacío.

**Decisiones (confirmadas explícitamente por el negocio):**
- Mes futuro → error de validación (400), no un detalle vacío. Distinto del caso "mes pasado o actual sin compras" (Regla 5), que sí es válido y responde vacío.
- No existe distinción entre "cliente sin compras este mes" y "clienteId inexistente"; el sistema no tiene un registro de clientes separado de sus compras (mismo criterio que la feature anterior).

### Regla 6: No debe restringir el acceso al detalle/total de otro cliente — queda fuera de alcance de esta feature.

**Decisión (confirmada explícitamente por el negocio):** igual que en "Cálculo básico de cashback", el `clienteId` se recibe como parámetro de la consulta y se asume ya validado externamente. No hay infraestructura de autenticación en este microservicio (sin `spring-security` en el pom.xml). Introducir control de acceso queda fuera de alcance; podría abordarse en una feature futura si el negocio lo requiere.

### Regla 7: Debe usar el mes calendario actual como valor por defecto cuando el cliente no especifica un mes en la consulta.

- Ejemplo: El caso en que el cliente consulta sin indicar mes → se muestra el detalle y total del mes en curso.
- Contraejemplo: El caso en que el cliente indica explícitamente un mes anterior → se muestra el detalle y total de ese mes indicado, no del actual.

**Decisión (confirmada explícitamente por el negocio):** el mes se especifica en formato `YYYY-MM`; si se omite, se usa el mes calendario actual.

### Regla 8: Debe paginar el detalle de compras del mes.

**Decisiones (confirmadas explícitamente por el negocio):**
- Tamaño de página por defecto: 20. Tamaño máximo permitido: 100.
- El total del mes (Regla 3) se calcula sobre **todas** las compras del mes, independiente de la paginación del detalle; el total no depende de qué página se está viendo.
