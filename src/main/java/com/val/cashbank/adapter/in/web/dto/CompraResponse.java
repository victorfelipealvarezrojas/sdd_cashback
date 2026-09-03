package com.val.cashbank.adapter.in.web.dto;

import java.math.BigDecimal;

public record CompraResponse(
        String clienteId,
        String comercioId,
        BigDecimal montoNeto,
        BigDecimal cashbackGanado,
        BigDecimal saldoRecompensas
) {
}
