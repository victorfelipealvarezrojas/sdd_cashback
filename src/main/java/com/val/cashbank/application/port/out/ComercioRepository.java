package com.val.cashbank.application.port.out;

import com.val.cashbank.domain.model.Comercio;

import java.util.Optional;

public interface ComercioRepository {

    Optional<Comercio> buscarPorId(String comercioId);
}
