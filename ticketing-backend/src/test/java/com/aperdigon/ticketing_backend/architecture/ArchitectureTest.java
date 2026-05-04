package com.aperdigon.ticketing_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Architecture fitness tests to prevent architectural erosion over time. */
@DisplayName("Architecture fitness tests")
@AnalyzeClasses(packages = "com.aperdigon.ticketing_backend")
class ArchitectureTest {

    @Nested
    @DisplayName("DomainLayer")
    class DomainLayer {

        @ArchTest
        static final ArchRule domain_must_not_depend_on_application = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..");

        @ArchTest
        static final ArchRule domain_must_not_depend_on_api = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..api..");

        @ArchTest
        static final ArchRule domain_must_not_depend_on_infrastructure = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

        @ArchTest
        static final ArchRule domain_must_not_depend_on_spring = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "org.springframework.security..");

        @ArchTest
        static final ArchRule domain_must_not_depend_on_jakarta_persistence = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

        @ArchTest
        static final ArchRule domain_should_not_depend_on_java_sql = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("java.sql..");

        @ArchTest
        static final ArchRule domain_should_not_use_spring_stereotypes = classes()
                .that().resideInAPackage("..domain..")
                .should().notBeAnnotatedWith(Component.class)
                .andShould().notBeAnnotatedWith(Service.class)
                .andShould().notBeAnnotatedWith(Repository.class)
                .andShould().notBeAnnotatedWith(Autowired.class);
    }

    @Nested
    @DisplayName("ApplicationLayer")
    class ApplicationLayer {

        @ArchTest
        static final ArchRule application_must_not_depend_on_api = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..api..");

        @ArchTest
        static final ArchRule application_must_not_depend_on_infrastructure = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

        @ArchTest
        static final ArchRule application_must_not_depend_on_spring_web = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.web..");

        @ArchTest
        static final ArchRule application_must_not_depend_on_spring_data_jpa = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.data.jpa..");

        @ArchTest
        static final ArchRule application_must_not_depend_on_jakarta_persistence = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

        @ArchTest
        static final ArchRule application_should_not_depend_on_java_sql = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("java.sql..");

        @ArchTest
        static final ArchRule application_should_not_use_web_annotations = classes()
                .that().resideInAPackage("..application..")
                .should().notBeAnnotatedWith(RestController.class)
                .andShould().notBeAnnotatedWith(RequestMapping.class)
                .andShould().notBeAnnotatedWith(GetMapping.class)
                .andShould().notBeAnnotatedWith(PostMapping.class)
                .andShould().notBeAnnotatedWith(PutMapping.class)
                .andShould().notBeAnnotatedWith(DeleteMapping.class);
    }

    @Nested
    @DisplayName("ApiLayer")
    class ApiLayer {

        @ArchTest
        static final ArchRule api_must_not_depend_on_infrastructure_persistence = noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..");

        @ArchTest
        static final ArchRule api_must_not_depend_on_infrastructure_repositories = noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence.jpa.repository..");
    }

    @Nested
    @DisplayName("InfrastructureLayer")
    class InfrastructureLayer {
        // Infrastructure rules are intentionally permissive regarding dependencies
        // on domain and application because infrastructure hosts adapters.
    }
}
