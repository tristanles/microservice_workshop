package com.nrkei.microservices.car_rental_offer

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

import com.nrkei.microservices.rapids_rivers.*
import com.nrkei.microservices.rapids_rivers.rabbit_mq.RabbitMqRapids


// Understands the requirement for advertising on a site
object Need {
  fun publish(rapidsConnection: RapidsConnection) {
    try {
      while (true) {
        val jsonMessage = needPacket().toJson()
        println(String.format(" [<] %s", jsonMessage))
        rapidsConnection.publish(jsonMessage)
        Thread.sleep(5_000)
      }
    } catch (e: Exception) {
      throw RuntimeException("Could not publish message:", e)
    }
  }

  private fun needPacket() = Packet(hashMapOf("need" to "car_rental_offer"))
}

fun main(args: Array<String>) {
  val host = args[0]
  val port = args[1]

  val rapidsConnection = RabbitMqRapids("car_rental_need_java", host, port)
  Need.publish(rapidsConnection)
}

