package com.val.cashbank.domain.model;

import java.math.BigDecimal;

public enum CategoriaComercio {

    SUPERMERCADO(new BigDecimal("2.00")),
    COMBUSTIBLE(new BigDecimal("1.00")),
    POR_DEFECTO(new BigDecimal("0.50"));

    private final BigDecimal tasaCashback;

    CategoriaComercio(BigDecimal tasaCashback) {
        this.tasaCashback = tasaCashback;
    }

    public BigDecimal tasaCashback() {
        return tasaCashback;
    }

    public static CategoriaComercio desde(String codigo) {
        if (codigo == null) {
            return POR_DEFECTO;
        }
        try {
            return CategoriaComercio.valueOf(codigo);
        } catch (IllegalArgumentException e) {
            return POR_DEFECTO;
        }
    }
}
