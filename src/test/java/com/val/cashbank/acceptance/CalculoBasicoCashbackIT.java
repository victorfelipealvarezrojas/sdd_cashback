package com.val.cashbank.acceptance;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Cálculo básico de cashback")
class CalculoBasicoCashbackIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void registrarCompra(String clienteId, String comercioId, BigDecimal montoNeto) throws Exception {
        String requestBody = """
                {
                  "clienteId": "%s",
                  "comercioId": "%s",
                  "montoNeto": %s
                }
                """.formatted(clienteId, comercioId, montoNeto);

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Nested
    @DisplayName("Regla: debe calcular el cashback como un porcentaje del monto neto de la compra, usando la tasa de cashback del comercio")
    class CalculoDelCashback {

        @Test
        @DisplayName("El caso en que un comercio con tasa de cashback configurada recibe una compra, el cliente gana cashback como el porcentaje del monto neto")
        void clienteGanaCashbackComoPorcentajeDelMontoNeto() throws Exception {
            BigDecimal tasaCashbackComercio = new BigDecimal("2.00");
            BigDecimal montoNeto = new BigDecimal("100.00");
            BigDecimal cashbackEsperado = montoNeto
                    .multiply(tasaCashbackComercio)
                    .movePointLeft(2);

            String requestBody = """
                    {
                      "clienteId": "cliente-1",
                      "comercioId": "comercio-1",
                      "montoNeto": %s
                    }
                    """.formatted(montoNeto);

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode cashbackGanadoNode = response.get("cashbackGanado");

            assertThat(cashbackGanadoNode).as("cashbackGanado no debe ser null").isNotNull();
            assertThat(cashbackGanadoNode.decimalValue()).isEqualByComparingTo(cashbackEsperado);
        }
    }

    @Nested
    @DisplayName("Regla: no debe generar cashback en compras de comercios sin una tasa de cashback configurada. Un comercio se considera \"asociado\" si y solo si tiene una tasa configurada")
    class ComercioSinTasaConfigurada {

        @Test
        @DisplayName("El caso en que el comercio \"Comercio A\" tiene una tasa configurada de 3% y el cliente compra por 100.00, se calcula un cashback de 3.00")
        void comercioConTasaConfiguradaEstaAsociadoYGeneraCashback() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-2",
                      "comercioId": "comercio-a",
                      "montoNeto": 100.00
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("El caso en que el comercio no tiene ninguna tasa de cashback configurada (no está asociado), la compra no genera cashback, se calcula 0.00, sin lanzar excepción")
        void comercioSinTasaConfiguradaNoGeneraCashbackNiLanzaExcepcion() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-3",
                      "comercioId": "comercio-sin-tasa",
                      "montoNeto": 100.00
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Regla: debe truncar el cashback calculado a 2 decimales sin redondear hacia arriba (RoundingMode.DOWN)")
    class TruncamientoDelCashback {

        @Test
        @DisplayName("El caso en que el monto neto es 33.33 y la tasa de cashback del comercio es 3%, el cashback exacto 0.9999 se acredita truncado a 0.99")
        void cashbackConFraccionSeTruncaHaciaAbajo() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-4",
                      "comercioId": "comercio-tasa-3",
                      "montoNeto": 33.33
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("0.99");
        }

        @Test
        @DisplayName("El caso en que el monto neto es 10.00 y la tasa de cashback del comercio es 7.5%, el cashback exacto 0.75 se acredita sin cambios")
        void cashbackExactoEnDosDecimalesNoCambiaAlTruncar() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-5",
                      "comercioId": "comercio-tasa-7-5",
                      "montoNeto": 10.00
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("0.75");
        }
    }

    @Nested
    @DisplayName("Regla: debe acreditar el cashback calculado al saldo de recompensas del cliente")
    class AcreditacionAlSaldoDeRecompensas {

        @Test
        @DisplayName("El caso en que el cliente tiene un saldo de recompensas actual de 50.00 y su compra genera un cashback de 5.00, el nuevo saldo es 55.00")
        void compraConSaldoPrevioAcreditaElCashbackAlSaldoExistente() throws Exception {
            String clienteId = "cliente-saldo-1";

            registrarCompra(clienteId, "comercio-1", new BigDecimal("2500.00"));

            String requestBody = """
                    {
                      "clienteId": "%s",
                      "comercioId": "comercio-1",
                      "montoNeto": 250.00
                    }
                    """.formatted(clienteId);

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("5.00");
            assertThat(response.get("saldoRecompensas")).as("saldoRecompensas no debe ser null").isNotNull();
            assertThat(response.get("saldoRecompensas").decimalValue()).isEqualByComparingTo("55.00");
        }

        @Test
        @DisplayName("El caso en que es la primera compra del cliente y no existe saldo previo, se crea el saldo de recompensas con el valor del cashback generado")
        void primeraCompraDelClienteCreaElSaldoConElCashbackGenerado() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-saldo-nuevo",
                      "comercioId": "comercio-1",
                      "montoNeto": 100.00
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("saldoRecompensas")).as("saldoRecompensas no debe ser null").isNotNull();
            assertThat(response.get("saldoRecompensas").decimalValue()).isEqualByComparingTo("2.00");
        }
    }

    @Nested
    @DisplayName("Regla: debe limitar el cashback acumulado del cliente a un tope máximo mensual fijo de 100.00; una compra que exceda el tope solo acredita el remanente disponible hasta completarlo")
    class TopeMensualDeCashback {

        @Test
        @DisplayName("El caso en que el cliente acumuló 20.00 de cashback en el mes y la nueva compra genera un cashback exacto de 10.00, se acredita el monto completo porque no alcanza el tope")
        void compraQueNoAlcanzaElTopeAcreditaElCashbackCompleto() throws Exception {
            String clienteId = "cliente-tope-1";

            registrarCompra(clienteId, "comercio-1", new BigDecimal("1000.00"));

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "clienteId": "%s",
                                      "comercioId": "comercio-1",
                                      "montoNeto": 500.00
                                    }
                                    """.formatted(clienteId)))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("El caso en que el cliente acumuló 97.00 de cashback en el mes y la nueva compra genera un cashback exacto de 8.00, solo se acredita el remanente de 3.00 hasta completar el tope")
        void compraQueSuperaElTopeAcreditaSoloElRemanente() throws Exception {
            String clienteId = "cliente-tope-2";

            registrarCompra(clienteId, "comercio-1", new BigDecimal("4850.00"));

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "clienteId": "%s",
                                      "comercioId": "comercio-1",
                                      "montoNeto": 400.00
                                    }
                                    """.formatted(clienteId)))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("El caso en que el cliente ya acumuló el tope de 100.00 en el mes, la nueva compra no acredita cashback adicional, se calcula 0.00")
        void compraDespuesDeAlcanzarElTopeNoAcreditaCashback() throws Exception {
            String clienteId = "cliente-tope-3";

            registrarCompra(clienteId, "comercio-1", new BigDecimal("5000.00"));

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "clienteId": "%s",
                                      "comercioId": "comercio-1",
                                      "montoNeto": 250.00
                                    }
                                    """.formatted(clienteId)))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("0.00");
        }
    }
}
