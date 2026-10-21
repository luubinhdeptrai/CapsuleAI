package com.capsuleai.architecture;

import com.capsuleai.architecturefixture.domain.InvalidDomainFixture;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureDependencyTest {

    private static ArchRule domainRule() {
        return noClasses()
                .that()
                .resideInAPackage("com.capsuleai..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "java.sql..",
                        "javax.sql..",
                        "java.net.http..",
                        "com.fasterxml..",
                        "tools.jackson..",
                        "com.capsuleai..application..",
                        "com.capsuleai..adapter.."
                )
                .allowEmptyShould(true);
    }

    @Test
    void productionDependenciesRespectInitialBoundaries() {

        JavaClasses production = new ClassFileImporter()
                .withImportOption(
                        ImportOption.Predefined.DO_NOT_INCLUDE_TESTS
                )
                .importPackages("com.capsuleai");

        assertThat(production)
                .extracting(JavaClass::getName)
                .contains("com.capsuleai.CapsuleAiApplication");

        domainRule().check(production);

        for (String owner : new String[] {
                "identity",
                "personalcontext",
                "wardrobe",
                "outfitadviceanduse",
                "wardrobeintelligence",
                "productmeasurement"
        }) {

            String base = "com.capsuleai." + owner;

            noClasses()
                    .that()
                    .resideOutsideOfPackage(base + "..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            base + ".domain..",
                            base + ".application..",
                            base + ".adapter.."
                    )
                    .allowEmptyShould(true)
                    .check(production);
        }
    }

    @Test
    void domainRuleDetectsTheDeliberateTestFixtureViolation() {

        JavaClasses fixture = new ClassFileImporter()
                .importClasses(InvalidDomainFixture.class);

        assertThat(
                domainRule().evaluate(fixture).hasViolation()
        ).isTrue();
    }
}