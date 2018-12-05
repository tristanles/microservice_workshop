/*
 * Copyright (c) 2018 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

package com.nrkei.microservices.rapidsrivers

import com.google.gson.Gson
import java.util.HashMap

// Understands a properly formatted JSON message
class Packet {
    private val problems: PacketProblems
    private var jsonHash: MutableMap<String, Any?>

    internal constructor(message: String, problems: PacketProblems) {
        this.problems = problems
        val jsonEngine =Gson()
        try {
            jsonHash = jsonEngine.fromJson<MutableMap<String, Any?>>(message, HashMap::class.java)
        }
        finally {

        }
    }
}