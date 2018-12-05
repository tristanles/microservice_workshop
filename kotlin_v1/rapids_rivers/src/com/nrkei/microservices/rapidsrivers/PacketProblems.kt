/*
 * Copyright (c) 2018 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

package com.nrkei.microservices.rapidsrivers

// Understands issue that arose when analyzing a JSON message
// Implements Collecting Parameter in Refactoring by Martin Fowler
class PacketProblems(val originalJson: String) : RuntimeException() {

    private val informationalMessages = mutableListOf<String>()
    private val warnings = mutableListOf<String>()
    private val errors = mutableListOf<String>()
    private val severeErrors = mutableListOf<String>()

    fun hasMessages() = informationalMessages.isNotEmpty() || warnings.isNotEmpty() || hasErrors()

    fun hasErrors() = errors.isNotEmpty() || severeErrors.isNotEmpty()

    infix fun information(explanation: String) = informationalMessages.add(explanation)

    infix fun warning(explanation: String) = warnings.add(explanation)

    infix fun error(explanation: String) = errors.add(explanation)

    infix fun severeError(explanation: String): Any {
        severeErrors.add(explanation)
        throw this;
    }

    override fun toString(): String {
        if (!hasMessages()) return "No errors detected in JSON:\n\t$originalJson"
        val results = StringBuffer()
        results.append("Errors and/or messages exist. Original JSON string is:\n\t")
        results.append(originalJson)
        append("Severe errors", severeErrors, results)
        append("Errors", errors, results)
        append("Warnings", warnings, results)
        append("Information", informationalMessages, results)
        results.append("\n")
        return results.toString()
    }

    private fun append(label: String, messages: List<String>, results: StringBuffer) {
        if (messages.isEmpty()) return
        results.append("\n")
        results.append(label)
        results.append(": ")
        results.append(messages.size)
        for (message in messages) {
            results.append("\n\t")
            results.append(message)
        }
    }
}