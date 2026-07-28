package com.prestamosfacil;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter().importPackages("com.prestamosfacil");
    }

    @Test
    void domainShouldNotDependOnInfrastructure() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..domain..", "java..", "lombok..",
                "org.junit..", "org.opentest4j..", "org.assertj..", "org.mockito..");

        rule.check(classes);
    }

    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat()
            .resideOutsideOfPackage("org.springframework..");

        rule.check(classes);
    }

    @Test
    void applicationShouldNotDependOnInfrastructureAdapters() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..application..", "..domain..", "java..", "lombok..",
                "org.springframework..", "jakarta..", "org.slf4j..",
                "org.junit..", "org.opentest4j..", "org.assertj..", "org.mockito..");

        rule.check(classes);
    }

    @Test
    void infrastructureCanDependOnApplicationAndDomain() {
        ArchRule rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

        rule.check(classes);
    }
}
