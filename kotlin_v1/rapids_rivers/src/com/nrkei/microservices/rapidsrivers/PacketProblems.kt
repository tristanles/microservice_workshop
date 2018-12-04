/*
 * Copyright (c) 2018 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

package com.nrkei.microservices.rapidsrivers

import java.util.ArrayList

// Understands issue that arose when analyzing a JSON message
// Implements Collecting Parameter in Refactoring by Martin Fowler
class PacketProblems(val validJson: String) {

    private val informationalMessages = mutableListOf<String>()
    private val warnings = mutableListOf<String>()
    private val errors = mutableListOf<String>()
    private val severeErrors = mutableListOf<String>()

    fun hasErrors() = errors.isNotEmpty() || severeErrors.isNotEmpty()
}