package com.val.cashbank.application.port.out;

import com.val.cashbank.domain.model.SaldoRecompensas;

import java.util.Optional;

public interface SaldoRecompensasRepository {

    Optional<SaldoRecompensas> buscarPorClienteId(String clienteId);

    void guardar(SaldoRecompensas saldo);
}
