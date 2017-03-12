package com.nrkei.microservices.rapids_rivers;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

// Ensures that River triggers its RiverListeners with correct Packets
public class RiverTest {

    private final static String SOLUTION_STRING =
            "{\"need\":\"car_rental_offer\"," +
                    "\"user_id\":456," +
                    "\"solutions\":[" +
                    "{\"offer\":\"15% discount\"}," +
                    "{\"offer\":\"500 extra points\"}," +
                    "{\"offer\":\"free upgrade\"}" +
                    "]," +
                    "\"frequent_renter\":\"\"," +
                    "\"system.read_count\":2," +
                    "\"contributing_services\":[]}";

    private final static String MISSING_COMMA =
            "{\"frequent_renter\":\"\" \"read_count\":2}";

    private final static String NEED_KEY = "need";
    private final static String KEY_TO_BE_ADDED = "key_to_be_added";
    private static final String EMPTY_ARRAY_KEY = "contributing_services";
    private static final String EMPTY_STRING_KEY = "frequent_renter";
    private static final String INTERESTING_KEY = "frequent_renter";
    private static final String SOLUTIONS_KEY = "solutions";

    private TestRapidsConnection rapidsConnection;
    private River river;

    @Before
    public void setUp() throws Exception {
        rapidsConnection = new TestRapidsConnection();
        river = new River(rapidsConnection);
        rapidsConnection.register(river);
    }

    @Test
    public void validJsonExtracted() throws Exception {
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void invalidJsonFormat() throws Exception {
        river.register(new TestPacketListener () {
            @Override
            public void onError(RapidsConnection connection, PacketProblems errors) {
                assertTrue(errors.hasErrors());
            }
        });
        rapidsConnection.process(MISSING_COMMA);
    }

    @Test
    public void requiredKeyExists() throws Exception {
        river.require(NEED_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertEquals("car_rental_offer", packet.get(NEED_KEY));
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void missingRequiredKey() throws Exception {
        river.require("missing key");
        river.register(new TestPacketListener() {
            @Override
            public void onError(RapidsConnection connection, PacketProblems errors) {
                assertTrue(errors.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void requiredKeyChangeable() throws Exception {
        river.require(NEED_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertEquals("car_rental_offer", packet.get(NEED_KEY));
                packet.put(NEED_KEY, "airline_offer");
                assertEquals("airline_offer", packet.get(NEED_KEY));
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void forbiddenFieldChangeable() throws Exception {
        river.forbid(KEY_TO_BE_ADDED);
        river.register(new TestPacketListener() {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertNull(packet.get(KEY_TO_BE_ADDED));
                packet.put(KEY_TO_BE_ADDED, "Bingo!");
                assertEquals("Bingo!", packet.get(KEY_TO_BE_ADDED));
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void emptyArrayPassesForbidden() throws Exception {
        river.forbid(EMPTY_ARRAY_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void emptyStringPassesForbidden() throws Exception {
        river.forbid(INTERESTING_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void forbiddenFieldRejected() throws Exception {
        river.forbid(NEED_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void onError(RapidsConnection connection, PacketProblems errors) {
                assertTrue(errors.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void emptyArrayFailsRequire() throws Exception {
        river.require(EMPTY_ARRAY_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void onError(RapidsConnection connection, PacketProblems errors) {
                assertTrue(errors.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void emptyStringFailsRequire() throws Exception {
        river.require(EMPTY_STRING_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void onError(RapidsConnection connection, PacketProblems errors) {
                assertTrue(errors.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void interestingFieldsIdentified() throws Exception {
        river.interestedIn(INTERESTING_KEY);
        river.register(new TestPacketListener() {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
                packet.put(INTERESTING_KEY, "interesting value");
                assertEquals("interesting value", packet.get(INTERESTING_KEY));
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void renderingJson() throws Exception {
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
                String expected = SOLUTION_STRING.replace(":2", ":3"); // Update read_count
                assertJsonEquals(expected, packet.toJson());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void changedKeyJson() throws Exception {
        river.require(NEED_KEY);
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                packet.put(NEED_KEY, "airline_offer");
                String expected = SOLUTION_STRING
                        .replace(":2", ":3")
                        .replace("car_rental_offer", "airline_offer");
                assertJsonEquals(expected, packet.toJson());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void traitChaining() throws Exception {
        river
                .require(NEED_KEY)
                .forbid(EMPTY_ARRAY_KEY, KEY_TO_BE_ADDED)
                .interestedIn(INTERESTING_KEY);
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void manipulatingJsonArrays() throws Exception {
        river.require(SOLUTIONS_KEY);
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                List solutions = packet.getList(SOLUTIONS_KEY);
                assertEquals(3, solutions.size());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void requireValue() throws Exception {
        river.requireValue(NEED_KEY, "car_rental_offer");
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
            }
        });
        rapidsConnection.process(SOLUTION_STRING);
    }

    @Test
    public void readCountAddedIfMissing() throws Exception {
        river.register(new TestPacketListener () {
            @Override
            public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
                assertFalse(warnings.hasErrors());
                assertEquals(0.0, json(packet.toJson()).get(Packet.READ_COUNT));
            }
        });
        rapidsConnection.process("{}");
    }

    @Test(expected = PacketProblems.class)
    public void problemsCanBeThrown() throws Exception {
        river.register(new TestPacketListener () {
            @Override
            public void onError(RapidsConnection connection, PacketProblems errors) {
                throw errors;
            }
        });
        rapidsConnection.process(MISSING_COMMA);
    }

    private void assertJsonEquals(String expected, String actual) {
        assertEquals(json(expected), json(actual));
    }

    private Map json(String jsonString) {
        return new Gson().fromJson(jsonString, HashMap.class);
    }

    private class TestRapidsConnection extends RapidsConnection {
        @Override public void publish(String message) { }  // Ignore for this test
        void process(String message) {
            for (MessageListener l : listeners) l.message(this, message);
        }
    }

    private abstract class TestPacketListener implements River.PacketListener {
        @Override
        public void packet(RapidsConnection connection, Packet packet, PacketProblems warnings) {
            fail("Unexpected success parsing JSON packet. Packet is:\n"
                    + packet.toJson()
                    + "\nWarnings discovered were:\n"
                    + warnings.toString());
        }

        @Override
        public void onError(RapidsConnection connection, PacketProblems errors) {
            fail("Unexpected JSON packet problem(s):\n" + errors.toString());
        }
    }
}