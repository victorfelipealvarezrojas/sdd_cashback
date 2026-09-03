package com.val.cashbank.adapter.out.persistence;

import com.val.cashbank.application.port.out.ComercioRepository;
import com.val.cashbank.domain.model.Comercio;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryComercioRepository implements ComercioRepository {

    private final Map<String, Comercio> comercios = Map.of(
            "comercio-1", new Comercio("comercio-1", new BigDecimal("2.00")),
            "comercio-a", new Comercio("comercio-a", new BigDecimal("3.00")),
            "comercio-tasa-3", new Comercio("comercio-tasa-3", new BigDecimal("3.00")),
            "comercio-tasa-7-5", new Comercio("comercio-tasa-7-5", new BigDecimal("7.50"))
    );

    @Override
    public Optional<Comercio> buscarPorId(String comercioId) {
        return Optional.ofNullable(comercios.get(comercioId));
    }
}
