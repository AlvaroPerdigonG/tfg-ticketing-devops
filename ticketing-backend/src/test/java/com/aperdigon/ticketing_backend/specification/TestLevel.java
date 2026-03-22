package com.aperdigon.ticketing_backend.specification;

/**
 * Declares the intended verification layer for a backend test that references a
 * canonical functional scenario.
 *
 * <p>The value is part of a lightweight academic traceability model based on the
 * chain {@code Requirement -> Scenario (.feature) -> Test -> Code}.</p>
 *
 * <p>This enum does not introduce BDD execution and does not imply the use of
 * Cucumber or any other feature runner. It only classifies how the JUnit test
 * contributes to specification coverage.</p>
 */
public enum TestLevel {
    UNIT,
    INTEGRATION,
    UI,
    E2E
}
