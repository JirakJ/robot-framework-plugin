/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RobotSections")
class RobotSectionsTest {

    @Test
    void rawHeaderNameStripsAsterisksAndWhitespace() {
        assertEquals("Test Cases", RobotSections.rawHeaderName("*** Test Cases ***"));
        assertEquals("settings", RobotSections.rawHeaderName("  *settings"));
        assertEquals("Keywords", RobotSections.rawHeaderName("***Keywords***   "));
    }

    @Test
    void nonHeadersReturnNull() {
        assertNull(RobotSections.rawHeaderName("Log    hello"));
        assertNull(RobotSections.rawHeaderName("***"));
        assertNull(RobotSections.rawHeaderName("*** ***"));
        assertNull(RobotSections.rawHeaderName(""));
        assertFalse(RobotSections.isHeader("    Log    *** not leading ***"));
        assertTrue(RobotSections.isHeader("*** Anything ***"));
    }

    @Test
    void knownHeadersAcceptSingularAndPluralAnyCase() {
        assertTrue(RobotSections.isKnownHeader("*** test cases ***"));
        assertTrue(RobotSections.isKnownHeader("*** Test Case ***"));
        assertTrue(RobotSections.isKnownHeader("*** TASKS ***"));
        assertTrue(RobotSections.isKnownHeader("*** Comment ***"));
        assertFalse(RobotSections.isKnownHeader("*** Something Else ***"));
        assertFalse(RobotSections.isKnownHeader("plain line"));
    }

    @Test
    void canonicalNameMapsAliases() {
        assertEquals("Settings", RobotSections.canonicalName("setting"));
        assertEquals("Variables", RobotSections.canonicalName("  VARIABLES "));
        assertEquals("Test Cases", RobotSections.canonicalName("Test Case"));
        assertNull(RobotSections.canonicalName("unknown"));
        assertNull(RobotSections.canonicalName(null));
    }

    @Test
    void singularHeadersAreDetected() {
        assertTrue(RobotSections.isSingularHeader("*** Test Case ***"));
        assertTrue(RobotSections.isSingularHeader("*** keyword ***"));
        assertFalse(RobotSections.isSingularHeader("*** Keywords ***"));
        assertFalse(RobotSections.isSingularHeader("not a header"));
    }

    @Test
    void canonicalizeNormalizesKnownAndKeepsUnknownNames() {
        assertEquals("*** Test Cases ***", RobotSections.canonicalize("*test case"));
        assertEquals("*** Settings ***", RobotSections.canonicalize("***setting***"));
        assertEquals("*** Custom Stuff ***", RobotSections.canonicalize("** Custom Stuff"));
        assertNull(RobotSections.canonicalize("Log    hi"));
    }
}
