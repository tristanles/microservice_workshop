package com.nrkei.microservices.car_rental_offer

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

import com.nrkei.microservices.rapids_rivers.Packet
import com.nrkei.microservices.rapids_rivers.PacketProblems
import com.nrkei.microservices.rapids_rivers.RapidsConnection
import com.nrkei.microservices.rapids_rivers.River
import com.nrkei.microservices.rapids_rivers.rabbit_mq.RabbitMqRapids

// Understands the messages on an event bus
object MembershipReliableSolution : River.PacketListener {
  override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
    packet.put("solution", hashMapOf(
      "name" to "Toyota Corolla, the most reliable car around!",
      "value" to 190,
      "likelihood" to 0.95))
    connection.publish(packet.toJson())
  }


  override fun onError(connection: RapidsConnection, errors: PacketProblems) = Unit
}

fun main(args: Array<String>) {
  val host = args[0]
  val port = args[1]

  val rapidsConnection = RabbitMqRapids("cheap_solution_in_kotlin", host, port)
  val river = River(rapidsConnection)
  // See RiverTest for various functions River supports to aid in filtering, like:
  river.requireValue("need", "car_rental_offer");  // Reject packet unless it has key:value pair
  //river.require("key1", "key2");       // Reject packet unless it has key1 and key2
  river.forbid("solution")        // Reject packet if it does have key1 or key2
  //river.interestedIn("key1", "key2");  // Allows key1 and key2 to be queried and set in a packet
  river.register(ReliableSolution)         // Hook up to the river to start receiving traffic
}
