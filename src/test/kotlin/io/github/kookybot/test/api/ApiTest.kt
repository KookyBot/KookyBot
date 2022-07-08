package io.github.kookybot.test.api

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.kookybot.client.Client
import io.github.kookybot.commands.Command
import io.github.kookybot.commands.CommandContext
import io.github.kookybot.commands.RequireChannel
import io.github.kookybot.commands.RequireGuild
import io.github.kookybot.events.channel.ChannelMessageEvent
import io.github.kookybot.message.CardMessage
import io.github.kookybot.message.SelfMessage
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

suspend fun main() {
    val token = File("data/token.txt").readText()
    val client = Client(token)
    val self = client.start()
    val logger = LoggerFactory.getLogger("ApiTest")
    client.eventManager.addListener<ChannelMessageEvent> {
        if (content.contains("hello")) {
            logger.info("hello")
            channel.sendCardMessage {
                Card {
                    HeaderModule(PlainTextElement("Hello"))
                    Divider()
                    SectionModule(
                        text = MarkdownElement("**Click Me!**"),
                        accessory = ButtonElement(
                            text = PlainTextElement("hi"),
                            onclick = {
                                it.channel?.sendMessage("hi~")
                                it.sender.talkTo().sendMessage("~~鬼魂：~~hi~")
                            })
                    )
                }
                Card {
                    SectionModule(
                        text = MarkdownElement("**执行命令测试**"),
                        accessory = ButtonElement(
                            text = PlainTextElement("run"),
                            value = "/echo hello",
                            click = CardMessage.ClickType.ExecuteCommand
                        )
                    )
                }
            }
        }
    }
    client.addCommand { dispatcher ->
        dispatcher.register(LiteralArgumentBuilder.literal(""))
    }
    client.eventManager.addCommand(object : Command("lottery") {
        override fun onExecute(context: CommandContext) {
            if (context.channel == null) return
            val list: MutableList<String> = mutableListOf()
            val num = context.args[2].toInt()
            context.channel!!.sendCardMessage {
                Card {
                    HeaderModule(PlainTextElement(context.args[0]))
                    CountdownModule(
                        mode = "hour",
                        startTime = Calendar.getInstance().timeInMillis + 1000,
                        endTime = Calendar.getInstance().timeInMillis + (context.args[1].toLong() * 1000) + 1000,
                    )
                    SectionModule(
                        PlainTextElement("点击右侧按钮参与抽奖"),
                        ButtonElement(
                            text = PlainTextElement("Click"),
                            onclick = {
                                if (!list.contains(it.sender.id)) {
                                    it.channel!!.sendMessage(
                                        "参与成功",
                                        it.sender.atGuild(context.channel!!.guild)
                                    )
                                    list.add(it.sender.id)
                                } else {
                                    it.channel!!.sendMessage(
                                        "你已经参与过了",
                                        it.sender.atGuild(context.channel!!.guild)
                                    )
                                }
                            })
                    )
                }
            }
            Thread {
                Thread.sleep(context.args[1].toLong() * 1000 + 1000)
                val winners: MutableList<String> = mutableListOf()
                for (ignored in (0 until num)) {
                    if (list.isNotEmpty()) {
                        val id = list.random()
                        winners.add(id)
                        list.removeAll(listOf(id))
                    }
                }
                context.channel!!.sendMessage(
                    "恭喜以下参与者获得 **${context.args[0]}**：" +
                            winners.joinToString {
                                "(met)$it(met)"
                            }
                )
            }.start()
        }
    })
    client.eventManager.commands.add(object : Command("test") {
        override fun onExecute(context: CommandContext) {
            when (context.args[0]) {
                "add_channel" -> {
                    context.channel!!.sendMessage(context.channel!!.guild.createTextChannel(context.args[1]).id)
                }
            }
        }
    })
    client.eventManager.addCommand(object : Command("vote") {
        var list: List<MutableList<String>> = listOf()
        var selfMessage: SelfMessage? = null
        var context: CommandContext? = null
        fun CardMessage.MessageScope.card() {
            Card {
                HeaderModule(PlainTextElement(context!!.args[0]))
                for (i in (1 until context!!.args.size)) {
                    SectionModule(
                        text = MarkdownElement(context!!.args[i]),
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
                        MarkdownElement("> ${list[i].firstOrNull()?.let { "(met)$it(met)" } ?: ""}等${list[i].size}人选择了此选项")
                    }
                }
            }
        }
        @RequireChannel(id = "7118025577525135")
        @RequireGuild(id = "9958078697384496")
        override fun onExecute(context: CommandContext) {
            list = (0 until  context.args.size).map { mutableListOf() }
            this.context = context
            val card = CardMessage(client) {
                card()
            }
            selfMessage = context.channel!!.sendMessage(card)
        }
    })
    while (true) {
        var cmd = readln()
        if (cmd == "status") {
            println("${client.status} ${client.pingStatus} ${client.pingDelay}")
        }
        if (cmd == "stop") {
            client.close()
            return
        }
        if (cmd == "ping") {
            client.ping()
        }
    }
}
