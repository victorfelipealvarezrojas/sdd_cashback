package com.val.cashbank.application.port.out;

import com.val.cashbank.domain.model.TransaccionCashback;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface TransaccionCashbackRepository {

    BigDecimal sumarCashbackAcreditadoDelMes(String clienteId, YearMonth mes);

    void guardar(TransaccionCashback transaccion);
}
