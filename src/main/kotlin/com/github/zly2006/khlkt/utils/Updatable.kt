package com.github.zly2006.khlkt.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.javaType

open class Updatable {
    @OptIn(ExperimentalStdlibApi::class)
    open fun updateByJson(jsonElement: JsonElement) = if (jsonElement is JsonObject) {
        this::class.members.forEach { it ->
            if (it is KMutableProperty1<*, *>) {
                val name = it.annotations.filter { it.javaClass == SerializedName::class.java }
                    .map { (it as SerializedName).value }
                    .firstOrNull() ?: it.name
                if (!jsonElement.has(name)) return@forEach
                if (it.annotations.filter { it.javaClass == Transient::class.java }.isNotEmpty()) return@forEach

                when (it.returnType.javaType) {
                    Int::class.java -> it.setter.call(this, jsonElement.get(name).asInt)
                    String::class.java -> it.setter.call(this, jsonElement.get(name).asString)
                    Char::class.java -> it.setter.call(this, jsonElement.get(name).asString[0])
                    Double::class.java -> it.setter.call(this, jsonElement.get(name).asDouble)
                    Float::class.java -> it.setter.call(this, jsonElement.get(name).asFloat)
                    Boolean::class.java -> it.setter.call(this, jsonElement.get(name).asBoolean)
                    else -> throw Exception("Updatable: type not supported")
                }
            }
        }
    }
    else{
        throw Exception("not supported")
    }
}