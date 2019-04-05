package com.nrkei.microservices.rapids_rivers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail as fail //to avoid using api.Assertions.fail, see https://github.com/junit-team/junit5/issues/1209
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows


/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

// Ensures that River triggers its RiverListeners with correct Packets
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RiverTest {

  private lateinit var rapidsConnection: TestRapidsConnection
  private lateinit var river : River

  @BeforeEach
  fun setup() {
    rapidsConnection = TestRapidsConnection()
    river = River(rapidsConnection)
    rapidsConnection.register(river)
  }

  @Test
  fun validJsonExtracted() {
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun invalidJsonFormat() {
    river.register(object : TestPacketListener() {
      override fun onError(connection: RapidsConnection, errors: PacketProblems) {
        assertTrue(errors.hasErrors())
      }
    })
    rapidsConnection.process(MISSING_COMMA)
  }

  @Test
  fun requiredKeyExists() {
    river.require(NEED_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertEquals("car_rental_offer", packet[NEED_KEY])
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun missingRequiredKey() {
    river.require("missing key")
    river.register(object : TestPacketListener() {
      override fun onError(connection: RapidsConnection, errors: PacketProblems) {
        assertTrue(errors.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun requiredKeyChangeable() {
    river.require(NEED_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertEquals("car_rental_offer", packet[NEED_KEY])
        packet.put(NEED_KEY, "airline_offer")
        assertEquals("airline_offer", packet[NEED_KEY])
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun forbiddenFieldChangeable() {
    river.forbid(KEY_TO_BE_ADDED)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertNull(packet[KEY_TO_BE_ADDED])
        packet.put(KEY_TO_BE_ADDED, "Bingo!")
        assertEquals("Bingo!", packet[KEY_TO_BE_ADDED])
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun emptyArrayPassesForbidden() {
    river.forbid(EMPTY_ARRAY_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun emptyStringPassesForbidden() {
    river.forbid(INTERESTING_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun forbiddenFieldRejected() {
    river.forbid(NEED_KEY)
    river.register(object : TestPacketListener() {
      override fun onError(connection: RapidsConnection, errors: PacketProblems) {
        assertTrue(errors.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun emptyArrayFailsRequire() {
    river.require(EMPTY_ARRAY_KEY)
    river.register(object : TestPacketListener() {
      override fun onError(connection: RapidsConnection, errors: PacketProblems) {
        assertTrue(errors.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun emptyStringFailsRequire() {
    river.require(EMPTY_STRING_KEY)
    river.register(object : TestPacketListener() {
      override fun onError(connection: RapidsConnection, errors: PacketProblems) {
        assertTrue(errors.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun interestingFieldsIdentified() {
    river.interestedIn(INTERESTING_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
        packet.put(INTERESTING_KEY, "interesting value")
        assertEquals("interesting value", packet[INTERESTING_KEY])
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun renderingJson() {
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
        val expected = SOLUTION_STRING.replace(":2", ":3") // Update read_count
        assertJsonEquals(expected, packet.toJson())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun changedKeyJson() {
    river.require(NEED_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        packet.put(NEED_KEY, "airline_offer")
        val expected = SOLUTION_STRING
          .replace(":2", ":3")
          .replace("car_rental_offer", "airline_offer")
        assertJsonEquals(expected, packet.toJson())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun traitChaining() {
    river
      .require(NEED_KEY)
      .forbid(EMPTY_ARRAY_KEY, KEY_TO_BE_ADDED)
      .interestedIn(INTERESTING_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun manipulatingJsonArrays() {
    river.require(SOLUTIONS_KEY)
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        val solutions = packet.getList(SOLUTIONS_KEY)
        assertEquals(3, solutions.size.toLong())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun requireValue() {
    river.requireValue(NEED_KEY, "car_rental_offer")
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
      }
    })
    rapidsConnection.process(SOLUTION_STRING)
  }

  @Test
  fun readCountAddedIfMissing() {
    river.register(object : TestPacketListener() {
      override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
        assertFalse(warnings.hasErrors())
        assertEquals(0.0, json(packet.toJson())[Packet.READ_COUNT])
      }
    })
    rapidsConnection.process("{}")
  }

  @Test
  fun problemsCanBeThrown() {
    river.register(object : TestPacketListener() {
      override fun onError(connection: RapidsConnection, errors: PacketProblems) {
        throw errors
      }
    })

    assertThrows<PacketProblems> { rapidsConnection.process(MISSING_COMMA) }

  }

  private fun assertJsonEquals(expected: String, actual: String) {
    assertEquals(json(expected), json(actual))
  }

  private fun json(jsonString: String): Map<*, *> {
    return Gson().fromJson<MutableMap<String, Any>>(jsonString)
  }
  //fix fox Gson : https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
  inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

  private inner class TestRapidsConnection : RapidsConnection() {
    override fun publish(message: String) {}  // Ignore for this test
    internal fun process(message: String) {
      for (l in listeners) l.message(this, message)
    }
  }

  private abstract inner class TestPacketListener : River.PacketListener {
    override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
      fail("Unexpected success parsing JSON packet. Packet is:\n"
        + packet.toJson()
        + "\nWarnings discovered were:\n"
        + warnings.toString())
    }

    override fun onError(connection: RapidsConnection, errors: PacketProblems) {
      fail("Unexpected JSON packet problem(s):\n$errors")
    }
  }

  companion object {

    private val SOLUTION_STRING = "{\"need\":\"car_rental_offer\"," +
      "\"user_id\":456," +
      "\"solutions\":[" +
      "{\"offer\":\"15% discount\"}," +
      "{\"offer\":\"500 extra points\"}," +
      "{\"offer\":\"free upgrade\"}" +
      "]," +
      "\"frequent_renter\":\"\"," +
      "\"system_read_count\":2," +
      "\"contributing_services\":[]}"

    private val MISSING_COMMA = "{\"frequent_renter\":\"\" \"read_count\":2}"

    private val NEED_KEY = "need"
    private val KEY_TO_BE_ADDED = "key_to_be_added"
    private val EMPTY_ARRAY_KEY = "contributing_services"
    private val EMPTY_STRING_KEY = "frequent_renter"
    private val INTERESTING_KEY = "frequent_renter"
    private val SOLUTIONS_KEY = "solutions"
  }
}