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
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Detalle y total de cashback mensual")
class DetalleYTotalCashbackMensualAcceptanceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Regla: debe mostrar el detalle de cashback del cliente para un mes dado, con una línea por cada compra que participó en el cálculo de cashback, incluyendo la información suficiente para verificar el cálculo (monto neto, tasa del comercio aplicada en el momento de la compra, cashback acreditado)")
    class DetalleDeCashbackDelMes {

        @Test
        @DisplayName("El caso en que el cliente tiene 2 compras en el mes, en comercios con tasas distintas, el detalle muestra 2 líneas con monto neto, tasa aplicada y cashback acreditado, ordenadas de la más antigua a la más reciente")
        void detalleMuestraUnaLineaPorCadaCompraConSuCalculoVerificable() throws Exception {
            String clienteId = "cliente-detalle-1";
            String mesActual = YearMonth.now().toString();

            registrarCompra(clienteId, "comercio-1", new BigDecimal("100.00"));
            registrarCompra(clienteId, "comercio-2", new BigDecimal("200.00"));

            MvcResult result = mockMvc.perform(get("/api/clientes/{clienteId}/cashback", clienteId)
                            .param("mes", mesActual))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode detalle = response.get("detalle");

            assertThat(detalle).as("detalle no debe ser null").isNotNull();
            assertThat(detalle.size()).isEqualTo(2);

            JsonNode primeraLinea = detalle.get(0);
            assertThat(primeraLinea.get("comercioId").asText()).isEqualTo("comercio-1");
            assertThat(primeraLinea.get("montoNeto").decimalValue()).isEqualByComparingTo("100.00");
            assertThat(primeraLinea.get("tasaCashback").decimalValue()).isEqualByComparingTo("2.00");
            assertThat(primeraLinea.get("cashbackGanado").decimalValue()).isEqualByComparingTo("2.00");
            assertThat(primeraLinea.get("limitadoPorTope").asBoolean()).isFalse();

            JsonNode segundaLinea = detalle.get(1);
            assertThat(segundaLinea.get("comercioId").asText()).isEqualTo("comercio-2");
            assertThat(segundaLinea.get("montoNeto").decimalValue()).isEqualByComparingTo("200.00");
            assertThat(segundaLinea.get("tasaCashback").decimalValue()).isEqualByComparingTo("2.34");
            assertThat(segundaLinea.get("cashbackGanado").decimalValue()).isEqualByComparingTo("4.68");
            assertThat(segundaLinea.get("limitadoPorTope").asBoolean()).isFalse();
        }

        @Test
        @DisplayName("El caso en que una compra alcanza el tope mensual y su cashback acreditado es menor al que resultaría de monto neto × tasa, el detalle marca esa línea como limitada por el tope")
        void detalleMarcaComoLimitadaPorTopeLaCompraQueSuperaElTopeMensual() throws Exception {
            String clienteId = "cliente-detalle-tope";
            String mesActual = YearMonth.now().toString();

            registrarCompra(clienteId, "comercio-alto-cashback", new BigDecimal("970.00"));
            registrarCompra(clienteId, "comercio-alto-cashback", new BigDecimal("80.00"));

            MvcResult result = mockMvc.perform(get("/api/clientes/{clienteId}/cashback", clienteId)
                            .param("mes", mesActual))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode detalle = response.get("detalle");

            assertThat(detalle).as("detalle no debe ser null").isNotNull();
            assertThat(detalle.size()).isEqualTo(2);

            JsonNode compraSinLimite = detalle.get(0);
            assertThat(compraSinLimite.get("cashbackGanado").decimalValue()).isEqualByComparingTo("97.00");
            assertThat(compraSinLimite.get("limitadoPorTope").asBoolean()).isFalse();

            JsonNode compraLimitadaPorTope = detalle.get(1);
            assertThat(compraLimitadaPorTope.get("montoNeto").decimalValue()).isEqualByComparingTo("80.00");
            assertThat(compraLimitadaPorTope.get("tasaCashback").decimalValue()).isEqualByComparingTo("10.00");
            assertThat(compraLimitadaPorTope.get("cashbackGanado").decimalValue())
                    .as("el cashback acreditado debe ser el remanente hasta el tope (3.00), no el exacto (8.00)")
                    .isEqualByComparingTo("3.00");
            assertThat(compraLimitadaPorTope.get("limitadoPorTope").asBoolean()).isTrue();
        }

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
    }
}
