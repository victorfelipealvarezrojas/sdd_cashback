package com.val.cashbank.domain.model;

import java.math.BigDecimal;

public record SaldoRecompensas(String clienteId, BigDecimal monto) {

    public SaldoRecompensas acreditar(BigDecimal cashback) {
        return new SaldoRecompensas(clienteId, monto.add(cashback));
    }
}
