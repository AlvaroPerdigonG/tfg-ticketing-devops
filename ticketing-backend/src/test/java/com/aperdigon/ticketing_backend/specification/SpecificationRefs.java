package com.aperdigon.ticketing_backend.specification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for repeatable {@link SpecificationRef} declarations.
 *
 * <p>Its purpose is purely structural: it allows a single JUnit test class or
 * test method to reference more than one canonical functional scenario when the
 * same automated check provides evidence for multiple requirements.</p>
 *
 * <p>Within the project traceability chain {@code Requirement -> Scenario
 * (.feature) -> Test -> Code}, this annotation helps keep the mapping simple and
 * explicit without introducing executable BDD tooling.</p>
 *
 * <p>It is intended for an academic JUnit-based test suite and should be used as
 * documentation and traceability metadata only.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SpecificationRefs {
    SpecificationRef[] value();
}
