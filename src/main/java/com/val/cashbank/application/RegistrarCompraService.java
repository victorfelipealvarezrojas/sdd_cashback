package com.val.cashbank.application;

import com.val.cashbank.application.port.in.RegistrarCompraUseCase;
import com.val.cashbank.application.port.out.ComercioRepository;
import com.val.cashbank.application.port.out.SaldoRecompensasRepository;
import com.val.cashbank.application.port.out.TransaccionCashbackRepository;
import com.val.cashbank.domain.model.CategoriaComercio;
import com.val.cashbank.domain.model.SaldoRecompensas;
import com.val.cashbank.domain.model.TransaccionCashback;
import com.val.cashbank.domain.service.CalculadoraCashback;
import com.val.cashbank.domain.service.TopeCashbackMensual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
public class RegistrarCompraService implements RegistrarCompraUseCase {

    private final ComercioRepository comercioRepository;
    private final SaldoRecompensasRepository saldoRecompensasRepository;
    private final TransaccionCashbackRepository transaccionCashbackRepository;
    private final CalculadoraCashback calculadoraCashback = new CalculadoraCashback();
    private final TopeCashbackMensual topeCashbackMensual = new TopeCashbackMensual();

    public RegistrarCompraService(ComercioRepository comercioRepository,
                                   SaldoRecompensasRepository saldoRecompensasRepository,
                                   TransaccionCashbackRepository transaccionCashbackRepository) {
        this.comercioRepository = comercioRepository;
        this.saldoRecompensasRepository = saldoRecompensasRepository;
        this.transaccionCashbackRepository = transaccionCashbackRepository;
    }

    @Override
    @Transactional
    public RegistrarCompraResult registrarCompra(RegistrarCompraCommand comando) {
        BigDecimal cashbackExacto = comando.categoriaComercio() != null
                ? calculadoraCashback.calcularExacto(comando.montoNeto(),
                        CategoriaComercio.desde(comando.categoriaComercio()).tasaCashback())
                : comercioRepository.buscarPorId(comando.comercioId())
                        .map(comercio -> calculadoraCashback.calcularExacto(comando.montoNeto(), comercio.tasaCashback()))
                        .orElse(BigDecimal.ZERO);

        YearMonth mesActual = YearMonth.now();
        BigDecimal acumuladoDelMes = transaccionCashbackRepository.sumarCashbackAcreditadoDelMes(
                comando.clienteId(), mesActual);

        BigDecimal cashbackLimitado = topeCashbackMensual.aplicar(cashbackExacto, acumuladoDelMes);
        BigDecimal cashbackGanado = calculadoraCashback.truncar(cashbackLimitado);

        SaldoRecompensas saldoActualizado = saldoRecompensasRepository.buscarPorClienteId(comando.clienteId())
                .map(saldo -> saldo.acreditar(cashbackGanado))
                .orElse(new SaldoRecompensas(comando.clienteId(), cashbackGanado));

        saldoRecompensasRepository.guardar(saldoActualizado);
        transaccionCashbackRepository.guardar(
                new TransaccionCashback(comando.clienteId(), cashbackGanado, mesActual));

        return new RegistrarCompraResult(comando.clienteId(), comando.comercioId(), comando.montoNeto(),
                cashbackGanado, saldoActualizado.monto());
    }
}
