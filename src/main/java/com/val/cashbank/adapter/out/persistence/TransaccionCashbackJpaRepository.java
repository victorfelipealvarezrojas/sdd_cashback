package com.val.cashbank.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionCashbackJpaRepository extends JpaRepository<TransaccionCashbackEntity, Long> {

    List<TransaccionCashbackEntity> findByClienteIdAndMes(String clienteId, String mes);
}
