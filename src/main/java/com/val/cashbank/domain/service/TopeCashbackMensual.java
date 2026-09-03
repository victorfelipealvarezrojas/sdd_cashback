package com.val.cashbank.domain.service;

import java.math.BigDecimal;

public class TopeCashbackMensual {

    private static final BigDecimal TOPE = new BigDecimal("100.00");

    public BigDecimal aplicar(BigDecimal cashbackExacto, BigDecimal acumuladoDelMes) {
        BigDecimal remanente = TOPE.subtract(acumuladoDelMes).max(BigDecimal.ZERO);
        return cashbackExacto.min(remanente);
    }
}
