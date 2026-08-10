---
paths:
  - "src/main/java/**/adapter/out/persistence/**"
---

Estás editando código del adaptador de persistencia.

Las entidades JPA viven AQUÍ, no en el dominio.
Mapea los objetos de dominio hacia/desde entidades JPA.
Implementa los puertos de salida de application/.
NUNCA expongas entidades JPA fuera de esta capa.
Prueba con @DataJpaTest.
Usa Testcontainers para los tests de integración.