---
paths:
  - "src/main/java/**/adapter/in/web/**"
---

Estás editando código del adaptador web.

Los controllers son FINOS — delegan de inmediato en los servicios de aplicación.
NUNCA pongas lógica de negocio en los controllers.
Usa DTOs (records), no tipos de dominio, sobre HTTP.
@Valid en todos los request bodies.
Prueba con @WebMvcTest (un controller).