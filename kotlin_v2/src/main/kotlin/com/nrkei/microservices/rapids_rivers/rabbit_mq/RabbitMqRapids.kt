package com.nrkei.microservices.rapids_rivers.rabbit_mq

/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George
 */

import com.nrkei.microservices.rapids_rivers.RapidsConnection
import com.rabbitmq.client.*

import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.TimeoutException

// Understands an event bus implemented with RabbitMQ in pub/sub mode (fanout)
class RabbitMqRapids(serviceName: String, host: String, port: String) : RapidsConnection(), AutoCloseable {

  private val factory: ConnectionFactory
  private var connection: Connection? = null
  private var channel: Channel? = null
  private val queueName: String

  init {
    queueName = serviceName + "_" + UUID.randomUUID().toString()
    factory = ConnectionFactory()
    factory.setHost(host)
    factory.setPort(Integer.parseInt(port))
  }

  override fun register(listener: RapidsConnection.MessageListener) {
    if (channel == null) connect()
    if (listeners.isEmpty()) {
      configureQueue()
      println(" [*] Waiting for messages. To exit press CTRL+C")
      consumeMessages(consumer(channel))
    }
    super.register(listener)
  }

  override fun publish(message: String) {
    if (channel == null) connect()
    try {
      channel!!.basicPublish(EXCHANGE_NAME, "", null, message.toByteArray(charset("UTF-8")))
    } catch (e: UnsupportedEncodingException) {
      e.printStackTrace()
      throw RuntimeException("UnsupportedEncodingException on message extraction", e)
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException when sending a message", e)
    }

  }

  private fun connect() {
    establishConnectivity()
    declareExchange()
  }

  private fun establishConnectivity() {
    connection = connection()
    channel = channel()
  }

  private fun connection(): Connection {
    try {
      return factory.newConnection()
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException on creating Connection", e)
    }

  }

  private fun channel(): Channel {
    try {
      return connection!!.createChannel()
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException in creating Channel", e)
    }

  }

  private fun declareExchange() {
    try {
      // Configure for durable, auto-delete
      channel!!.exchangeDeclare(EXCHANGE_NAME, RABBIT_MQ_PUB_SUB, true, true, HashMap<String, Any>())
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException declaring Exchange", e)
    }

  }

  private fun configureQueue() {
    declareQueue()
    bindQueueToExchange()
  }

  private fun declareQueue(): AMQP.Queue.DeclareOk {
    try {
      // Configured for non-durable, auto-delete, and exclusive
      return channel!!.queueDeclare(queueName, false, true, true, HashMap<String, Any>())
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException declaring Queue", e)
    }

  }

  private fun bindQueueToExchange() {
    try {
      channel!!.queueBind(queueName, EXCHANGE_NAME, "")
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException binding Queue to Exchange", e)
    }

  }

  private fun consumeMessages(consumer: Consumer): String {
    try {
      return channel!!.basicConsume(queueName, true, consumer)
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException while consuming messages", e)
    }

  }

  private fun consumer(channel: Channel?): DefaultConsumer {
    val sendPort = this
    return object : DefaultConsumer(channel) {
      @Throws(IOException::class)
      override fun handleDelivery(consumerTag: String, envelope: Envelope,
                                  properties: AMQP.BasicProperties, body: ByteArray) {
        val message = String(body, Charset.forName("UTF-8"))
        //                System.out.println(" [x] Received '" + message + "'");
        for (listener in listeners) listener.message(sendPort, message)
      }
    }
  }

  override fun close() {
    try {
      if (channel != null) channel!!.close()
      if (connection != null) connection!!.close()
    } catch (e: IOException) {
      e.printStackTrace()
      throw RuntimeException("IOException on close", e)
    }

  }

  companion object {
    // See RabbitMQ pub/sub documentation: https://www.rabbitmq.com/tutorials/tutorial-three-python.html
    private val RABBIT_MQ_PUB_SUB = "fanout"
    private val EXCHANGE_NAME = "rapids"
  }

}
