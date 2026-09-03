package com.val.cashbank.adapter.out.persistence;

import com.val.cashbank.domain.model.SaldoRecompensas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Repositorio de saldo de recompensas")
class JpaSaldoRecompensasRepositoryTest {

    @Autowired
    private SaldoRecompensasJpaRepository saldoRecompensasJpaRepository;

    private JpaSaldoRecompensasRepository saldoRecompensasRepository;

    @BeforeEach
    void setUp() {
        saldoRecompensasRepository = new JpaSaldoRecompensasRepository(saldoRecompensasJpaRepository);
    }

    @Nested
    @DisplayName("Regla: debe acreditar el cashback calculado al saldo de recompensas del cliente")
    class AcreditacionAlSaldoDeRecompensas {

        @Test
        @DisplayName("El caso en que es la primera compra del cliente y no existe saldo previo, buscarPorClienteId no devuelve ningún saldo")
        void buscarPorClienteIdDevuelveVacioSiNoHaySaldoPrevio() {
            Optional<SaldoRecompensas> saldo = saldoRecompensasRepository.buscarPorClienteId("cliente-sin-saldo");

            assertThat(saldo).isEmpty();
        }

        @Test
        @DisplayName("El caso en que se guarda un saldo de recompensas, buscarPorClienteId lo devuelve con el mismo monto")
        void guardarPersisteElSaldoYBuscarPorClienteIdLoRecupera() {
            saldoRecompensasRepository.guardar(new SaldoRecompensas("cliente-1", new BigDecimal("55.00")));

            Optional<SaldoRecompensas> saldo = saldoRecompensasRepository.buscarPorClienteId("cliente-1");

            assertThat(saldo).isPresent();
            assertThat(saldo.get().monto()).isEqualByComparingTo("55.00");
        }
    }
}
