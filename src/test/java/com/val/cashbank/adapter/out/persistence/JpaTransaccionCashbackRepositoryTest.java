package com.val.cashbank.adapter.out.persistence;

import com.val.cashbank.domain.model.TransaccionCashback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Repositorio de transacciones de cashback")
class JpaTransaccionCashbackRepositoryTest {

    @Autowired
    private TransaccionCashbackJpaRepository transaccionCashbackJpaRepository;

    private JpaTransaccionCashbackRepository transaccionCashbackRepository;

    @BeforeEach
    void setUp() {
        transaccionCashbackRepository = new JpaTransaccionCashbackRepository(transaccionCashbackJpaRepository);
    }

    @Nested
    @DisplayName("Regla: debe limitar el cashback acumulado del cliente a un tope máximo mensual fijo de 100.00; una compra que exceda el tope solo acredita el remanente disponible hasta completarlo")
    class LimiteDelTopeMensual {

        @Test
        @DisplayName("El caso en que el cliente no tiene transacciones en el mes, sumarCashbackAcreditadoDelMes devuelve 0.00")
        void sumarCashbackAcreditadoDelMesDevuelveCeroSinTransacciones() {
            BigDecimal acumulado = transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(
                    "cliente-sin-transacciones", YearMonth.now());

            assertThat(acumulado).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("El caso en que el cliente acumuló 20.00 y 10.00 en dos compras del mes, sumarCashbackAcreditadoDelMes devuelve 30.00")
        void sumarCashbackAcreditadoDelMesSumaLasTransaccionesDelMes() {
            YearMonth mesActual = YearMonth.now();
            transaccionCashbackRepository.guardar(
                    new TransaccionCashback("cliente-1", new BigDecimal("20.00"), mesActual));
            transaccionCashbackRepository.guardar(
                    new TransaccionCashback("cliente-1", new BigDecimal("10.00"), mesActual));

            BigDecimal acumulado = transaccionCashbackRepository.sumarCashbackAcreditadoDelMes("cliente-1", mesActual);

            assertThat(acumulado).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("El caso en que el cliente tiene una transacción de un mes distinto al consultado, sumarCashbackAcreditadoDelMes no la incluye")
        void sumarCashbackAcreditadoDelMesNoIncluyeTransaccionesDeOtroMes() {
            YearMonth mesAnterior = YearMonth.now().minusMonths(1);
            transaccionCashbackRepository.guardar(
                    new TransaccionCashback("cliente-2", new BigDecimal("50.00"), mesAnterior));

            BigDecimal acumulado = transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(
                    "cliente-2", YearMonth.now());

            assertThat(acumulado).isEqualByComparingTo("0.00");
        }
    }
}
