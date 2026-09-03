---
paths:
  - "src/main/java/**/application/**"
---

Estás editando código de la capa de aplicación (orquestación de casos de uso).

Solo orquestación — nada de lógica de negocio aquí (eso vive en domain/).
Los conceptos de negocio (comercios, compras, clientes...) se nombran en español, siguiendo el lenguaje de negocio de la spec.

Nomenclatura de los puertos de entrada (port/in) — sufijos técnicos SIEMPRE en inglés, aunque el resto del nombre esté en español. NUNCA traduzcas estos sufijos:
- Casos de uso: `UseCase` (ej. `RegistrarCompraUseCase`).
- Comandos de escritura/mutación: `Command`, nunca "Comando" (ej. `RegistrarCompraCommand`, no `RegistrarCompraComando`).
- Consultas de lectura: `Query`, nunca "Consulta" (ej. `ConsultarCashbackMensualQuery`, no `ConsultarCashbackMensualConsulta`).
- Resultados de un caso de uso: `Result`, nunca "Resultado" (ej. `RegistrarCompraResult`, no `RegistrarCompraResultado`).

Constructor injection únicamente — nada de @Autowired en campos.
