/*
 * Copyright (c) 2018 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

package com.nrkei.microservices.rapidsrivers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*

// Ensures that PacketProblems operates correctly
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PacketProblemsTest {

    private val VALID_JSON = "{\"key1\":\"value1\"}"
    private var problems = PacketProblems(VALID_JSON)

    @Test
    fun noProblemsFoundDefault() {
        assertFalse(problems.hasErrors())
    }
}