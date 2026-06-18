package aisafe.architecture;

/*
 * Architectural fitness functions for the AISafe codebase.
 *
 * Each rule encodes a convention from CLAUDE.md. Pattern for future work:
 *   - Document the convention in CLAUDE.md first.
 *   - Add the @ArchTest rule here in the same PR.
 *   - To fix a failing rule: open an issue, fix the violation in its own PR,
 *     enable the TODO stub in that same PR so CI enforces it forever after.
 */

import aisafe.shared.application.UseCase;
import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.DomainException;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "aisafe", importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

    // --- Layering: domain isolation ---

    @ArchTest
    static final ArchRule domain_no_jpa_imports =
        noClasses()
            .that().resideInAPackage("aisafe.*.domain..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .because("domain classes must not import jakarta.persistence");

    @ArchTest
    static final ArchRule domain_no_jackson_imports =
        noClasses()
            .that().resideInAPackage("aisafe.*.domain..")
            .should().dependOnClassesThat().resideInAnyPackage("com.fasterxml.jackson..", "tools.jackson..")
            .because("domain classes must not import Jackson (covers both Jackson 2.x and 3.x package roots)");

    /*
     * security.domain.User intentionally implements Spring Security's UserDetails,
     * so that package is excluded. For all other domain packages, only
     * org.springframework.util.Assert is currently tolerated (CLAUDE.md).
     */
    @ArchTest
    static final ArchRule domain_no_spring_except_assert =
        noClasses()
            .that().resideInAPackage("aisafe.*.domain..")
            .and().resideOutsideOfPackage("aisafe.security..")
            .should().dependOnClassesThat(
                resideInAPackage("org.springframework..")
                    .and(not(equivalentTo(org.springframework.util.Assert.class))))
            .because("domain classes must not depend on Spring Framework; "
                + "only org.springframework.util.Assert is currently tolerated");

    // --- Use case shape ---

    @ArchTest
    static final ArchRule use_cases_annotated =
        classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .and().areNotInterfaces()
            .and().resideInAPackage("aisafe.*.application..")
            .should().beAnnotatedWith(UseCase.class)
            .because("use cases must carry @UseCase (= @Service + @Validated + @Transactional)");

    @ArchTest
    static final ArchRule use_cases_reside_in_application =
        classes()
            .that().areAnnotatedWith(UseCase.class)
            .should().resideInAPackage("aisafe.*.application..")
            .because("@UseCase must only appear in the application layer");

    private static final ArchCondition<JavaClass> HAVE_PUBLIC_EXECUTE =
        new ArchCondition<>("have a public method named 'execute'") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                boolean found = clazz.getMethods().stream()
                    .anyMatch(m -> "execute".equals(m.getName())
                        && m.getModifiers().contains(JavaModifier.PUBLIC));
                if (!found)
                    events.add(SimpleConditionEvent.violated(clazz,
                        clazz.getName() + " does not have a public 'execute' method"));
            }
        };

    @ArchTest
    static final ArchRule use_cases_have_execute_method =
        classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .and().areNotInterfaces()
            .and().resideInAPackage("aisafe.*.application..")
            .should(HAVE_PUBLIC_EXECUTE)
            .because("every use case exposes exactly one public method named 'execute'");

    // --- Repository pattern ---

    /*
     * security.domain.UserRepository intentionally omits findAll/delete and does not extend
     * BaseRepository - the security context is a known special case (see domain_no_spring_except_assert).
     */
    @ArchTest
    static final ArchRule domain_repositories_extend_base =
        classes()
            .that().areInterfaces()
            .and().haveSimpleNameEndingWith("Repository")
            .and().resideInAPackage("aisafe.*.domain..")
            .and().resideOutsideOfPackage("aisafe.security..")
            .and().doNotHaveSimpleName("BaseRepository")
            .should().beAssignableTo(BaseRepository.class)
            .because("domain repository interfaces must extend BaseRepository");

    @ArchTest
    static final ArchRule jpa_adapters_have_profile =
        classes()
            .that().resideInAPackage("aisafe.*.infrastructure.persistence..")
            .and().areAnnotatedWith(Repository.class)
            .should().beAnnotatedWith(Profile.class)
            .because("JPA adapters must be profile-guarded so the in-memory adapter can swap in");

    // --- Exception hierarchy ---

    @ArchTest
    static final ArchRule domain_exceptions_extend_domain_exception =
        classes()
            .that().haveSimpleNameEndingWith("Exception")
            .and().resideInAPackage("aisafe.*.domain..")
            .and().doNotHaveSimpleName("DomainException")
            .should().beAssignableTo(DomainException.class)
            .because("domain exceptions must extend DomainException for correct HTTP mapping");

    // --- Controller placement ---

    @ArchTest
    static final ArchRule controllers_in_infrastructure =
        classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("aisafe.*.infrastructure..")
            .because("controllers are infrastructure concerns");

    // --- TODO stubs: enable in the same PR that fixes the violation ---

    private static final ArchCondition<JavaClass> NOT_RETURN_DOMAIN_ENTITIES =
        new ArchCondition<>("not return domain entities from their execute method") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                clazz.getMethods().stream()
                    .filter(m -> "execute".equals(m.getName()) && m.getModifiers().contains(JavaModifier.PUBLIC))
                    .forEach(m -> {
                        JavaClass returnType = m.getRawReturnType();
                        String name = returnType.getName();
                        if (name.startsWith("aisafe.") && name.contains(".domain.")
                                && !name.equals("aisafe.shared.domain.PaginatedResult")) {
                            events.add(SimpleConditionEvent.violated(clazz,
                                clazz.getName() + " returns domain type " + name));
                        }
                    });
            }
        };

    @ArchTest
    static final ArchRule use_cases_dont_return_domain_entities =
        classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .and().areNotInterfaces()
            .and().resideInAPackage("aisafe.*.application..")
            .should(NOT_RETURN_DOMAIN_ENTITIES)
            .because("use cases must return DTOs or primitives, never domain entities");

    @ArchTest
    static final ArchRule application_no_security_context_holder =
        noClasses()
            .that().resideInAPackage("aisafe.*.application..")
            .should().dependOnClassesThat(equivalentTo(org.springframework.security.core.context.SecurityContextHolder.class))
            .because("application layer must not access SecurityContextHolder directly; pass identity in via request DTOs or arguments");

    @ArchTest
    static final ArchRule application_no_infrastructure_dependency =
        noClasses()
            .that().resideInAPackage("aisafe.*.application..")
            .should().dependOnClassesThat().resideInAPackage("aisafe.*.infrastructure..")
            .because("application layer must not depend on infrastructure layer");
}
