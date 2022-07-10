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
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.kookybot.client.Client
import io.github.kookybot.commands.*
import io.github.kookybot.contract.Guild
import io.github.kookybot.contract.TextChannel
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlin.reflect.KClass
import kotlin.system.exitProcess
import io.github.kookybot.events.Listener as Listener1


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

    @Deprecated("use brigadier")
    val commands: MutableList<Command> = mutableListOf()

    // 用于click处理
    val clickEvents: MutableList<Pair<String, (CardButtonClickEvent) -> Unit>> = mutableListOf()

    fun parseCommand(consoleCommand: String) {
        if (consoleCommand == "") return
        try {
            dispatcher.execute(consoleCommand, CommandSource())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parseCommand(event: MessageEvent) {
        if (!event.content.startsWith('/')) return
        @Deprecated("")
        if (commands.find { event.content.startsWith("/${it.name} ") } != null) {
            val args = mutableListOf<String>()
            event.content.substring(1 until event.content.length).split(' ').map {
                if (it == "") listOf()
                else if (it.contains("(met")) {
                    listOf(it)
                } else
                    listOf(it)
            }.forEach { args.addAll(it) }
            if (args.size == 0) {
                when (event) {
                    is DirectMessageEvent -> event.sender.sendMessage("找不到命令")
                    is ChannelMessageEvent -> event.channel.sendMessage("(met)${event.sender.id}(met)找不到命令")
                }
                return
            }
            val command = commands.find { it.name == args[0] }!!
            val source = when (event) {
                is DirectMessageEvent -> CommandContext(
                    user = event.sender,
                    label = args[0],
                    args = if (args.size == 1) arrayOf() else args.subList(1, args.size).toTypedArray(),
                    command = command,
                    channel = null
                )
                is ChannelMessageEvent -> CommandContext(
                    user = event.sender,
                    label = args[0],
                    args = if (args.size == 1) arrayOf() else args.subList(1, args.size).toTypedArray(),
                    command = command,
                    channel = event.channel
                )
                else -> throw Exception()
            }
            try {
                var invoke = true
                for (annotation in command.javaClass.getMethod("onExecute", CommandContext::class.java).annotations) {
                    if (annotation is RequireGuild) {
                        invoke = false
                        if (event is ChannelMessageEvent) {
                            if (event.guild.id == annotation.id) {
                                invoke = true
                                break
                            }
                        }
                    }
                    if (annotation is RequireChannel) {
                        invoke = false
                        if (event is ChannelMessageEvent) {
                            if (event.channel.id != annotation.id) {
                                invoke = true
                                break
                            }
                        }
                    }
                }
                if (invoke) {
                    command.onExecute(source)
                }
            } catch (e: Exception) {
                when (event) {
                    is DirectMessageEvent -> event.sender.sendMessage("执行命令时发生了错误，请联系开发者。详细信息：\n```\n${e}\n```")
                    is ChannelMessageEvent -> event.channel.sendMessage("(met)${event.sender.id}(met)执行命令时发生了错误，请联系开发者。详细信息：\n```\n${e}\n```")
                }
                e.printStackTrace()
            }
            return
        }
        val source = when (event) {
            is ChannelMessageEvent -> CommandSource(
                type = CommandSource.Type.Channel,
                channel = event.channel,
                user = event.sender
            )
            is DirectMessageEvent -> CommandSource(
                type = CommandSource.Type.Private,
                user = event.sender,
                private = event.sender
            )
            else -> throw Exception("invalid type")
        }
        try {
            dispatcher.execute(event.content.substring(1), source)
        } catch (e: CommandSyntaxException) {
            when (event) {
                is DirectMessageEvent -> event.sender.sendMessage("命令语法不正确。详细信息：\n```\n${e.localizedMessage}\n```")
                is ChannelMessageEvent -> event.channel.sendMessage("(met)${event.sender.id}(met)命令语法不正确。详细信息：\n```\n${e.localizedMessage}\n```")
            }
            e.printStackTrace()
        } catch (e: Exception) {
            when (event) {
                is DirectMessageEvent -> event.sender.sendMessage("执行命令时发生了错误，请联系开发者。详细信息：\n```\n${e}\n```")
                is ChannelMessageEvent -> event.channel.sendMessage("(met)${event.sender.id}(met)执行命令时发生了错误，请联系开发者。详细信息：\n```\n${e}\n```")
            }
            e.printStackTrace()
        }
    }

    inline fun <reified T : Event> callEvent(event: T) {
        when (event) {
            is CardButtonClickEvent -> {
                clickEvents.forEach {
                    if (it.first == event.value)
                        it.second(event)
                }
            }
            is DirectMessageEvent -> if (event.eventType == MessageEvent.EventType.MARKDOWN) parseCommand(event)
            is ChannelMessageEvent -> if (event.eventType == MessageEvent.EventType.MARKDOWN) parseCommand(event)
        }
        listeners[T::class]?.forEach { it ->
            try {
                it.handle(event)
            } catch (e: Exception) {
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
                            it.channels.firstOrNull {
                                it.id == json.get("target_id").asString
                            }
                        }.firstOrNull { it != null }!!
                        MessageEvent.ChannelType.PERSON -> client.self!!.chattingUsers.firstOrNull { it.id == json.get("target_id").asString }!!
                        else -> throw Exception("Invalid channel type.")
                    } as TextChannel,
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
                    callEvent(channelCancelReactionEvent)
                }
                "message_btn_click" -> {
                    val cardButtonClickEvent = Gson().fromJson(json, CardButtonClickEvent::class.java)
                    cardButtonClickEvent.channel =
                        client.self!!.getChannel(event.extra.get("target_id").asString) as TextChannel
                    cardButtonClickEvent.sender = client.self!!.getUser(event.extra.get("user_id").asString)
                    cardButtonClickEvent.targetId = event.extra.get("msg_id").asString
                    cardButtonClickEvent.value = event.extra.get("value").asString
                    callEvent(cardButtonClickEvent)
                }
                "private_added_reaction" -> {
                    val directPostReactionEvent = Gson().fromJson(json, DirectPostReactionEvent::class.java)
                    directPostReactionEvent.sender =
                        client.self!!.chattingUsers.find { it.code == event.extra.get("chat_code").asString }!!
                    directPostReactionEvent.sender.update()
                    directPostReactionEvent.emoji = Emoji(
                        event.extra.get("emoji").asJsonObject.get("id").asString,
                        event.extra.get("emoji").asJsonObject.get("name").asString
                    )
                    callEvent(directPostReactionEvent)
                }
                "private_deleted_reaction" -> {
                    val directCancelReactionEvent = Gson().fromJson(json, DirectCancelReactionEvent::class.java)
                    directCancelReactionEvent.sender =
                        client.self!!.chattingUsers.find { it.code == event.extra.get("chat_code").asString }!!
                    directCancelReactionEvent.sender.update()
                    directCancelReactionEvent.emoji = Emoji(
                        event.extra.get("emoji").asJsonObject.get("id").asString,
                        event.extra.get("emoji").asJsonObject.get("name").asString
                    )
                    callEvent(directCancelReactionEvent)
                }
                "updated_guild" -> {
                    val guild: Guild = client.self!!.guilds.find { it.id == event.extra.get("id").asString }!!
                    guild.updateByJson(event.extra)
                    callEvent(GuildUpdateEvent(guild, client.self!!))
                }
                "deleted_guild" -> {
                    val guild: Guild = client.self!!.guilds.find { it.id == event.extra.get("id").asString }!!
                    (client.self!!.guilds as MutableList).removeIf { it.id == guild.id }
                    callEvent(GuildDeleteEvent(guild, client.self!!))
                }
                "added_channel" -> {
                    val guild: Guild = client.self!!.guilds.find { it.id == event.extra.get("guild_id").asString }!!
                    guild.update()
                    val channel = guild.channels.find { it.id == event.extra.get("id").asString }!!
                    callEvent(ChannelAddedEvent(channel, client.self!!))
                }
                "updated_channel" -> {
                    val guild: Guild = client.self!!.guilds.find { it.id == event.extra.get("guild_id").asString }!!
                    guild.update()
                    val channel = guild.channels.find { it.id == event.extra.get("id").asString }!!
                    callEvent(ChannelUpdateEvent(channel, client.self!!))
                }
                "deleted_channel" -> {
                    val guild: Guild = client.self!!.guilds.find { it.id == event.extra.get("guild_id").asString }!!
                    val channel = guild.channels.find { it.id == event.extra.get("id").asString }!!
                    guild.channels -= channel
                    callEvent(ChannelDeletedEvent(channel, client.self!!))
                }
                "updated_guild_member" -> {
                    val guild = client.self!!.guilds.find { it.id == event.targetId }!!
                    val user = client.self!!.getGuildUser(event.extra.get("user_id").asString, guild.id)!!
                    user.update()
                    callEvent(GuildMemberUpdateEvent(client.self!!, guild, user, user.nickname))
                }
                "joined_guild" -> {
                    val guild = client.self!!.guilds.find { it.id == event.targetId }!!
                    val user = client.self!!.getGuildUser(event.extra.get("user_id").asString, guild.id)!!
                    callEvent(GuildUserJoinEvent(client.self!!, guild, user))
                }
                "exited_guild" -> {
                    val guild = client.self!!.guilds.find { it.id == event.targetId }!!
                    val user = client.self!!.getGuildUser(event.extra.get("user_id").asString, guild.id)!!
                    callEvent(GuildUserExitEvent(client.self!!, guild, user))
                }
                "self_joined_guild" -> {
                    val guild = Guild(client, event.extra.get("guild_id").asString)
                    guild.update()
                    (client.self!!.guilds as MutableList).add(guild)
                    callEvent(SelfJoinedGuildEvent(client.self!!, guild))
                }
                "self_exited_guild" -> {
                    val guild = client.self!!.guilds.find { it.id == event.extra.get("guild_id").asString }!!
                    (client.self!!.guilds as MutableList).remove(guild)
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
            val guild = client.self!!.guilds.firstOrNull { it.id == json["extra"].asJsonObject["guild_id"].asString }
            channelMessageEvent.guild = guild!!
            val channel = guild.channels.firstOrNull { it.id == json["target_id"].asString }
            channelMessageEvent.channel = channel!! as TextChannel
            val user = client.self!!.getGuildUser(json["author_id"].asString, guild.id)!!
            channelMessageEvent.sender = user
            callEvent(channelMessageEvent)
        } else if (event.channelType == MessageEvent.ChannelType.PERSON) {
            // process
            if (client.self!!.chattingUsers.find { it.id == event.authorId } == null) {
                client.self!!.updatePrivateChatUser(event.authorId)
            }
            val directMessageEvent = Gson().fromJson(json, DirectMessageEvent::class.java)
            directMessageEvent.sender =
                client.self!!.chattingUsers.find { it.code == json.get("extra").asJsonObject.get("code").asString }!!
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

    fun addCommand(command: Command) {
        commands.add(command)
    }

    fun addCommand(listener: (CommandDispatcher<CommandSource>) -> Unit) {
        listener(dispatcher)
    }

    init {
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("help")
            .executes { context ->

                context.source.sendMessage(
                    dispatcher.getAllUsage(dispatcher.root, context.source, true).joinToString("") { "\n/${it}" }
                )
                0
            }
            .then(RequiredArgumentBuilder.argument<CommandSource?, String?>(
                "command",
                StringArgumentType.greedyString()
            )
                .executes { context ->
                    val parseResults: ParseResults<CommandSource> =
                        dispatcher.parse(StringArgumentType.getString(context, "command"), context.source);
                    if (parseResults.getContext().getNodes().isEmpty()) {
                        throw Exception("Command not found.")
                    } else {
                        context.source.sendMessage(buildString {
                            dispatcher.getSmartUsage(parseResults.context.nodes.last().node, context.source)
                                .forEach {
                                    append("/" + parseResults.reader.string + it)
                                    context.source
                                }
                        })

                        0
                    }
                })
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("echo")
            .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("text", StringArgumentType.greedyString())
                .executes {
                    it.source.sendMessage(StringArgumentType.getString(it, "text"))
                    0
                }
            )
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("stop")
            .requires { it.hasPermission("kooky.owner") }
            .executes {
                client.close()
                GlobalScope.coroutineContext.cancel()
                exitProcess(0)
            }
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("setowner")
            .requires { it.hasPermission("kooky.owner") }
            .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("uid", UserArgumentType.id())
                .executes {
                    client.permissionManager.setPermission(
                        perm = "kooky.owner",
                        user = UserArgumentType.getId(it, "uid"),
                        value = true
                    )
                    it.source.sendMessage("Set owner")
                    0
                }
            )
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("op")
            .requires { it.hasPermission("kooky.operator") }
            .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("scope", StringArgumentType.word())
                .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("name", UserArgumentType.id())
                    .executes {
                        val name = UserArgumentType.getId(it, "name")
                        when (StringArgumentType.getString(it, "scope")) {
                            "global" -> client.permissionManager.setPermission(
                                perm = "kooky.operator",
                                user = name,
                                value = true
                            )
                            "channel" -> client.permissionManager.setPermission(
                                perm = "kooky.operator",
                                user = name,
                                channelId = it.source.channel!!.id,
                                value = true
                            )
                            "guild" -> client.permissionManager.setPermission(
                                perm = "kooky.operator",
                                user = name,
                                guildId = it.source.channel!!.guild.id,
                                value = true
                            )
                        }
                        it.source.sendMessage("Oped")
                        0
                    }
                )
            )
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("permission")
            .then(LiteralArgumentBuilder.literal<CommandSource?>("me")
                .executes {
                    if (it.source.type == CommandSource.Type.Console) {
                        it.source.sendMessage("Console has all permissions.")
                        return@executes 0
                    }
                    val pm = it.source.user!!.client.permissionManager
                    it.source.sendMessage(
                        buildString {
                            append("global:\n")
                            pm.global[it.source.user!!.id]?.forEach {
                                append("  ${it.key} = ${it.value}\n")
                            }
                            if (it.source.channel != null) {
                                append("this guild:\n")
                                pm.guild[it.source.user!!.id]?.get(it.source.channel!!.guild.id)?.forEach {
                                    append("  ${it.key} = ${it.value}\n")
                                }
                                append("this channel:\n")
                                pm.channel[it.source.user!!.id]?.get(it.source.channel!!.id)?.forEach {
                                    append("  ${it.key} = ${it.value}\n")
                                }
                            }
                        }
                    )
                    0
                }
            )
        )
    }
}