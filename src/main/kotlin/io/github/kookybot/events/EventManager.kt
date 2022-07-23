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

package io.github.kookybot.events

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.kookybot.annotation.Filter
import io.github.kookybot.client.Client
import io.github.kookybot.commands.CommandSource
import io.github.kookybot.contract.Guild
import io.github.kookybot.contract.PrivateChatUser
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.contract.VoiceChannel
import io.github.kookybot.events.channel.*
import io.github.kookybot.events.direct.DirectCancelReactionEvent
import io.github.kookybot.events.direct.DirectMessageEvent
import io.github.kookybot.events.direct.DirectPostReactionEvent
import io.github.kookybot.events.guild.*
import io.github.kookybot.events.self.SelfExitedGuildEvent
import io.github.kookybot.events.self.SelfJoinedGuildEvent
import io.github.kookybot.events.self.SelfMessageEvent
import io.github.kookybot.message.SelfMessage
import io.github.kookybot.utils.Emoji
import kotlinx.coroutines.DelicateCoroutinesApi
import org.apache.directory.api.ldap.model.cursor.Tuple
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.cast
import kotlin.reflect.jvm.kotlinFunction
import io.github.kookybot.events.Listener as Listener1


fun Filter.parse(input: String): Map<String, Any>? {
    val list = mutableListOf<Tuple<String?, Regex>>()
    val ret = mutableMapOf<String, String>()
    var start = 0
    while (pattern.indexOf('{', start) != -1) {
        val index = pattern.indexOf('{', start)
        if (start != index) {
            list.add(Tuple(null, Regex.fromLiteral(pattern.substring(start until index))))
        }
        start = pattern.indexOf('}', index)
        val s = pattern.substring(index + 1 until start)
        start += 1
        var name = s
        var regex = Regex("\\w+")
        if (s.contains(',')) {
            name = s.substring(0 until s.indexOf(','))
            regex = Regex(s.substring(s.indexOf(',') + 1))
        }
        list.add(Tuple(name, regex))
    }
    if (start != pattern.length) {
        list.add(Tuple(null, Regex.fromLiteral(pattern.substring(start until pattern.length))))
    }
    // parsing start
    start = 0
    for (item in list) {
        val result = item.value.matchAt(input, start)
        if (result?.range?.start != start)
            return null
        if (item.key != null) {
            ret[item.key!!] = result.value
        }
        start = result.range.last + 1
    }
    return ret
}
class SingleEventHandler<T>(
    var handler: (T) -> Unit,
    private val eventManager: EventManager
) {
    @Suppress("UNCHECKED_CAST")
    fun handle(event: Event) {
        handler(event as T)
    }
    fun cancel() {
        for (handlers in eventManager.listeners.values) {
            handlers.remove(this)
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
class EventManager(
    private val client: Client,
) {
    val dispatcher = CommandDispatcher<CommandSource>()
    val listeners: MutableMap<KClass<out Event>, MutableList<SingleEventHandler<*>>> = mutableMapOf()
    val classListeners: MutableList<Listener1> = mutableListOf()

    // 用于click处理
    val clickEvents: MutableList<Pair<String, (CardButtonClickEvent) -> Unit>> = mutableListOf()

    fun parseCommand(consoleCommand: String) {
        if (consoleCommand == "") return
        try {
            dispatcher.execute(consoleCommand, CommandSource(timestamp = Calendar.getInstance().timeInMillis))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parseCommand(event: MessageEvent) {
        if (!client.config.enableCommand) return
        if (client.config.commandPrefix.map { event.content.startsWith(it) }.find { it } == null) return
        val source = when (event) {
            is ChannelMessageEvent -> CommandSource(
                type = CommandSource.Type.Channel,
                channel = event.channel,
                user = event.sender,
                timestamp = event.timestamp.toLong()
            )
            is DirectMessageEvent -> CommandSource(
                type = CommandSource.Type.Private,
                user = event.sender,
                private = event.sender,
                timestamp = event.timestamp.toLong()
            )
            else -> throw Exception("invalid type")
        }
        try {
            dispatcher.execute(event.content.substring(1), source)
        } catch (e: CommandSyntaxException) {
            if (client.config.responseCommandExceptions) {
                when (event) {
                    is DirectMessageEvent -> event.sender.sendMessage("命令语法不正确。详细信息：\n```\n${e.localizedMessage}\n```")
                    is ChannelMessageEvent -> event.channel.sendMessage("(met)${event.sender.id}(met)命令语法不正确。详细信息：\n```\n${e.localizedMessage}\n```")
                }
            }
            e.printStackTrace()
        } catch (e: Exception) {
            if (client.config.responseCommandExceptions) {
                when (event) {
                    is DirectMessageEvent -> event.sender.sendMessage("执行命令时发生了错误，请联系开发者。详细信息：\n```\n${e}\n```")
                    is ChannelMessageEvent -> event.channel.sendMessage("(met)${event.sender.id}(met)执行命令时发生了错误，请联系开发者。详细信息：\n```\n${e}\n```")
                }
            }
            e.printStackTrace()
        }
    }

    /**
     * 不inline
     */
    fun checkAndParseCommand(event: MessageEvent) {
        if (with(client) {
                (config.enableCommand)
            }) {
            when (event) {
                is DirectMessageEvent -> if (event.eventType == MessageEvent.EventType.MARKDOWN) parseCommand(event)
                is ChannelMessageEvent -> if (event.eventType == MessageEvent.EventType.MARKDOWN) parseCommand(event)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T : Event> callEvent(event: T) {
        if (event is CardButtonClickEvent) {
            clickEvents.forEach {
                if (it.first == event.value) {
                    try {
                        it.second(event)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (event is MessageEvent) {
            checkAndParseCommand(event)
        }
        listeners[T::class]?.forEach { it ->
            try {
                it.handle(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        classListeners.forEach {
            try {
                it.javaClass.methods.forEach { method ->
                    if (method.annotations.find { it.annotationClass == EventHandler::class } != null) {
                        try {
                            if (method.parameterTypes[0] == T::class.java) {
                                method.invoke(it, event)
                            } else if (event is ChannelMessageEvent || event is DirectMessageEvent) {
                                event as MessageEvent
                                val kotlinFilters = method.annotations.filter { it.annotationClass == Filter::class }
                                    .map { Filter::class.cast(it) }
                                for (filter in kotlinFilters) {
                                    filter.parse(event.content)?.toMutableMap()?.let { parsingResult ->
                                        val args: MutableMap<KParameter, Any?> = mutableMapOf()
                                        val func = method.kotlinFunction
                                        val source = when (event) {
                                            is ChannelMessageEvent -> CommandSource(
                                                type = CommandSource.Type.Channel,
                                                channel = event.channel,
                                                user = event.sender,
                                                timestamp = event.timestamp.toLong()
                                            )
                                            is DirectMessageEvent -> CommandSource(
                                                type = CommandSource.Type.Private,
                                                user = event.sender,
                                                private = event.sender,
                                                timestamp = event.timestamp.toLong()
                                            )
                                            else -> throw Exception("invalid type")
                                        }
                                        parsingResult["source"] = source
                                        if (func == null) return@let
                                        if ((method.parameters.isNotEmpty()) && (!func.parameters.map {
                                                it.name?.matches(
                                                    Regex("^arg\\d+$")
                                                )
                                            }.contains(false))) {
                                            // java method, ordered.
                                            method.invoke(it, *parsingResult.values.toTypedArray())
                                        } else if (parsingResult.keys.containsAll(func.parameters.mapNotNull { it.name })) {
                                            func.parameters.map { parameter ->
                                                when (parameter.type.classifier) {
                                                    String::class -> {
                                                        args[parameter] = parsingResult[parameter.name] as String
                                                    }
                                                    CommandSource::class -> {
                                                        if (parameter.name == "source")
                                                            args[parameter] = source
                                                        else
                                                            null
                                                    }
                                                    it::class -> {
                                                        args[parameter] = it
                                                    }
                                                    else -> {
                                                        throw Exception("unsupported type.")
                                                    }
                                                }
                                            }
                                            try {
                                                func.callBy(args)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun callEventRaw(json: JsonObject) {
        callEvent(RawEvent(json, client.self!!))
        val event = Gson().fromJson(json, MessageEvent::class.java)
        if (event.authorId == client.self?.id) {
            if (event.channelType == MessageEvent.ChannelType.GROUP ||
                event.channelType == MessageEvent.ChannelType.PERSON
            ) {
                callEvent(SelfMessageEvent(SelfMessage(
                    client = client,
                    id = json.get("msg_id").asString,
                    timestamp = json.get("msg_timestamp").asInt,
                    target = when (event.channelType) {
                        MessageEvent.ChannelType.GROUP -> client.self!!.guilds.map {
                            it.value.lazyChannels[json["target_id"].asString]?.value
                        }.first { it != null } as TextChannel
                        MessageEvent.ChannelType.PERSON -> client.self!!.chattingUsers[json["target_id"].asString]!!.value as PrivateChatUser
                        else -> throw Exception("Invalid channel type.")
                    },
                    content = json.get("content").asString
                ), client.self!!))
            }
            return
        }
        if (event.eventType == MessageEvent.EventType.SYSTEM) {
            event.extra.get("body").asJsonObject.addProperty("type", event.extra.get("type").asString)
            event.extra = event.extra.get("body").asJsonObject
            when (event.extra.get("type").asString) {
                "added_reaction" -> {
                    val channelPostReactionEvent = Gson().fromJson(json, ChannelPostReactionEvent::class.java)
                    channelPostReactionEvent.channel =
                        client.self!!.getChannel(event.extra.get("channel_id").asString)!! as TextChannel
                    channelPostReactionEvent.emoji = Emoji(
                        event.extra.get("emoji").asJsonObject.get("id").asString,
                        event.extra.get("emoji").asJsonObject.get("name").asString
                    )
                    channelPostReactionEvent.guild = channelPostReactionEvent.channel.guild
                    channelPostReactionEvent.sender = client.self!!.getGuildUser(event.extra.get("user_id").asString,
                        channelPostReactionEvent.guild.id)!!
                    channelPostReactionEvent.targetId = event.extra.get("msg_id").asString
                    channelPostReactionEvent.self = client.self!!
                    callEvent(channelPostReactionEvent)
                }
                "deleted_reaction" -> {
                    val channelCancelReactionEvent = Gson().fromJson(json, ChannelCancelReactionEvent::class.java)
                    channelCancelReactionEvent.channel =
                        client.self!!.getChannel(event.extra.get("channel_id").asString)!! as TextChannel
                    channelCancelReactionEvent.emoji = Emoji(
                        event.extra.get("emoji").asJsonObject.get("id").asString,
                        event.extra.get("emoji").asJsonObject.get("name").asString
                    )
                    channelCancelReactionEvent.guild = channelCancelReactionEvent.channel.guild
                    channelCancelReactionEvent.sender = client.self!!.getGuildUser(event.extra.get("user_id").asString,
                        channelCancelReactionEvent.guild.id)!!
                    channelCancelReactionEvent.targetId = event.extra.get("msg_id").asString
                    channelCancelReactionEvent.self = client.self!!
                    callEvent(channelCancelReactionEvent)
                }
                "message_btn_click" -> {
                    val cardButtonClickEvent = Gson().fromJson(json, CardButtonClickEvent::class.java)
                    cardButtonClickEvent.channel =
                        client.self!!.getChannel(event.extra.get("target_id").asString) as TextChannel
                    cardButtonClickEvent.sender = client.self!!.getUser(event.extra.get("user_id").asString)
                    cardButtonClickEvent.targetId = event.extra.get("msg_id").asString
                    cardButtonClickEvent.value = event.extra.get("value").asString
                    cardButtonClickEvent.self = client.self!!
                    callEvent(cardButtonClickEvent)
                }
                "private_added_reaction" -> {
                    val directPostReactionEvent = Gson().fromJson(json, DirectPostReactionEvent::class.java)
                    directPostReactionEvent.sender =
                        client.self!!.chattingUsers[event.extra["user_id"].asString]!!.value
                    directPostReactionEvent.sender.update()
                    directPostReactionEvent.emoji = Emoji(
                        event.extra.get("emoji").asJsonObject.get("id").asString,
                        event.extra.get("emoji").asJsonObject.get("name").asString
                    )
                    directPostReactionEvent.content = event.extra["msg_id"].asString
                    directPostReactionEvent.self = client.self!!
                    callEvent(directPostReactionEvent)
                }
                "private_deleted_reaction" -> {
                    val directCancelReactionEvent = Gson().fromJson(json, DirectCancelReactionEvent::class.java)
                    directCancelReactionEvent.sender =
                        client.self!!.chattingUsers[event.extra["user_id"].asString]!!.value
                    directCancelReactionEvent.sender.update()
                    directCancelReactionEvent.emoji = Emoji(
                        event.extra.get("emoji").asJsonObject.get("id").asString,
                        event.extra.get("emoji").asJsonObject.get("name").asString
                    )
                    directCancelReactionEvent.content = event.extra["msg_id"].asString
                    directCancelReactionEvent.self = client.self!!
                    callEvent(directCancelReactionEvent)
                }
                "updated_guild" -> {
                    val guild: Guild = client.self!!.guilds[event.extra["id"].asString]!!
                    guild.updateByJson(event.extra)
                    callEvent(GuildUpdateEvent(guild, client.self!!))
                }
                "deleted_guild" -> {
                    val id = event.extra["id"].asString
                    val guild: Guild = client.self!!.guilds[id]!!
                    (client.self!!.guilds as MutableMap).remove(id)
                    callEvent(GuildDeleteEvent(guild, client.self!!))
                }
                "added_channel" -> {
                    val guild: Guild = client.self!!.guilds[event.extra["guild_id"].asString]!!
                    guild.update()
                    val channel = guild.lazyChannels[event.extra["id"].asString]!!.value
                    callEvent(ChannelAddedEvent(channel, client.self!!))
                }
                "updated_channel" -> {
                    val channel =
                        client.self!!.guilds[event.extra["guild_id"].asString]!!.lazyChannels[event.extra["id"].asString]!!.value
                    channel.update()
                    callEvent(ChannelUpdateEvent(channel, client.self!!))
                }
                "deleted_channel" -> {
                    val id = event.extra["id"].asString
                    val guild: Guild = client.self!!.guilds[event.targetId]!!
                    val channel = guild.lazyChannels[id].let {
                        if (it?.isInitialized() == true) it.value
                        else null
                    }
                    (guild.lazyChannels as MutableMap).remove(id)
                    callEvent(ChannelDeletedEvent(id, channel, client.self!!))
                }
                "updated_guild_member" -> {
                    val guild = client.self!!.guilds[event.targetId]!!
                    val user = client.self!!.getGuildUser(event.extra.get("user_id").asString, guild.id)!!
                    user.update()
                    callEvent(GuildMemberUpdateEvent(client.self!!, guild, user, user.nickname))
                }
                "joined_guild" -> {
                    val guild = client.self!!.guilds[event.targetId]!!
                    guild.update()
                    val user = client.self!!.getGuildUser(event.extra.get("user_id").asString, guild.id)!!
                    callEvent(GuildUserJoinEvent(client.self!!, guild, user))
                }
                "exited_guild" -> {
                    val guild = client.self!!.guilds[event.targetId]!!
                    val user = client.self!!.getGuildUser(event.extra.get("user_id").asString, guild.id)!!
                    callEvent(GuildUserExitEvent(client.self!!, guild, user))
                }
                "self_joined_guild" -> {
                    val guild = Guild(client, event.extra["guild_id"].asString)
                    guild.update()
                    (client.self!!.guilds as MutableMap) += (event.extra["guild_id"].asString to guild)
                    callEvent(SelfJoinedGuildEvent(client.self!!, guild))
                }
                "self_exited_guild" -> {
                    val guild = client.self!!.guilds[event.extra["guild_id"].asString]!!
                    (client.self!!.guilds as MutableMap).remove(event.extra["guild_id"].asString)
                    callEvent(SelfExitedGuildEvent(client.self!!, guild))
                }
                "added_block_list" -> {
                    //TODO
                }
                "deleted_block_list" -> {
                    //TODO
                }
                "added_role" -> {
                    //TODO
                }
                "deleted_role" -> {
                    //TODO
                }
                "updated_role" -> {
                    //TODO
                }
                "user_updated" -> {
                    //TODO
                    //private chat
                }
            }
        } else if (event.channelType == MessageEvent.ChannelType.GROUP) {
            val channelMessageEvent = Gson().fromJson(json, ChannelMessageEvent::class.java)
            val guild = client.self!!.guilds[json["extra"].asJsonObject["guild_id"].asString]
            channelMessageEvent.guild = guild!!
            val channel = guild.lazyChannels[json["target_id"].asString]!!.value
            channelMessageEvent.channel = channel as TextChannel
            val user = client.self!!.getGuildUser(json["author_id"].asString, guild.id)!!
            channelMessageEvent.sender = user
            callEvent(channelMessageEvent)
        } else if (event.channelType == MessageEvent.ChannelType.PERSON) {
            // process
            if (client.self!!.chattingUsers[event.authorId] == null) {
                client.self!!.updatePrivateChatUser(event.authorId)
            }
            val directMessageEvent = Gson().fromJson(json, DirectMessageEvent::class.java)
            directMessageEvent.sender = client.self!!.chattingUsers[event.authorId]!!.value
            directMessageEvent.sender.update()
            callEvent(directMessageEvent)
        }
    }

    inline fun <reified T> addListener(noinline handler: T.() -> Unit): SingleEventHandler<T> where T : Event {
        val eventHandler = SingleEventHandler(handler, this)
        if (listeners.contains(T::class)) {
            listeners[T::class]!!.add(eventHandler)
        } else {
            listeners[T::class] = mutableListOf(eventHandler)
        }
        return eventHandler
    }

    fun addClassListener(listener: Listener1) {
        classListeners.add(listener)
    }

    fun addCommand(listener: (CommandDispatcher<CommandSource>) -> Unit) {
        listener(dispatcher)
    }
}