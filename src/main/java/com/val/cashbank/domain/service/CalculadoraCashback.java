package com.val.cashbank.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraCashback {

    public BigDecimal calcularExacto(BigDecimal montoNeto, BigDecimal tasaCashback) {
        return montoNeto.multiply(tasaCashback).movePointLeft(2);
    }

    public BigDecimal truncar(BigDecimal cashback) {
        return cashback.setScale(2, RoundingMode.DOWN);
    }
}
