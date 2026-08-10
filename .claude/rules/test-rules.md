---
paths:
  - "src/test/java/**"
---

Estás editando código de pruebas.

Las pruebas son especificaciones ejecutables.
@DisplayName en cada clase y método.
Usa @Nested para agrupar pruebas relacionadas.

# Convenciones de nombres (buena práctica de Maven)

Tests de aceptación → *IT (tests de integración, se ejecutan durante mvn verify).
Todos los demás tests → *Test (tests unitarios, se ejecutan durante mvn test).
Aceptación = @SpringBootTest + MockMvc