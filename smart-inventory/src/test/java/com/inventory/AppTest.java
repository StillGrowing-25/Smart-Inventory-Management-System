package com.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test — verifies the test infrastructure is wired correctly.
 */
@DisplayName("Application Smoke Test")
public class AppTest {

    @Test
    @DisplayName("Test infrastructure is operational")
    void smokeTest() {
        assertTrue(true, "JUnit 5 is running correctly");
    }
}
