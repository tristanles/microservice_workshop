package com.nrkei.microservices.rapids_rivers

import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

// Ensures that PacketProblems operates correctly
class PacketProblemsTest {

  private var problems: PacketProblems? = null

  @Before
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
    assertThat<String>(problems!!.toString(), containsString("Simple error"))
  }

  @Test
  fun severeErrorsDetected() {
    problems!!.severeError("Severe error")
    assertTrue(problems!!.hasErrors())
    assertThat<String>(problems!!.toString(), containsString("Severe error"))
  }

  @Test
  fun warningsDetected() {
    problems!!.warning("Warning explanation")
    assertFalse(problems!!.hasErrors())
    assertThat<String>(problems!!.toString(), containsString("Warning explanation"))
  }

  @Test
  fun informationalMessagesDetected() {
    problems!!.warning("Information only message")
    assertFalse(problems!!.hasErrors())
    assertThat<String>(problems!!.toString(), containsString("Information only message"))
  }

  companion object {

    private val VALID_JSON = "{\"key1\":\"value1\"}"
  }
}