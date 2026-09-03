package com.val.cashbank.adapter.in.web;

import com.val.cashbank.application.port.in.RegistrarCompraUseCase;
import com.val.cashbank.application.port.in.RegistrarCompraUseCase.RegistrarCompraCommand;
import com.val.cashbank.application.port.in.RegistrarCompraUseCase.RegistrarCompraResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompraController.class)
@DisplayName("Controller de compras")
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarCompraUseCase registrarCompraUseCase;

    @Nested
    @DisplayName("Regla: debe calcular el cashback como un porcentaje del monto neto de la compra, usando la tasa de cashback del comercio")
    class CalculoDelCashback {

        @Test
        @DisplayName("El caso en que se registra una compra válida, responde 201 con el cashback ganado devuelto por el caso de uso")
        void registrarCompraRespondeConElCashbackGanado() throws Exception {
            when(registrarCompraUseCase.registrarCompra(
                    new RegistrarCompraCommand("cliente-1", "comercio-1", new BigDecimal("100.00"))))
                    .thenReturn(new RegistrarCompraResult(
                            "cliente-1", "comercio-1", new BigDecimal("100.00"), new BigDecimal("2.00"),
                            new BigDecimal("2.00")));

            String requestBody = """
                    {
                      "clienteId": "cliente-1",
                      "comercioId": "comercio-1",
                      "montoNeto": 100.00
                    }
                    """;

            mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cashbackGanado").value(2.00));
        }
    }

    @Nested
    @DisplayName("Regla: debe acreditar el cashback calculado al saldo de recompensas del cliente")
    class AcreditacionAlSaldoDeRecompensas {

        @Test
        @DisplayName("El caso en que se registra una compra válida, responde 201 con el saldo de recompensas actualizado devuelto por el caso de uso")
        void registrarCompraRespondeConElSaldoDeRecompensas() throws Exception {
            when(registrarCompraUseCase.registrarCompra(
                    new RegistrarCompraCommand("cliente-saldo-1", "comercio-1", new BigDecimal("250.00"))))
                    .thenReturn(new RegistrarCompraResult(
                            "cliente-saldo-1", "comercio-1", new BigDecimal("250.00"), new BigDecimal("5.00"),
                            new BigDecimal("55.00")));

            String requestBody = """
                    {
                      "clienteId": "cliente-saldo-1",
                      "comercioId": "comercio-1",
                      "montoNeto": 250.00
                    }
                    """;

            mockMvc.perform(post("/api/compras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.saldoRecompensas").value(55.00));
        }
    }
}
