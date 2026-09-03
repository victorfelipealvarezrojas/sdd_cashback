package com.val.cashbank.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "transaccion_cashback")
public class TransaccionCashbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clienteId;

    private BigDecimal cashbackGanado;

    private String mes;

    protected TransaccionCashbackEntity() {
    }

    public TransaccionCashbackEntity(String clienteId, BigDecimal cashbackGanado, String mes) {
        this.clienteId = clienteId;
        this.cashbackGanado = cashbackGanado;
        this.mes = mes;
    }

    public String getClienteId() {
        return clienteId;
    }

    public BigDecimal getCashbackGanado() {
        return cashbackGanado;
    }

    public String getMes() {
        return mes;
    }
}
