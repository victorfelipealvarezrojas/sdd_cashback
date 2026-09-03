    package com.val.cashbank.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Reglas de arquitectura hexagonal (Puertos y Adaptadores)")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.val.cashbank";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("El dominio es Java puro")
    class DomainEsJavaPuro {

        @Test
        @DisplayName("El dominio nunca depende de Spring Framework")
        void dominioNoDependeDeSpring() {
            noClasses()
                    .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                    .because("el dominio debe ser Java puro, sin dependencias de framework")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("El dominio nunca depende de jakarta.persistence")
        void dominioNoDependeDeJpa() {
            noClasses()
                    .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                    .because("las entidades JPA pertenecen a adapter/out/persistence, no al dominio")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Las dependencias fluyen hacia adentro: adapter -> application -> domain")
    class FlujoDeDependencias {

        @Test
        @DisplayName("adapter puede depender de application y domain, application solo de domain, domain de nada")
        void capasRespetanElFlujoDeDependencias() {
            ArchRule regla = Architectures.layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Adapter").definedBy(BASE_PACKAGE + ".adapter..")
                    .layer("Application").definedBy(BASE_PACKAGE + ".application..")
                    .layer("Domain").definedBy(BASE_PACKAGE + ".domain..")

                    .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter");

            regla.check(importedClasses);
        }
    }
}
