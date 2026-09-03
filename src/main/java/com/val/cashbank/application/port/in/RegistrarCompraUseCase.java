package com.val.cashbank.application.port.in;

import java.math.BigDecimal;

public interface RegistrarCompraUseCase {

    RegistrarCompraResult registrarCompra(RegistrarCompraCommand comando);

    record RegistrarCompraCommand(String clienteId, String comercioId, BigDecimal montoNeto) {
    }

    record RegistrarCompraResult(String clienteId, String comercioId, BigDecimal montoNeto,
                                  BigDecimal cashbackGanado, BigDecimal saldoRecompensas) {
    }
}
