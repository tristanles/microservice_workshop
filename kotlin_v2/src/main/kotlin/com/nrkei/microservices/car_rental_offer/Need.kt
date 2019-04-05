package com.nrkei.microservices.car_rental_offer

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

import com.nrkei.microservices.rapids_rivers.Packet
import com.nrkei.microservices.rapids_rivers.RapidsConnection
import com.nrkei.microservices.rapids_rivers.rabbit_mq.RabbitMqRapids
import java.util.*


// Understands the requirement for advertising on a site
object Need {
  fun publish(rapidsConnection: RapidsConnection) {
    try {
      while (true) {
        publish(rapidsConnection, Packet(anonymousNeed()))
        publish(rapidsConnection, Packet(registeredNeed()))
      }
    } catch (e: Exception) {
      throw RuntimeException("Could not publish message:", e)
    }
  }

  private fun publish(rapidsConnection: RapidsConnection, packet: Packet) {
    println(String.format(" [<] %s", packet.toJson()))
    rapidsConnection.publish(packet.toJson())
    Thread.sleep(5_000)
  }

  private fun anonymousNeed() = hashMapOf(
      "need" to "car_rental_offer",
      "need_id" to UUID.randomUUID())

  private fun registeredNeed() = anonymousNeed().plus(
    "user_id" to UUID.randomUUID()) as MutableMap<String, Any?>

}

fun main(args: Array<String>) {
  val host = args[0]
  val port = args[1]

  val rapidsConnection = RabbitMqRapids("car_rental_need_java", host, port)
  Need.publish(rapidsConnection)
}

