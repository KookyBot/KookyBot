package com.github.zly2006.khlkt.events

import com.github.zly2006.khlkt.client.Client
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.reflect.KClass

@OptIn(DelicateCoroutinesApi::class)
class EventManager(
    val client: Client
) {
    val listeners: MutableMap<KClass<out Event>, MutableList<EventHandler<*>>> = mutableMapOf()

    inline fun <reified T : Event> callEvent(event: T) {
        listeners[T::class]?.forEach { it ->
            it.handle(event)
        }
    }

    fun callEventRaw(json: JsonObject) {
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
}