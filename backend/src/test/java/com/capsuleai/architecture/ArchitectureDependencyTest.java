package com.capsuleai.architecture;

import com.capsuleai.architecturefixture.adapter.in.InvalidInboundAdapterFixture;
import com.capsuleai.architecturefixture.adapter.in.JdbcInInboundAdapterFixture;
import com.capsuleai.architecturefixture.adapter.out.InvalidOutboundAdapterFixture;
import com.capsuleai.architecturefixture.application.InvalidApplicationFixture;
import com.capsuleai.architecturefixture.domain.InvalidDomainFixture;
import com.capsuleai.architecturefixture.ownera.api.OwnerALeakingContractFixture;
import com.capsuleai.architecturefixture.ownera.api.OwnerAPublishedContractFixture;
import com.capsuleai.architecturefixture.ownerb.application.OwnerBBypassingServiceFixture;
import com.capsuleai.architecturefixture.ownerb.application.OwnerBPublishedApiConsumerFixture;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureDependencyTest {

    private static final String FIXTURE_ROOT = "com.capsuleai.architecturefixture";

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

    /**
     * ADA-016/017/018 and AR-03: another module may use only a module's published
     * {@code api}. Its domain, application, ports, adapters (including persistence) and
     * configuration are private.
     */
    private static ArchRule onlyThePublishedApiIsUsedFromOtherModules(String rootPackage, String modulePackage) {
        String base = rootPackage + "." + modulePackage;
        return noClasses()
                .that()
                .resideOutsideOfPackage(base + "..")
                .should()
                .dependOnClassesThat(resideInAPackage(base + "..").and(not(resideInAPackage(base + ".api.."))))
                .allowEmptyShould(true)
                .because("module " + modulePackage + " publishes only its api package (ADA-016, ADA-018, AR-03)");
    }

    /** ADA-018: a published contract carries published language, never a private model. */
    private static ArchRule publishedApiExposesNoPrivateModelRule() {
        return noClasses()
                .that()
                .resideInAPackage("com.capsuleai..api..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.capsuleai..domain..", "com.capsuleai..application..")
                .allowEmptyShould(true)
                .because("published contracts must not expose private domain or application models (ADA-018)");
    }

    /** ADA-015 and AR-03: SQL access stays in persistence adapters; configuration only wires them. */
    private static ArchRule springJdbcStaysInPersistenceAdaptersRule() {
        return noClasses()
                .that()
                .resideOutsideOfPackages("com.capsuleai..adapter.out..", "com.capsuleai..configuration..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework.jdbc..", "javax.sql..")
                .allowEmptyShould(true)
                .because("only a module's own persistence adapters access the database (ADA-015, AR-03)");
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
    }

    @Test
    void modulesUseEachOtherOnlyThroughPublishedApis() {

        JavaClasses production = productionClasses();

        for (BusinessModule module : BusinessModule.values()) {
            onlyThePublishedApiIsUsedFromOtherModules(BusinessModule.ROOT_PACKAGE, module.packageName())
                    .check(production);
        }
    }

    @Test
    void everyBusinessModulePackageIsRegistered() {

        assertThat(productionClasses())
                .extracting(JavaClass::getPackageName)
                .filteredOn(name -> name.startsWith(BusinessModule.ROOT_PACKAGE + "."))
                .map(name -> name.substring(BusinessModule.ROOT_PACKAGE.length() + 1).split("\\.")[0])
                .allSatisfy(module -> assertThat(BusinessModule.ofPackageName(module))
                        .as("module package %s is listed in BusinessModule with its schema", module)
                        .isPresent());
    }

    @Test
    void modulesHaveNoDependencyCycles() {

        slices()
                .matching(BusinessModule.ROOT_PACKAGE + ".(*)..")
                .should()
                .beFreeOfCycles()
                .allowEmptyShould(true)
                .check(productionClasses());
    }

    @Test
    void publishedApisExposeNoPrivateModel() {

        publishedApiExposesNoPrivateModelRule().check(productionClasses());
    }

    @Test
    void onlyPersistenceAdaptersAndConfigurationUseSpringJdbc() {

        springJdbcStaysInPersistenceAdaptersRule().check(productionClasses());
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

    @Test
    void moduleBoundaryRuleDetectsAPrivateRepositoryBypassAndAcceptsThePublishedApi() {

        ArchRule ownerABoundary = onlyThePublishedApiIsUsedFromOtherModules(FIXTURE_ROOT, "ownera");

        assertThat(ownerABoundary.evaluate(new ClassFileImporter()
                .importClasses(OwnerBBypassingServiceFixture.class)).hasViolation()).isTrue();
        assertThat(ownerABoundary.evaluate(new ClassFileImporter()
                .importClasses(OwnerBPublishedApiConsumerFixture.class, OwnerAPublishedContractFixture.class))
                .hasViolation()).isFalse();
    }

    @Test
    void publishedApiRuleDetectsALeakedPrivateModel() {

        assertThat(publishedApiExposesNoPrivateModelRule().evaluate(new ClassFileImporter()
                .importClasses(OwnerALeakingContractFixture.class)).hasViolation()).isTrue();
        assertThat(publishedApiExposesNoPrivateModelRule().evaluate(new ClassFileImporter()
                .importClasses(OwnerAPublishedContractFixture.class)).hasViolation()).isFalse();
    }

    @Test
    void jdbcRuleDetectsDatabaseAccessOutsideAPersistenceAdapter() {

        assertThat(springJdbcStaysInPersistenceAdaptersRule().evaluate(new ClassFileImporter()
                .importClasses(JdbcInInboundAdapterFixture.class)).hasViolation()).isTrue();
    }
}
