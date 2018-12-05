/*
 * Copyright (c) 2018 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

package com.nrkei.microservices.rapidsrivers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat

// Ensures that PacketProblems operates correctly
@TestInstance(TestInstance.Lifecycle.PER_METHOD)  // We want a fresh copy of instance variables for each test
internal class PacketProblemsTest {

    private val validJson = "{\"key1\":\"value1\"}"
    private var problems = PacketProblems(validJson)

    @Test fun `no problems in new instance`() {
        assertFalse(problems.hasErrors())
        assertFalse(problems.hasMessages())
    }

    @Test fun `informational messages detected`() {
        problems information  "Only information"
        assertTrue(problems.hasMessages())
        assertFalse(problems.hasErrors())
        assertThat(problems.toString(), containsString("Information"))
        assertThat(problems.toString(), containsString("Only information"))
    }

    @Test fun `warnings detected`() {
        problems warning "Just a warning"
        assertTrue(problems.hasMessages())
        assertFalse(problems.hasErrors())
        assertThat(problems.toString(), containsString("Warnings"))
        assertThat(problems.toString(), containsString("Just a warning"))
    }

    @Test fun `errors detected`() {
        problems error "Simple error"
        assertTrue(problems.hasMessages())
        assertTrue(problems.hasErrors())
        assertThat(problems.toString(), containsString("Errors"))
        assertThat(problems.toString(), containsString("Simple error"))
    }

    @Test fun `severe errors throw exception`() {
        try {
            problems severeError "Severe error"
            fail<String>("Severe error did not automatically throw an exception")
        } catch (p: PacketProblems) {
            assertTrue(p.hasMessages())
            assertTrue(p.hasErrors())
            assertThat(p.toString(), containsString("Severe errors"))
            assertThat(p.toString(), containsString("Severe error"))
        }
    }
}