package com.val.cashbank.adapter.out.persistence;

import com.val.cashbank.application.port.out.SaldoRecompensasRepository;
import com.val.cashbank.domain.model.SaldoRecompensas;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaSaldoRecompensasRepository implements SaldoRecompensasRepository {

    private final SaldoRecompensasJpaRepository jpaRepository;

    public JpaSaldoRecompensasRepository(SaldoRecompensasJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SaldoRecompensas> buscarPorClienteId(String clienteId) {
        return jpaRepository.findById(clienteId)
                .map(entity -> new SaldoRecompensas(entity.getClienteId(), entity.getMonto()));
    }

    @Override
    public void guardar(SaldoRecompensas saldo) {
        jpaRepository.save(new SaldoRecompensasEntity(saldo.clienteId(), saldo.monto()));
    }
}
