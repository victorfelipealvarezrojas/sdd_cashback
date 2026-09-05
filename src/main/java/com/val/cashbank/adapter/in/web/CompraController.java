package com.val.cashbank.adapter.in.web;

import com.val.cashbank.adapter.in.web.dto.CompraRequest;
import com.val.cashbank.adapter.in.web.dto.CompraResponse;
import com.val.cashbank.application.port.in.RegistrarCompraUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final RegistrarCompraUseCase registrarCompraUseCase;

    public CompraController(RegistrarCompraUseCase registrarCompraUseCase) {
        this.registrarCompraUseCase = registrarCompraUseCase;
    }

    @PostMapping
    public ResponseEntity<CompraResponse> registrarCompra(@Valid @RequestBody CompraRequest request) {
        RegistrarCompraUseCase.RegistrarCompraResult resultado = registrarCompraUseCase.registrarCompra(
                new RegistrarCompraUseCase.RegistrarCompraCommand(
                        request.clienteId(),
                        request.comercioId(),
                        request.montoNeto(),
                        request.categoriaComercio()
                )
        );

        CompraResponse response = new CompraResponse(
                resultado.clienteId(),
                resultado.comercioId(),
                resultado.montoNeto(),
                resultado.cashbackGanado(),
                resultado.saldoRecompensas()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
