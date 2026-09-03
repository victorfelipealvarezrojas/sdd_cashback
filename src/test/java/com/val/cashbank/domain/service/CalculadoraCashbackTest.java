package com.val.cashbank.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Calculadora de cashback")
class CalculadoraCashbackTest {

    private final CalculadoraCashback calculadoraCashback = new CalculadoraCashback();

    @Nested
    @DisplayName("Regla: debe calcular el cashback como un porcentaje del monto neto de la compra, usando la tasa de cashback del comercio")
    class CalculoDelCashback {

        @Test
        @DisplayName("El caso en que el monto neto es 100.00 y la tasa del comercio es 2%, el cashback calculado es 2.00")
        void calculaElCashbackComoPorcentajeDelMontoNeto() {
            BigDecimal montoNeto = new BigDecimal("100.00");
            BigDecimal tasaCashback = new BigDecimal("2.00");

            BigDecimal cashback = calculadoraCashback.calcularExacto(montoNeto, tasaCashback);

            assertThat(cashback).isEqualByComparingTo("2.00");
        }

        @Test
        @DisplayName("El caso en que el monto neto es 33.33 y la tasa es 3%, calcularExacto devuelve el cashback sin truncar (0.9999), porque la Regla 5 necesita el valor exacto para compararlo contra el tope antes de truncar")
        void calcularExactoNoTruncaElResultado() {
            BigDecimal montoNeto = new BigDecimal("33.33");
            BigDecimal tasaCashback = new BigDecimal("3.00");

            BigDecimal cashbackExacto = calculadoraCashback.calcularExacto(montoNeto, tasaCashback);

            assertThat(cashbackExacto).isEqualByComparingTo("0.9999");
        }
    }

    @Nested
    @DisplayName("Regla: debe truncar el cashback calculado a 2 decimales sin redondear hacia arriba (RoundingMode.DOWN)")
    class TruncamientoDelCashback {

        @Test
        @DisplayName("El caso en que el cashback exacto es 0.9999, se trunca a 0.99")
        void truncaHaciaAbajoUnCashbackConFraccion() {
            BigDecimal cashback = calculadoraCashback.truncar(new BigDecimal("0.9999"));

            assertThat(cashback).isEqualByComparingTo("0.99");
        }

        @Test
        @DisplayName("El caso en que el cashback exacto es 0.0001, se trunca a 0.00 (contraejemplo: compra válida cuyo cashback trunca a cero)")
        void truncaACeroUnCashbackMuyPequeno() {
            BigDecimal cashback = calculadoraCashback.truncar(new BigDecimal("0.0001"));

            assertThat(cashback).isEqualByComparingTo("0.00");
        }
    }
}
