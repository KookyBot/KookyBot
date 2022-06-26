/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
Copyright (C) <year>  <name of author>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package com.github.zly2006.khlkt.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaField

interface Updatable {
    fun update()
    @OptIn(ExperimentalStdlibApi::class)
    fun updateByJson(jsonElement: JsonElement) = if (jsonElement is JsonObject) {
        this::class.members.forEach { it ->
            if (it is KMutableProperty1<*, *>) {
                val name = it.javaField?.annotations?.filter { it.annotationClass == SerializedName::class }
                    ?.map { (it as SerializedName).value }
                    ?.firstOrNull() ?: it.name
                if (!jsonElement.has(name)) return@forEach
                if (it.annotations.filter { it.annotationClass == DontUpdate::class }.isNotEmpty()) return@forEach

                when (it.returnType.javaType) {
                    Int::class.java -> it.setter.call(this, jsonElement.get(name).asInt)
                    String::class.java -> it.setter.call(this, jsonElement.get(name).asString)
                    Char::class.java -> it.setter.call(this, jsonElement.get(name).asString[0])
                    Double::class.java -> it.setter.call(this, jsonElement.get(name).asDouble)
                    Float::class.java -> it.setter.call(this, jsonElement.get(name).asFloat)
                    Boolean::class.java -> it.setter.call(this, jsonElement.get(name).asBoolean)
                    //else -> throw Exception("Updatable: type ${it.returnType} of ${it.name} not supported")
                }
            }
        }
    }
    else{
        throw Exception("not supported")
    }
}