package com.val.cashbank.adapter.out.persistence;

import com.val.cashbank.application.port.out.TransaccionCashbackRepository;
import com.val.cashbank.domain.model.TransaccionCashback;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;

@Repository
public class JpaTransaccionCashbackRepository implements TransaccionCashbackRepository {

    private final TransaccionCashbackJpaRepository jpaRepository;

    public JpaTransaccionCashbackRepository(TransaccionCashbackJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BigDecimal sumarCashbackAcreditadoDelMes(String clienteId, YearMonth mes) {
        return jpaRepository.findByClienteIdAndMes(clienteId, mes.toString()).stream()
                .map(TransaccionCashbackEntity::getCashbackGanado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void guardar(TransaccionCashback transaccion) {
        jpaRepository.save(new TransaccionCashbackEntity(
                transaccion.clienteId(), transaccion.cashbackGanado(), transaccion.mes().toString()));
    }
}
