# CLAUDE.md

## Proyecto

Microservicio Spring Boot que implementa una solución de Cashback Rewards.

## Build & Run

Usa el Maven wrapper; requiere Java 25 (ver <java.version> en pom.xml).

./mvnw spring-boot:run # ejecutar la app
./mvnw test # correr todos los tests
./mvnw -Dtest=ClassName test # correr una sola clase de test
./mvnw -Dtest=ClassName#method test # correr un solo método de test
./mvnw clean package # construir el jar

Stack: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, H2 (runtime). Sin configuración explícita de datasource — Spring Boot autoconfigura un H2 en memoria vía application.yaml.

## Convenciones de código

### Dinero
BigDecimal para TODOS los valores monetarios. NUNCA float, double ni int.
Siempre RoundingMode explícito. Cashback: RoundingMode.DOWN, escala 2.
BigDecimal.valueOf() o new BigDecimal("...") — NUNCA new BigDecimal(double).
Si una regla de negocio necesita comparar o limitar un monto (p. ej. un tope) antes de truncarlo, separa el cálculo "exacto" (sin escala fija) del truncamiento final en métodos distintos — no los combines en un solo paso, o la regla del tope terminará comparando contra un valor ya truncado.

### Java 25
Records para value objects, sealed interfaces, pattern matching.
Sin Lombok — los records lo reemplazan.

### REST & Spring
Solo inyección por constructor (nada de @Autowired en campos).
@Valid en los request bodies. 201 al crear, 200 al consultar, 400 validación, 404 no encontrado.
Excepciones de dominio para violaciones de reglas de negocio. Mapear a HTTP solo en el controller.
Nunca tragar excepciones ni filtrar detalles de infraestructura.
Un caso de uso que ejecuta más de una escritura de persistencia (p. ej. actualizar dos repositorios distintos) debe marcarse @Transactional — si no, una falla a mitad de camino puede dejar el estado inconsistente.

## Estructura del proyecto

domain/ — lógica de negocio, modelos, puertos. Sin imports de Spring.
application/ — orquestación de casos de uso.
adapter/in/web/ — controllers REST (Spring MVC).
adapter/out/persistence/ — repositorios y entidades JPA.

NUNCA importar clases de adapter desde domain.

## Proceso de desarrollo - ATDD + TDD

Sigue estos pasos para cada feature. NO te saltes pasos.

### Paso 1: Descubrimiento
Ejecuta /discover.
Propón reglas, expón las preguntas con opciones, deja que el usuario decida.
Guarda el borrador del spec en docs/specs/.
DETENTE. El usuario revisa, edita y anota el spec.
NO continúes si el spec tiene preguntas sin resolver.
Relee el spec final antes de seguir.
El doc/specs es tu fuente de la verdad y debes sentirte delimitado por y respetar sus restricciones de comportamiento

### Paso 2: Test de aceptación ATDD (loop externo)
Escribe el test solo para la SIGUIENTE regla.
@Nested = regla, test = ejemplo. @SpringBootTest + MockMvc.
Completa el Paso 3 hasta que esta regla esté en VERDE antes de escribir la siguiente.

### Paso 3: TDD (loop interno)
RED → GREEN → REFACTOR.
Escribe UN test que falle. Código mínimo para pasarlo. Refactoriza.
Corre TODOS los tests. DETENTE después de cada ciclo.

### Paso 4: Revisión
Verifica cobertura, bordes, sin AI smells.
Actualiza CLAUDE.md si surgieron convenciones nuevas.

## Estandares del testing
Acceptance test live in:  .../acceptance/ - @SpringBootTest + MockMvc
- domain: .../domain/ - Plain JUnit + AssertJ, NO Spring
- repository: .../adapter/out/persistence - @DataJpaTest

Las pruebas son especificaciones ejecutables, no solo verificaciones.
Usa clases @Nested para agrupar — incluso en tests unitarios.
@DisplayName con lenguaje de negocio en cada clase y método.
Usa AssertJ para todas las aserciones.
Para dinero: isEqualByComparingTo("1.60").
Datos de prueba inline por test. Nada de fixtures compartidas.

## Arquitectura: Hexagonal (Puertos y Adaptadores)

Dominio (domain/): Java puro. SIN Spring, SIN dependencias de framework.
model/ — entidades y objetos de valor
service/ — reglas de negocio

Aplicación (application/): interfaces port/in/ y port/out/.
Solo orquestación con @Service — nada de lógica de negocio aquí.

Adaptadores:
adapter/in/web/ — @RestController, solo DTOs.
adapter/out/persistence/ — repos y entidades JPA (NO en el dominio).

El dominio NUNCA importa org.springframework ni jakarta.persistence.
Los controllers NUNCA contienen lógica de negocio.
Las dependencias fluyen hacia adentro: adapter → application → domain