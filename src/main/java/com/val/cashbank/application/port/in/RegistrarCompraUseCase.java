package com.val.cashbank.application.port.in;

import java.math.BigDecimal;

public interface RegistrarCompraUseCase {

    RegistrarCompraResult registrarCompra(RegistrarCompraCommand comando);

    record RegistrarCompraCommand(String clienteId, String comercioId, BigDecimal montoNeto, String categoriaComercio) {

        public RegistrarCompraCommand(String clienteId, String comercioId, BigDecimal montoNeto) {
            this(clienteId, comercioId, montoNeto, null);
        }
    }

    record RegistrarCompraResult(String clienteId, String comercioId, BigDecimal montoNeto,
                                  BigDecimal cashbackGanado, BigDecimal saldoRecompensas) {
    }
}
