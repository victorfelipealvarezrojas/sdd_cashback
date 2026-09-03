package com.val.cashbank.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record TransaccionCashback(String clienteId, BigDecimal cashbackGanado, YearMonth mes) {
}
