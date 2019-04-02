package com.nrkei.microservices.rapids_rivers
/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

// Understands accessing a stream of messages
abstract class RapidsConnection {

  protected val listeners = mutableListOf<MessageListener>()

  open fun register(listener: MessageListener) {
    listeners.add(listener)
  }

  abstract fun publish(message: String)

  interface MessageListener {
    fun message(sendPort: RapidsConnection, message: String)
  }
}
