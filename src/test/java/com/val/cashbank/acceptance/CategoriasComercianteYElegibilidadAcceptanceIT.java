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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Categorías de comerciante y elegibilidad")
class CategoriasComercianteYElegibilidadAcceptanceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Regla: debe calcular el cashback usando la tasa de la categoría (MCC) de la transacción: Supermercado 2%, Combustible 1%, Por defecto 0.5%")
    class CalculoDeCashbackSegunCategoriaDeLaTransaccion {

        @Test
        @DisplayName("El caso en que la transacción tiene categoría Supermercado, se calcula el cashback con la tasa de 2%")
        void transaccionCategoriaSupermercadoAplicaTasaDeDosPorCiento() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-categoria-1",
                      "comercioId": "comercio-hipermercado",
                      "montoNeto": 100.00,
                      "categoriaComercio": "SUPERMERCADO"
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("2.00");
        }

        @Test
        @DisplayName("El caso en que el mismo comercio realiza otra transacción con categoría Combustible, se calcula el cashback con la tasa de 1% (la categoría es de la transacción, no del comercio)")
        void mismoComercioConCategoriaCombustibleAplicaTasaDeUnoPorCiento() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-categoria-2",
                      "comercioId": "comercio-hipermercado",
                      "montoNeto": 100.00,
                      "categoriaComercio": "COMBUSTIBLE"
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("1.00");
        }

        @Test
        @DisplayName("El caso en que la transacción tiene categoría Por defecto, se calcula el cashback con la tasa de 0.5%")
        void transaccionCategoriaPorDefectoAplicaTasaDeMedioPorCiento() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-categoria-3",
                      "comercioId": "comercio-hipermercado",
                      "montoNeto": 100.00,
                      "categoriaComercio": "POR_DEFECTO"
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("0.50");
        }

        @Test
        @DisplayName("El caso en que la transacción trae una categoría no reconocida por el sistema, se le aplica la tasa \"Por defecto\" de 0.5%, no un error ni 0.00")
        void categoriaNoReconocidaAplicaTasaPorDefectoSinError() throws Exception {
            String requestBody = """
                    {
                      "clienteId": "cliente-categoria-4",
                      "comercioId": "comercio-hipermercado",
                      "montoNeto": 100.00,
                      "categoriaComercio": "FARMACIA"
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

            assertThat(response.get("cashbackGanado").decimalValue()).isEqualByComparingTo("0.50");
        }
    }
}
