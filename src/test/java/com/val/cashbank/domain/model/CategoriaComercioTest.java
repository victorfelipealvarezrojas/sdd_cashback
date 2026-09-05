package com.val.cashbank.domain.model;

import com.val.cashbank.domain.service.CalculadoraCashback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Categoría de comercio")
class CategoriaComercioTest {

    private final CalculadoraCashback calculadoraCashback = new CalculadoraCashback();

    @Nested
    @DisplayName("Regla: debe calcular el cashback usando la tasa de la categoría del comercio: Supermercado 2%, Combustible 1%, Por defecto 0.5%")
    class CalculoDeCashbackSegunCategoria {

        @Test
        @DisplayName("El caso en que la categoría es Supermercado, otorga cashback con la tasa de la categoría configurada (2%)")
        void otorgaCashbackConLaTasaDeLaCategoriaConfigurada() {
            BigDecimal montoNeto = new BigDecimal("100.00");

            BigDecimal cashback = calculadoraCashback.calcularExacto(montoNeto, CategoriaComercio.SUPERMERCADO.tasaCashback());

            assertThat(cashback).isEqualByComparingTo("2.00");
        }

        @Test
        @DisplayName("El caso en que la categoría es Combustible, otorga cashback con la tasa de la categoría configurada (1%)")
        void otorgaCashbackConLaTasaDeCombustible() {
            BigDecimal montoNeto = new BigDecimal("100.00");

            BigDecimal cashback = calculadoraCashback.calcularExacto(montoNeto, CategoriaComercio.COMBUSTIBLE.tasaCashback());

            assertThat(cashback).isEqualByComparingTo("1.00");
        }

        @Test
        @DisplayName("El caso en que la categoría es Por defecto, otorga cashback con la tasa de la categoría configurada (0.5%)")
        void otorgaCashbackConLaTasaPorDefecto() {
            BigDecimal montoNeto = new BigDecimal("100.00");

            BigDecimal cashback = calculadoraCashback.calcularExacto(montoNeto, CategoriaComercio.POR_DEFECTO.tasaCashback());

            assertThat(cashback).isEqualByComparingTo("0.50");
        }

        @Test
        @DisplayName("El caso en que el código de categoría no es reconocido por el sistema, se resuelve a la categoría Por defecto, no un error")
        void codigoNoReconocidoResuelveACategoriaPorDefecto() {
            CategoriaComercio categoria = CategoriaComercio.desde("FARMACIA");

            assertThat(categoria).isEqualTo(CategoriaComercio.POR_DEFECTO);
        }

        @Test
        @DisplayName("El caso en que no se informa ningún código de categoría, se resuelve a la categoría Por defecto, no un error")
        void codigoAusenteResuelveACategoriaPorDefecto() {
            CategoriaComercio categoria = CategoriaComercio.desde(null);

            assertThat(categoria).isEqualTo(CategoriaComercio.POR_DEFECTO);
        }
    }
}
