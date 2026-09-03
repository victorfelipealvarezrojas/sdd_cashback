package com.val.cashbank.application;

import com.val.cashbank.application.port.in.RegistrarCompraUseCase.RegistrarCompraCommand;
import com.val.cashbank.application.port.in.RegistrarCompraUseCase.RegistrarCompraResult;
import com.val.cashbank.application.port.out.ComercioRepository;
import com.val.cashbank.application.port.out.SaldoRecompensasRepository;
import com.val.cashbank.application.port.out.TransaccionCashbackRepository;
import com.val.cashbank.domain.model.Comercio;
import com.val.cashbank.domain.model.SaldoRecompensas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Registrar compra")
class RegistrarCompraServiceTest {

    private final ComercioRepository comercioRepository = mock(ComercioRepository.class);
    private final SaldoRecompensasRepository saldoRecompensasRepository = mock(SaldoRecompensasRepository.class);
    private final TransaccionCashbackRepository transaccionCashbackRepository = mock(TransaccionCashbackRepository.class);
    private final RegistrarCompraService registrarCompraService =
            new RegistrarCompraService(comercioRepository, saldoRecompensasRepository, transaccionCashbackRepository);

    @BeforeEach
    void sinAcumuladoDelMesPorDefecto() {
        when(transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(any(), any()))
                .thenReturn(BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("Regla: debe calcular el cashback como un porcentaje del monto neto de la compra, usando la tasa de cashback del comercio")
    class CalculoDelCashback {

        @Test
        @DisplayName("El caso en que el comercio tiene tasa de cashback configurada, el resultado incluye el cashback ganado sobre el monto neto")
        void registrarCompraCalculaElCashbackGanado() {
            when(comercioRepository.buscarPorId("comercio-1"))
                    .thenReturn(Optional.of(new Comercio("comercio-1", new BigDecimal("2.00"))));

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-1", "comercio-1", new BigDecimal("100.00")));

            assertThat(resultado.cashbackGanado()).isEqualByComparingTo("2.00");
        }
    }

    @Nested
    @DisplayName("Regla: no debe generar cashback en compras de comercios sin una tasa de cashback configurada. Un comercio se considera \"asociado\" si y solo si tiene una tasa configurada")
    class ComercioSinTasaConfigurada {

        @Test
        @DisplayName("El caso en que el comercio no tiene ninguna tasa de cashback configurada (no está asociado), la compra no genera cashback, se calcula 0.00, sin lanzar excepción")
        void comercioSinTasaConfiguradaNoGeneraCashbackNiLanzaExcepcion() {
            when(comercioRepository.buscarPorId("comercio-sin-tasa")).thenReturn(Optional.empty());

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-3", "comercio-sin-tasa", new BigDecimal("100.00")));

            assertThat(resultado.cashbackGanado()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Regla: debe acreditar el cashback calculado al saldo de recompensas del cliente")
    class AcreditacionAlSaldoDeRecompensas {

        @Test
        @DisplayName("El caso en que el cliente tiene un saldo de recompensas actual de 50.00 y su compra genera un cashback de 5.00, el nuevo saldo es 55.00")
        void compraConSaldoPrevioAcreditaElCashbackAlSaldoExistente() {
            when(comercioRepository.buscarPorId("comercio-1"))
                    .thenReturn(Optional.of(new Comercio("comercio-1", new BigDecimal("2.00"))));
            when(saldoRecompensasRepository.buscarPorClienteId("cliente-saldo-1"))
                    .thenReturn(Optional.of(new SaldoRecompensas("cliente-saldo-1", new BigDecimal("50.00"))));

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-saldo-1", "comercio-1", new BigDecimal("250.00")));

            assertThat(resultado.cashbackGanado()).isEqualByComparingTo("5.00");
            assertThat(resultado.saldoRecompensas()).isEqualByComparingTo("55.00");
        }

        @Test
        @DisplayName("El caso en que es la primera compra del cliente y no existe saldo previo, se crea el saldo de recompensas con el valor del cashback generado")
        void primeraCompraDelClienteCreaElSaldoConElCashbackGenerado() {
            when(comercioRepository.buscarPorId("comercio-1"))
                    .thenReturn(Optional.of(new Comercio("comercio-1", new BigDecimal("2.00"))));
            when(saldoRecompensasRepository.buscarPorClienteId("cliente-saldo-nuevo"))
                    .thenReturn(Optional.empty());

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-saldo-nuevo", "comercio-1", new BigDecimal("100.00")));

            assertThat(resultado.saldoRecompensas()).isEqualByComparingTo("2.00");
        }
    }

    @Nested
    @DisplayName("Regla: debe limitar el cashback acumulado del cliente a un tope máximo mensual fijo de 100.00; una compra que exceda el tope solo acredita el remanente disponible hasta completarlo")
    class TopeMensualDeCashback {

        @Test
        @DisplayName("El caso en que el cliente acumuló 20.00 de cashback en el mes y la nueva compra genera un cashback exacto de 10.00, se acredita el monto completo porque no alcanza el tope")
        void compraQueNoAlcanzaElTopeAcreditaElCashbackCompleto() {
            when(comercioRepository.buscarPorId("comercio-1"))
                    .thenReturn(Optional.of(new Comercio("comercio-1", new BigDecimal("2.00"))));
            when(transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(eq("cliente-tope-1"), any()))
                    .thenReturn(new BigDecimal("20.00"));

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-tope-1", "comercio-1", new BigDecimal("500.00")));

            assertThat(resultado.cashbackGanado()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("El caso en que el cliente acumuló 97.00 de cashback en el mes y la nueva compra genera un cashback exacto de 8.00, solo se acredita el remanente de 3.00 hasta completar el tope")
        void compraQueSuperaElTopeAcreditaSoloElRemanente() {
            when(comercioRepository.buscarPorId("comercio-1"))
                    .thenReturn(Optional.of(new Comercio("comercio-1", new BigDecimal("2.00"))));
            when(transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(eq("cliente-tope-2"), any()))
                    .thenReturn(new BigDecimal("97.00"));

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-tope-2", "comercio-1", new BigDecimal("400.00")));

            assertThat(resultado.cashbackGanado()).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("El caso en que el cliente ya acumuló el tope de 100.00 en el mes, la nueva compra no acredita cashback adicional, se calcula 0.00")
        void compraDespuesDeAlcanzarElTopeNoAcreditaCashback() {
            when(comercioRepository.buscarPorId("comercio-1"))
                    .thenReturn(Optional.of(new Comercio("comercio-1", new BigDecimal("2.00"))));
            when(transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(eq("cliente-tope-3"), any()))
                    .thenReturn(new BigDecimal("100.00"));

            RegistrarCompraResult resultado = registrarCompraService.registrarCompra(
                    new RegistrarCompraCommand("cliente-tope-3", "comercio-1", new BigDecimal("250.00")));

            assertThat(resultado.cashbackGanado()).isEqualByComparingTo("0.00");
        }
    }
}
