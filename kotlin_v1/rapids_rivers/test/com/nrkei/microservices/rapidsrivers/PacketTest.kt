/*
 * Copyright (c) 2018 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

package com.nrkei.microservices.rapidsrivers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

// Ensures Packet operates correctly
// NOTE: This test exercises internal methods, so is placed in the same module
class PacketTest {

    private val solutionString =
        "{\"need\":\"car_rental_offer\"," +
        "\"user_id\":456," +
        "\"solutions\":[" +
        "{\"offer\":\"15% discount\"}," +
        "{\"offer\":\"500 extra points\"}," +
        "{\"offer\":\"free upgrade\"}" +
        "]," +
        "\"frequent_renter\":\"\"," +
        "\"system_read_count\":2," +
        "\"contributing_services\":[]}"

    private val missingComma = "{\"frequent_renter\":\"\" \"read_count\":2}"

    private val needKey = "need"
    private val keyToBeAdded = "key_to_be_added"
    private val emptyArrayKey = "contributing_services"
    private val emptyStringKey = "frequent_renter"
    private val interestingKey = "frequent_renter"
    private val solutionsKey = "solutions"

    @Test fun `valid JSON accepted`() {
        val problems = PacketProblems(solutionString)
        Packet(solutionString, problems)
        assertFalse(problems.hasMessages())
    }
}