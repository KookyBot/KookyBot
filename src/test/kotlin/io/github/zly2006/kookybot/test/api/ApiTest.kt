package io.github.zly2006.kookybot.test.api

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.commands.Command
import io.github.zly2006.kookybot.commands.CommandSource
import io.github.zly2006.kookybot.contract.PrivateChatUser
import io.github.zly2006.kookybot.events.channel.ChannelMessageEvent
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
                }
                Card {
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
            }
        }
    }
    client.eventManager.commands.add(object : Command(
        name = "echo",
        alias = listOf("test_echo")
    ) {
        override fun onExecute(source: CommandSource) {
            if (source.channel != null) {
                source.channel!!.sendMessage(source.args[0])
            }
            else {
                (source.user as PrivateChatUser).sendMessage(source.args[0])
            }
        }
    })
    client.eventManager.commands.add(object : Command("stop", ) {
        override fun onExecute(source: CommandSource) {
            client.close()
            throw Error("stopped")
        }
    })
    client.eventManager.addCommand(object : Command("lottery") {
        override fun onExecute(source: CommandSource) {
            if (source.channel == null) return
            val list: MutableList<String> = mutableListOf()
            val num = source.args[2].toInt()
            source.channel!!.sendCardMessage {
                Card {
                    HeaderModule(PlainTextElement(source.args[0]))
                    CountdownModule(
                        mode = "hour",
                        startTime = Calendar.getInstance().timeInMillis+1000,
                        endTime = Calendar.getInstance().timeInMillis+(source.args[1].toLong()*1000)+1000,
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
                                }
                                else {
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
                Thread.sleep(source.args[1].toLong()*1000+1000)
                val winners: MutableList<String> = mutableListOf()
                for (ignored in (0 until num)) {
                    if (list.isNotEmpty()) {
                        val id = list.random()
                        winners.add(id)
                        list.removeAll(listOf(id))
                    }
                }
                source.channel!!.sendMessage(
                    "恭喜以下参与者获得 **${source.args[0]}**：" +
                            winners.joinToString {
                                "(met)$it(met)"
                            }
                )
            }.start()
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
    }
}
