package io.github.kookybot.test.api

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import io.github.kookybot.client.Client
import io.github.kookybot.commands.CommandSource
import io.github.kookybot.commands.StringListArgumentType
import io.github.kookybot.message.CardMessage
import io.github.kookybot.message.ImageMessage
import io.github.kookybot.message.SelfMessage
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

suspend fun main() {
    val token = File("data/token.txt").readText()
    val client = Client(token) {
        withDefaultCommands()
    }
    val self = client.start()
    val logger = LoggerFactory.getLogger("ApiTest")
    client.addCommand { dispatcher ->
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("lottery")
            .then(argument<CommandSource?, String?>("name", StringArgumentType.word())
                .then(argument<CommandSource?, Int?>("time", IntegerArgumentType.integer(1))
                    .then(argument<CommandSource?, Int?>("nums", IntegerArgumentType.integer(1))
                        .executes {
                            it.run {
                                if (source.channel == null) {
                                    return@executes 0
                                }
                                val list: MutableList<String> = mutableListOf()
                                val num = IntegerArgumentType.getInteger(it, "nums")
                                val name = StringArgumentType.getString(it, "name")
                                source.channel!!.sendCardMessage {
                                    Card {
                                        HeaderModule(PlainTextElement(name))
                                        CountdownModule(
                                            mode = "hour",
                                            startTime = Calendar.getInstance().timeInMillis + 1000,
                                            endTime = Calendar.getInstance().timeInMillis + (IntegerArgumentType.getInteger(
                                                it,
                                                "time") * 1000) + 1000,
                                        )
                                        SectionModule(
                                            PlainTextElement("点击右侧按钮参与抽奖"),
                                            ButtonElement(
                                                text = PlainTextElement("Click"),
                                                onclick = {
                                                    if (!list.contains(it.sender.id)) {
                                                        it.channel!!.sendMessage(
                                                            "参与成功",
                                                            it.sender.atGuild(source.channel!!.guild)
                                                        )
                                                        list.add(it.sender.id)
                                                    } else {
                                                        it.channel!!.sendMessage(
                                                            "你已经参与过了",
                                                            it.sender.atGuild(source.channel!!.guild)
                                                        )
                                                    }
                                                })
                                        )
                                    }
                                }
                                Thread {
                                    Thread.sleep((IntegerArgumentType.getInteger(it, "time") * 1000L + 1000))
                                    val winners: MutableList<String> = mutableListOf()
                                    for (ignored in (0 until num)) {
                                        if (list.isNotEmpty()) {
                                            val id = list.random()
                                            winners.add(id)
                                            list.removeAll(listOf(id))
                                        }
                                    }
                                    source.channel!!.sendMessage(
                                        "恭喜以下参与者获得 **${name}**：" +
                                                winners.joinToString {
                                                    "(met)$it(met)"
                                                }
                                    )
                                }.start()
                            }
                            0
                        }
                    )
                )
            )
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("vote")
            .requires { it.hasPermission("kooky.operator") }
            .then(argument<CommandSource?, String?>("name", StringArgumentType.word())
                .then(argument<CommandSource?, MutableList<String>?>("option",
                    StringListArgumentType.stringList())
                    .executes {
                        val name = StringArgumentType.getString(it, "name")
                        val args = StringListArgumentType.getStringList(it, "option")

                        val list: List<MutableList<String>> = (0 until args.size).map { mutableListOf() }
                        var selfMessage: SelfMessage? = null
                        fun CardMessage.MessageScope.card() {
                            Card {
                                HeaderModule(PlainTextElement(name))
                                for (i in (0 until args.size)) {
                                    SectionModule(
                                        text = MarkdownElement(args[i]),
                                        accessory = ButtonElement(
                                            text = PlainTextElement("选择"),
                                        ) {
                                            with(it) {
                                                if (list.map { it.contains(sender.id) }.contains(true)) {
                                                    channel!!.sendMessage("voted", sender.atGuild(channel!!.guild))
                                                    return@with
                                                }
                                                list[i].add(sender.id)
                                                channel!!.sendMessage("ok", sender.atGuild(channel!!.guild))
                                                selfMessage!!.edit(CardMessage(client) {
                                                    card()
                                                }.content())
                                            }
                                        }
                                    )
                                    ContextModule {
                                        MarkdownElement("> ${
                                            list[i].firstOrNull()?.let { "(met)$it(met)" } ?: ""
                                        }等${list[i].size}人选择了此选项")
                                    }
                                }
                            }
                        }

                        val card = CardMessage(client) {
                            card()
                        }
                        selfMessage = it.source.channel!!.sendMessage(card)

                        0
                    }
                )
            )
        )
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource?>("kooky")
            .executes {
                it.source.channel?.let {
                    ImageMessage(
                        client = client,
                        file = File("data/kooky.png")
                    ).send(it)
                }
                0
            }
        )
    }
    while (true) {
        client.eventManager.parseCommand(readln())
    }
}
