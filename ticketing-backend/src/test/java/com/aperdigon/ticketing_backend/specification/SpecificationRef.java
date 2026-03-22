package com.aperdigon.ticketing_backend.specification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links a JUnit test class or test method to a canonical functional scenario
 * defined in the project's {@code docs/features/*.feature} files.
 *
 * <p>The annotation supports a lightweight traceability chain:
 * {@code Requirement -> Scenario (.feature) -> Test -> Code}.</p>
 *
 * <p>Its purpose is documentary and architectural. It makes it easier to audit
 * which automated tests provide evidence for which functional scenarios, while
 * keeping the solution simple, explicit, and maintainable.</p>
 *
 * <p>This annotation does <strong>not</strong> imply executable BDD, does not run
 * Gherkin scenarios, and does not require Cucumber. The executable verification
 * remains implemented directly in JUnit tests.</p>
 *
 * <p>The annotation is designed for an academic project where a small amount of
 * runtime metadata is enough to support traceability without introducing heavy
 * tooling or additional test runners.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(SpecificationRefs.class)
public @interface SpecificationRef {

    /**
     * Stable scenario identifier, for example {@code TICKET-USER-01}.
     */
    String value();

    /**
     * Intended test layer that provides evidence for the referenced scenario.
     */
    TestLevel level();

    /**
     * Optional feature file name, for example {@code tickets-user.feature}.
     */
    String feature() default "";

    /**
     * Optional note to clarify the scope of the correspondence.
     */
    String note() default "";
}
