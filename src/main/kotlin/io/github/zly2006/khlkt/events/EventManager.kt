/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
Copyright (C) 2022, zly2006 & contributors

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

package io.github.zly2006.khlkt.events

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.zly2006.khlkt.client.Client
import kotlin.reflect.KClass

class EventHandler<T> (
    var handler: (T) -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    fun handle(event: Event) {
        handler(event as T)
    }
}


class EventManager(
    private val client: Client
) {
    val listeners: MutableMap<KClass<out Event>, MutableList<EventHandler<*>>> = mutableMapOf()
    val javaListeners: MutableList<JavaEventHandler<*>> = mutableListOf()

    inline fun <reified T : Event> callEvent(event: T) {
        listeners[T::class]?.forEach { it ->
            try {
                it.handle(event)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        javaListeners.forEach {
            try {
                it.call(event)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun callEventRaw(json: JsonObject) {
        callEvent(RawEvent(json))
        val event = Gson().fromJson(json, MessageEvent::class.java)
        if (event.authorId == client.self?.id)
            return
        if (event.eventType == MessageEvent.EventType.SYSTEM) {
            // TODO
        }
        else if (event.channelType == MessageEvent.ChannelType.GROUP) {
            val channelMessageEvent = Gson().fromJson(json, ChannelMessageEvent::class.java)
            val guild = client.self!!.guilds.firstOrNull { it.id == json["extra"].asJsonObject["guild_id"].asString }
            channelMessageEvent.guild = guild!!
            val channel = guild.channels.firstOrNull { it.id == json["target_id"].asString }
            channelMessageEvent.channel = channel!!
            val user = client.getGuildUser(json["author_id"].asString, guild.id)
            channelMessageEvent.sender = user
            callEvent(channelMessageEvent)
        }
    }

    inline fun<reified T> addListener(noinline handler: T.() -> Unit) where T:Event{
        val eventHandler = EventHandler(handler)
        if (listeners.contains(T::class)) {
            listeners[T::class]!!.add(eventHandler)
        }
        else {
            listeners[T::class] = mutableListOf(eventHandler)
        }
    }

    fun <T> addJavaListener(javaEventHandler: JavaEventHandler<T>) {
        javaListeners.add(javaEventHandler)
    }
}