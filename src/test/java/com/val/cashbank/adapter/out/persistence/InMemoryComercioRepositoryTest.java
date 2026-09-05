package com.val.cashbank.adapter.out.persistence;

import com.val.cashbank.domain.model.Comercio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Repositorio de comercios")
class InMemoryComercioRepositoryTest {

    private final InMemoryComercioRepository comercioRepository = new InMemoryComercioRepository();

    @Nested
    @DisplayName("Regla: debe calcular el cashback como un porcentaje del monto neto de la compra, usando la tasa de cashback del comercio")
    class CalculoDelCashback {

        @Test
        @DisplayName("El caso en que se busca el comercio-1, devuelve el comercio con su tasa de cashback configurada")
        void buscarPorIdDevuelveElComercioConSuTasa() {
            Optional<Comercio> comercio = comercioRepository.buscarPorId("comercio-1");

            assertThat(comercio).isPresent();
            assertThat(comercio.get().tasaCashback()).isEqualByComparingTo("2.00");
        }
    }

    @Nested
    @DisplayName("Regla: no debe generar cashback en compras de comercios sin una tasa de cashback configurada. Un comercio se considera \"asociado\" si y solo si tiene una tasa configurada")
    @Disabled("Regla superada por doc/specs/categorias-comerciante-y-elegibilidad.md (Regla 1): la tasa ya no se configura manualmente por comercio, y todo comercio tiene una tasa aplicable (mínimo 0.5%, categoría Por defecto); el concepto de \"comercio no asociado = 0.00\" ya no existe.")
    class ComercioSinTasaConfigurada {

        @Test
        @DisplayName("El caso en que el comercio \"Comercio A\" tiene una tasa configurada de 3%, buscarPorId devuelve el comercio con esa tasa")
        void buscarPorIdDevuelveComercioAConSuTasa() {
            Optional<Comercio> comercio = comercioRepository.buscarPorId("comercio-a");

            assertThat(comercio).isPresent();
            assertThat(comercio.get().tasaCashback()).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("El caso en que el comercio no tiene ninguna tasa de cashback configurada (no está asociado), buscarPorId no devuelve ningún comercio")
        void buscarPorIdDevuelveVacioParaComercioSinTasa() {
            Optional<Comercio> comercio = comercioRepository.buscarPorId("comercio-sin-tasa");

            assertThat(comercio).isEmpty();
        }
    }

    @Nested
    @DisplayName("Regla: debe truncar el cashback calculado a 2 decimales sin redondear hacia arriba (RoundingMode.DOWN)")
    @Disabled("Fixtures comercio-tasa-3 (3%) y comercio-tasa-7-5 (7.5%) representaban tasas no alcanzables bajo el modelo de categorías (máx. 2%, ver doc/specs/categorias-comerciante-y-elegibilidad.md); además, esta clase solo verificaba la tasa configurada del fixture, no el truncamiento en sí, que ya cubre CalculadoraCashbackTest.")
    class TruncamientoDelCashback {

        @Test
        @DisplayName("El caso en que se busca el comercio-tasa-3, devuelve el comercio con tasa de cashback 3%")
        void buscarPorIdDevuelveComercioConTasa3() {
            Optional<Comercio> comercio = comercioRepository.buscarPorId("comercio-tasa-3");

            assertThat(comercio).isPresent();
            assertThat(comercio.get().tasaCashback()).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("El caso en que se busca el comercio-tasa-7-5, devuelve el comercio con tasa de cashback 7.5%")
        void buscarPorIdDevuelveComercioConTasa7Punto5() {
            Optional<Comercio> comercio = comercioRepository.buscarPorId("comercio-tasa-7-5");

            assertThat(comercio).isPresent();
            assertThat(comercio.get().tasaCashback()).isEqualByComparingTo("7.50");
        }
    }
}
