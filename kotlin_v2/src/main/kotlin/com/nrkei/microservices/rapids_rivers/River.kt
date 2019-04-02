package com.nrkei.microservices.rapids_rivers

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

// Understands a stream of valid JSON packets meeting certain criteria
// Implements GOF Observer pattern to trigger listeners with packets and/or problems
// Implements GOF Command pattern for validations
class River(private val rapidsConnection: RapidsConnection) : RapidsConnection.MessageListener {
  private val listeners = mutableListOf<PacketListener>()
  private val validations = mutableListOf<Validation>()

  init {
    rapidsConnection.register(this)
  }

  fun register(listener: PacketListener) {
    listeners.add(listener)
  }

  override fun message(sendPort: RapidsConnection, message: String) {
    val problems = PacketProblems(message)
    val packet = Packet(message, problems)
    validations.forEach { it.validate(packet) }
    if (problems.hasErrors())
      onError(sendPort, problems)
    else
      packet(sendPort, packet, problems)
  }

  private fun packet(sendPort: RapidsConnection, packet: Packet, warnings: PacketProblems) {
    for (l in listeners) l.packet(sendPort, packet, warnings)
  }

  private fun onError(sendPort: RapidsConnection, errors: PacketProblems) {
    listeners.forEach { it.onError(sendPort, errors) }
  }

  fun require(vararg jsonKeys: String): River {
    validations.add(RequiredKeys(*jsonKeys))
    return this
  }

  fun forbid(vararg jsonKeys: String): River {
    validations.add(ForbiddenKeys(*jsonKeys))
    return this
  }

  fun interestedIn(vararg jsonKeys: String): River {
    validations.add(InterestingKeys(*jsonKeys))
    return this
  }

  fun requireValue(jsonKey: String, expectedValue: String): River {
    validations.add(RequiredValue(jsonKey, expectedValue))
    return this
  }

  interface PacketListener {
    fun packet(connection: RapidsConnection, packet: Packet, warnings: PacketProblems)
    fun onError(connection: RapidsConnection, errors: PacketProblems)
  }

  private interface Validation {
    fun validate(packet: Packet)
  }

  private inner class RequiredKeys internal constructor(private vararg val requiredKeys: String) : Validation {
    override fun validate(packet: Packet) = packet.require(*requiredKeys)
  }

  private inner class ForbiddenKeys internal constructor(private vararg val forbiddenKeys: String) : Validation {
    override fun validate(packet: Packet) = packet.forbid(*forbiddenKeys)
  }

  private inner class InterestingKeys internal constructor(private vararg val forbiddenKeys: String) : Validation {
    override fun validate(packet: Packet) = packet.interestedIn(*forbiddenKeys)
  }

  private inner class RequiredValue internal constructor(private val requiredKey: String, private val requiredValue: String) : Validation {
    override fun validate(packet: Packet) = packet.requireValue(requiredKey, requiredValue)
  }

}
