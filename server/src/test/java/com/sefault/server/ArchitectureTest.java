package com.sefault.server;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.web.bind.annotation.RestController;

@ArchTag("ArchitectureTests")
@AnalyzeClasses(packages = "com.sefault.server")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule rest_controllers_should_be_suffixed = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .haveSimpleNameEndingWith("Controller")
            .because("We need strict naming conventions for maintainability, regardless of the package.");

    @ArchTest
    static final ArchRule services_should_be_interfaces = classes()
            .that()
            .haveSimpleNameEndingWith("Service")
            .should()
            .beInterfaces()
            .because("You defined in the spec that we must use interfaces for services.");

    @ArchTest
    static final ArchRule no_depending_on_service_impls = noClasses()
            .that().haveNameNotMatching(".*Test(s)?(\\$.*)?")
            .should()
            .dependOnClassesThat()
            .haveSimpleNameEndingWith("ServiceImpl")
            .because("Dependency Injection must rely on Service Interfaces, not concrete Implementations.");

    @ArchTest
    static final ArchRule controllers_must_not_depend_on_entities = noClasses()
            .that()
            .areAnnotatedWith(RestController.class)
            .or()
            .haveSimpleNameEndingWith("Controller")
            .should()
            .dependOnClassesThat()
            .areAnnotatedWith(Entity.class)
            .because(
                    "Controllers must only return DTOs. Leaking database entities to the web layer is strictly forbidden.");

    @ArchTest
    static final ArchRule services_should_be_injected_by_interfaces = noClasses()
            .that().haveNameNotMatching(".*Test(s)?(\\$.*)?")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("ServiceImpl")
            .because("Dependency Injection must rely on Service Interfaces, not concrete Implementations.");
}
