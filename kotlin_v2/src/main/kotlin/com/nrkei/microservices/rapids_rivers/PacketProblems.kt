package com.nrkei.microservices.rapids_rivers

import java.lang.RuntimeException

class PacketProblems(private val originalJson: String) : RuntimeException() {

  private val informationalMessages = mutableListOf<String>()
  private val warnings = mutableListOf<String>()
  private val errors = mutableListOf<String>()
  private val severeErrors = mutableListOf<String>()

  fun hasErrors() = errors.any() || severeErrors.any()
  private fun hasMessages() = hasErrors() || informationalMessages.any() || warnings.any()

  fun information(explanation: String) = informationalMessages.add(explanation)
  fun warning(explanation: String) = warnings.add(explanation)
  fun error(explanation: String) = errors.add(explanation)
  fun severeError(explanation: String) = severeErrors.add(explanation)

  override fun getLocalizedMessage() = toString()
  override fun toString(): String {
    if (!hasMessages()) return "No errors detected in JSON:\n\t$originalJson"
    val results = StringBuffer()
    results.append("Errors and/or messages exist. Original JSON string is:")
    results.newLine().tab().append(originalJson)
    results.appendMessages("Severe errors", severeErrors)
    results.appendMessages("Errors", errors)
    results.appendMessages("Warnings", warnings)
    results.appendMessages("Information", informationalMessages)
    results.newLine()
    return results.toString()
  }

  fun StringBuffer.appendMessages(label: String, messages: List<String>) : StringBuffer {
    if (messages.isEmpty()) return this
    this.newLine()
    this.append("$label: ${messages.size}")
    messages.forEach { this.newLine().tab().append(it) }
    return this
  }

  fun StringBuffer.newLine() : StringBuffer = this.append("\n")
  fun StringBuffer.tab() : StringBuffer = this.append("\t")

  companion object {
    val Empty: PacketProblems = PacketProblems("")
  }
}