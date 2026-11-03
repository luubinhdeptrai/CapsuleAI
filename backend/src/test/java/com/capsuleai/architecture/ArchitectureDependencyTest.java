package com.capsuleai.architecture;

import com.capsuleai.architecturefixture.adapter.in.InvalidInboundAdapterFixture;
import com.capsuleai.architecturefixture.adapter.out.InvalidOutboundAdapterFixture;
import com.capsuleai.architecturefixture.application.InvalidApplicationFixture;
import com.capsuleai.architecturefixture.domain.InvalidDomainFixture;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
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

    private static ArchRule frameworkFreeApplicationAndApiRule() {
        return noClasses()
                .that()
                .resideInAnyPackage(
                        "com.capsuleai..application..",
                        "com.capsuleai..api.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.servlet..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "java.sql..",
                        "javax.sql..",
                        "java.net.http..",
                        "com.fasterxml..",
                        "tools.jackson..",
                        "com.capsuleai..adapter..",
                        "com.capsuleai..configuration.."
                )
                .allowEmptyShould(true);
    }

    /**
     * Inbound and outbound adapters are independent edges: a format both must
     * agree on lives in a neutral package (for example {@code adapter.jwt}), TD-25.
     */
    private static ArchRule independentAdaptersRule() {
        return CompositeArchRule.of(
                        noClasses()
                                .that()
                                .resideInAPackage("com.capsuleai..adapter.in..")
                                .should()
                                .dependOnClassesThat()
                                .resideInAPackage("com.capsuleai..adapter.out..")
                                .allowEmptyShould(true))
                .and(
                        noClasses()
                                .that()
                                .resideInAPackage("com.capsuleai..adapter.out..")
                                .should()
                                .dependOnClassesThat()
                                .resideInAPackage("com.capsuleai..adapter.in..")
                                .allowEmptyShould(true));
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(
                        ImportOption.Predefined.DO_NOT_INCLUDE_TESTS
                )
                .importPackages("com.capsuleai");
    }

    @Test
    void productionDependenciesRespectInitialBoundaries() {

        JavaClasses production = productionClasses();

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
    void applicationAndApiCodeStaysFrameworkFree() {

        frameworkFreeApplicationAndApiRule().check(productionClasses());
    }

    @Test
    void inboundAndOutboundAdaptersStayIndependent() {

        independentAdaptersRule().check(productionClasses());
    }

    @Test
    void adapterRuleDetectsTheDeliberateTestFixtureViolations() {

        JavaClasses inboundFixture = new ClassFileImporter()
                .importClasses(InvalidInboundAdapterFixture.class);
        JavaClasses outboundFixture = new ClassFileImporter()
                .importClasses(InvalidOutboundAdapterFixture.class);

        assertThat(
                independentAdaptersRule().evaluate(inboundFixture).hasViolation()
        ).isTrue();
        assertThat(
                independentAdaptersRule().evaluate(outboundFixture).hasViolation()
        ).isTrue();
    }

    @Test
    void domainRuleDetectsTheDeliberateTestFixtureViolation() {

        JavaClasses fixture = new ClassFileImporter()
                .importClasses(InvalidDomainFixture.class);

        assertThat(
                domainRule().evaluate(fixture).hasViolation()
        ).isTrue();
    }

    @Test
    void applicationRuleDetectsTheDeliberateTestFixtureViolation() {

        JavaClasses fixture = new ClassFileImporter()
                .importClasses(InvalidApplicationFixture.class);

        assertThat(
                frameworkFreeApplicationAndApiRule().evaluate(fixture).hasViolation()
        ).isTrue();
    }
}