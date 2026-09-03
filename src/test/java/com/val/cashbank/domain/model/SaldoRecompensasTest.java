package com.val.cashbank.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Saldo de recompensas")
class SaldoRecompensasTest {

    @Nested
    @DisplayName("Regla: debe acreditar el cashback calculado al saldo de recompensas del cliente")
    class AcreditacionAlSaldoDeRecompensas {

        @Test
        @DisplayName("El caso en que el cliente tiene un saldo de recompensas actual de 50.00 y su compra genera un cashback de 5.00, el nuevo saldo es 55.00")
        void acreditarSumaElCashbackAlSaldoExistente() {
            SaldoRecompensas saldoActual = new SaldoRecompensas("cliente-1", new BigDecimal("50.00"));

            SaldoRecompensas saldoActualizado = saldoActual.acreditar(new BigDecimal("5.00"));

            assertThat(saldoActualizado.monto()).isEqualByComparingTo("55.00");
        }
    }
}
