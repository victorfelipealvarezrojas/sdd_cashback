---
paths:
  - "src/main/java/**/domain/**"
---

Estás editando código de la capa de dominio.

NUNCA importes org.springframework.
NUNCA importes jakarta.persistence.

Solo lógica de negocio — Java puro.
Usa records para los objetos de valor.
BigDecimal para TODOS los valores monetarios.
Prueba con JUnit puro + AssertJ.
