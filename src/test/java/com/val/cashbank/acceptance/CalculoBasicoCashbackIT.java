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
}
