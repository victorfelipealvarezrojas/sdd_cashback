package com.val.cashbank.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CompraRequest(
        @NotBlank String clienteId,
        @NotBlank String comercioId,
        @NotNull BigDecimal montoNeto,
        String categoriaComercio
) {
}
