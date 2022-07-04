/* KookyBot - a SDK of <https://www.kookapp.cn> for JVM platform
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

package io.github.zly2006.kookybot.events

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.message.SelfMessage
import io.github.zly2006.kookybot.utils.Emoji
import kotlin.reflect.KClass
import io.github.zly2006.kookybot.events.Listener as Listener1

class SingleEventHandler<T> (
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
    val listeners: MutableMap<KClass<out Event>, MutableList<SingleEventHandler<*>>> = mutableMapOf()
    val classListeners: MutableList<Listener1> = mutableListOf()
    // 用于click处理
    val clickEvents: MutableList<Pair<String, (CardButtonClickEvent) -> Unit>> = mutableListOf()

    inline fun <reified T : Event> callEvent(event: T) {
        if (event is CardButtonClickEvent) {
            clickEvents.forEach {
                if (it.first == event.value)
                    it.second(event)
            }
        }
        listeners[T::class]?.forEach { it ->
            try {
                it.handle(event)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        classListeners.forEach {
            it.javaClass.methods.forEach { method ->
                 if (method.annotations.find { it.annotationClass == EventHandler::class } != null) {
                    if (method.parameterTypes[0] == T::class.java) {
                        method.invoke(it, event)
                    }
                }
            }
        }
    }

    fun callEventRaw(json: JsonObject) {
        callEvent(RawEvent(json))
        val event = Gson().fromJson(json, MessageEvent::class.java)
        if (event.authorId == client.self?.id) {
            if (event.channelType == MessageEvent.ChannelType.GROUP ||
                    event.channelType == MessageEvent.ChannelType.PERSON) {
                callEvent(SelfMessageEvent(SelfMessage(
                    client = client,
                    id = json.get("msg_id").asString,
                    timestamp = json.get("msg_timestamp").asInt,
                    target = when (event.channelType) {
                        MessageEvent.ChannelType.GROUP -> client.self!!.guilds.map {
                            it.channels.firstOrNull {
                                it.id == json.get("target_id").asString
                            }
                        }.firstOrNull { it != null }!!
                        MessageEvent.ChannelType.PERSON -> client.self!!.chattingUsers.firstOrNull { it.id == json.get("target_id").asString }!!
                        else -> TODO()
                    },
                    content = json.get("content").asString
                )))
            }
            return
        }
        if (event.eventType == MessageEvent.EventType.SYSTEM) {
            when (json.get("extra").asJsonObject.get("type").asString) {
                "added_reaction" -> {
                    val channelPostReactionEvent = Gson().fromJson(json, ChannelPostReactionEvent::class.java)
                    channelPostReactionEvent.channel = client.self!!.getChannel(json.get("extra").asJsonObject.get("channel_id").asString)!!
                    channelPostReactionEvent.emoji = Emoji(
                        json.get("extra").asJsonObject.get("emoji").asJsonObject.get("id").asString,
                        json.get("extra").asJsonObject.get("emoji").asJsonObject.get("name").asString
                    )
                    channelPostReactionEvent.guild = channelPostReactionEvent.channel.guild
                    channelPostReactionEvent.sender = client.self!!.getGuildUser(json.get("extra").asJsonObject.get("user_id").asString, channelPostReactionEvent.guild.id)!!
                    channelPostReactionEvent.targetId = json.get("extra").asJsonObject.get("msg_id").asString
                    callEvent(channelPostReactionEvent)
                }
                "deleted_reaction" -> {
                    val channelCancelReactionEvent = Gson().fromJson(json, ChannelCancelReactionEvent::class.java)
                    channelCancelReactionEvent.channel = client.self!!.getChannel(json.get("extra").asJsonObject.get("channel_id").asString)!!
                    channelCancelReactionEvent.emoji = Emoji(
                        json.get("extra").asJsonObject.get("emoji").asJsonObject.get("id").asString,
                        json.get("extra").asJsonObject.get("emoji").asJsonObject.get("name").asString
                    )
                    channelCancelReactionEvent.guild = channelCancelReactionEvent.channel.guild
                    channelCancelReactionEvent.sender = client.self!!.getGuildUser(json.get("extra").asJsonObject.get("user_id").asString, channelCancelReactionEvent.guild.id)!!
                    channelCancelReactionEvent.targetId = json.get("extra").asJsonObject.get("msg_id").asString
                    callEvent(channelCancelReactionEvent)
                }
                "message_btn_click" -> {
                    event.extra = event.extra.get("body").asJsonObject
                    val cardButtonClickEvent = Gson().fromJson(json, CardButtonClickEvent::class.java)
                    cardButtonClickEvent.channel = client.self!!.getChannel(event.extra.get("target_id").asString)
                    cardButtonClickEvent.sender = client.self!!.getUser(event.extra.get("user_id").asString)
                    cardButtonClickEvent.targetId = event.extra.get("msg_id").asString
                    cardButtonClickEvent.value = event.extra.get("value").asString
                    callEvent(cardButtonClickEvent)
                }
            }
        }
        else if (event.channelType == MessageEvent.ChannelType.GROUP) {
            val channelMessageEvent = Gson().fromJson(json, ChannelMessageEvent::class.java)
            val guild = client.self!!.guilds.firstOrNull { it.id == json["extra"].asJsonObject["guild_id"].asString }
            channelMessageEvent.guild = guild!!
            val channel = guild.channels.firstOrNull { it.id == json["target_id"].asString }
            channelMessageEvent.channel = channel!!
            val user = client.self!!.getGuildUser(json["author_id"].asString, guild.id)!!
            channelMessageEvent.sender = user
            callEvent(channelMessageEvent)
        }
        else if (event.channelType == MessageEvent.ChannelType.PERSON) {
            // process
            if (client.self!!.chattingUsers.find { it.id == event.authorId } == null) {
                client.self!!.updatePrivateChatUser(event.authorId)
            }
            val privateMessageEvent = Gson().fromJson(json, PrivateMessageEvent::class.java)
            privateMessageEvent.sender = client.self!!.chattingUsers.find { it.code == json.get("extra").asJsonObject.get("code").asString }!!
            privateMessageEvent.sender.update()
            callEvent(privateMessageEvent)
        }
    }

    inline fun<reified T> addListener(noinline handler: T.() -> Unit) where T:Event{
        val eventHandler = SingleEventHandler(handler)
        if (listeners.contains(T::class)) {
            listeners[T::class]!!.add(eventHandler)
        }
        else {
            listeners[T::class] = mutableListOf(eventHandler)
        }
    }

    fun addClassListener(listener: Listener1) {
        classListeners.add(listener)
    }
}