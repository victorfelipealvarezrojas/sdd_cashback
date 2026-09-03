package com.val.cashbank.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "saldo_recompensas")
public class SaldoRecompensasEntity {

    @Id
    private String clienteId;

    private BigDecimal monto;

    protected SaldoRecompensasEntity() {
    }

    public SaldoRecompensasEntity(String clienteId, BigDecimal monto) {
        this.clienteId = clienteId;
        this.monto = monto;
    }

    public String getClienteId() {
        return clienteId;
    }

    public BigDecimal getMonto() {
        return monto;
    }
}
