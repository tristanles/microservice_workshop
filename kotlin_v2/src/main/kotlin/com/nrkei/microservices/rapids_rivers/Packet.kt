package com.nrkei.microservices.rapids_rivers

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class Packet private constructor(private val jsonHash: MutableMap<String, Any?>, private val problems: PacketProblems) {
  private val recognizedKeys = mutableMapOf<String, Any?>()

  constructor(jsonHash: MutableMap<String, Any?>) : this(jsonHash, PacketProblems.Empty) {
    incrementCounter()
  }

  internal constructor(message: String, problems: PacketProblems) : this(mutableMapOf<String, Any?>(), problems) {
    val jsonEngine = Gson()
    try {
      jsonHash.putAll(jsonEngine.fromJson<MutableMap<String, Any>>(message))
      incrementCounter()
    } catch (e: JsonSyntaxException) {
      problems.severeError("Invalid JSON format per Gson library")
    } catch (e: Exception) {
      problems.severeError("Unknown failure. Message is: $e")
    }
  }

  private fun incrementCounter() {
    jsonHash.putIfAbsent(READ_COUNT, -1.0)
    jsonHash[READ_COUNT] = (jsonHash[READ_COUNT] as Double).toInt() + 1.0
  }

  //fix fox Gson : https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
  inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

  internal fun addAccessor(requiredJsonKey: String) {
    if (!recognizedKeys.containsKey(requiredJsonKey))
      recognizedKeys.put(requiredJsonKey, jsonHash[requiredJsonKey])
  }

  internal fun require(vararg requiredJsonKeys: String) {
    requiredJsonKeys.forEach {
      if (hasKey(it) && !isKeyEmpty(it)) {
        addAccessor(it)
        return@forEach
      }
      problems.error("Missing required key '$it'")
    }
  }

  internal fun forbid(vararg forbiddenJsonKeys: String) {
    forbiddenJsonKeys.forEach {
      if (isKeyMissing(it)) {
        addAccessor(it)
        return@forEach
      }
      problems.error("Forbidden key '$it' already defined")
    }
  }

  internal fun interestedIn(vararg interestingJsonKeys: String) = interestingJsonKeys.forEach { addAccessor(it) }

  internal fun requireValue(requiredKey: String, requiredValue: Any) {
    if (isKeyMissing(requiredKey) || jsonHash[requiredKey] != requiredValue) {
      problems.error("Required key '$requiredKey' does not have required value '$requiredValue'")
      return
    }
    addAccessor(requiredKey)
  }

  operator fun get(key: String) = recognizedKeys[key]

  fun put(key: String, value: Any) {
    if (!recognizedKeys.containsKey(key))
      throw IllegalArgumentException(
        "Manipulated keys must be declared as required, forbidden, or interesting")
    recognizedKeys[key] = value
  }

  fun toJson(): String {
    val updatedHash = HashMap(jsonHash)
    recognizedKeys.keys.forEach { updatedHash[it] = recognizedKeys[it] }
    return Gson().toJson(updatedHash)
  }

  fun getList(solutionsKey: String): List<*> = get(solutionsKey) as List<*>

  // TODO: May need expansion for deeper keys...
  private fun hasKey(key: String) = jsonHash.containsKey(key)

  // TODO: May need expansion for deeper keys...
  private fun isKeyMissing(forbiddenJsonKey: String) = !hasKey(forbiddenJsonKey) || isKeyEmpty(forbiddenJsonKey)

  private fun isKeyEmpty(jsonKey: String): Boolean {
    val value = jsonHash[jsonKey]
    if (value is String) return value.isEmpty()
    if (value is Collection<*>) return value.isEmpty()
    return false
  }

  companion object {
    const val READ_COUNT: String = "system_read_count"
  }


}