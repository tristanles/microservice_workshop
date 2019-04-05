package com.nrkei.microservices.rapids_rivers

import assertk.assertThat
import assertk.assertions.contains
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

// Ensures that PacketProblems operates correctly
class PacketProblemsTest {

  private var problems: PacketProblems? = null

  @BeforeEach
  fun setUp() {
    problems = PacketProblems(VALID_JSON)
  }

  @Test
  fun noProblemsFoundDefault() {
    assertFalse(problems!!.hasErrors())
  }

  @Test
  fun errorsDetected() {
    problems!!.error("Simple error")
    assertTrue(problems!!.hasErrors())
    assertThat(problems!!.toString()).contains("Simple error")
  }

  @Test
  fun severeErrorsDetected() {
    problems!!.severeError("Severe error")
    assertTrue(problems!!.hasErrors())
    assertThat(problems!!.toString()).contains("Severe error")

  }

  @Test
  fun warningsDetected() {
    problems!!.warning("Warning explanation")
    assertFalse(problems!!.hasErrors())
    assertThat(problems!!.toString()).contains("Warning explanation")

  }

  @Test
  fun informationalMessagesDetected() {
    problems!!.warning("Information only message")
    assertFalse(problems!!.hasErrors())
    assertThat(problems!!.toString()).contains("Information only message")
  }

  companion object {
    private val VALID_JSON = "{\"key1\":\"value1\"}"
  }
}