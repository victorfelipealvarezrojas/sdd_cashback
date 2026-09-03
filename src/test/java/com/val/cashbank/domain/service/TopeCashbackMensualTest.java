package com.val.cashbank.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tope mensual de cashback")
class TopeCashbackMensualTest {

    private final TopeCashbackMensual topeCashbackMensual = new TopeCashbackMensual();

    @Nested
    @DisplayName("Regla: debe limitar el cashback acumulado del cliente a un tope máximo mensual fijo de 100.00; una compra que exceda el tope solo acredita el remanente disponible hasta completarlo")
    class LimiteDelTopeMensual {

        @Test
        @DisplayName("El caso en que el cliente acumuló 20.00 de cashback en el mes y la nueva compra genera un cashback exacto de 10.00, se acredita el monto completo porque no alcanza el tope")
        void noLimitaCuandoElAcumuladoMasElExactoNoAlcanzaElTope() {
            BigDecimal cashbackLimitado = topeCashbackMensual.aplicar(new BigDecimal("10.00"), new BigDecimal("20.00"));

            assertThat(cashbackLimitado).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("El caso en que el cliente acumuló 97.00 de cashback en el mes y la nueva compra genera un cashback exacto de 8.00, solo se acredita el remanente de 3.00 hasta completar el tope")
        void limitaAlRemanenteCuandoElExactoSuperaElTope() {
            BigDecimal cashbackLimitado = topeCashbackMensual.aplicar(new BigDecimal("8.00"), new BigDecimal("97.00"));

            assertThat(cashbackLimitado).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("El caso en que el cliente ya acumuló el tope de 100.00 en el mes, la nueva compra no acredita cashback adicional, se calcula 0.00")
        void noAcreditaNadaCuandoYaSeAlcanzoElTope() {
            BigDecimal cashbackLimitado = topeCashbackMensual.aplicar(new BigDecimal("5.00"), new BigDecimal("100.00"));

            assertThat(cashbackLimitado).isEqualByComparingTo("0.00");
        }
    }
}
