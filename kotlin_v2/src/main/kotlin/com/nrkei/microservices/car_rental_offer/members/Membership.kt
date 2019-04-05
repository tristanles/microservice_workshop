package com.nrkei.microservices.car_rental_offer.members

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
object Membership : River.PacketListener {
  override fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems) {
    val membership = getMembershipStatus(packet["user-id"].toString()) ?: return
    packet.put(membership.first, membership.second)
    connection.publish(packet.toJson())
  }

  private fun getMembershipStatus(userId: String): Pair<String, String>? {
    val lastChar = userId.last()
    return when {
      lastChar.isLetter() -> return null
      lastChar.toInt() in 0..2 -> "member" to "silver"
      lastChar.toInt() in 3..6 -> "member" to "gold"
      else -> "member" to "platinium"

    }
  }


  override fun onError(connection: RapidsConnection, errors: PacketProblems) = Unit
}

fun main(args: Array<String>) {
  val host = args[0]
  val port = args[1]

  val rapidsConnection = RabbitMqRapids("membership_kotlin", host, port)
  val river = River(rapidsConnection)
  river.requireValue("need", "car_rental_offer")  // Reject packet unless it has key:value pair
  river.require("user-id")  // Reject packet unless it has key:value pair
  river.forbid("solution", "member")        // Reject packet if it does have key1 or key2
  river.register(Membership)         // Hook up to the river to start receiving traffic
}
