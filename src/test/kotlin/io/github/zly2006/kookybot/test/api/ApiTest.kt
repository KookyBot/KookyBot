package io.github.zly2006.kookybot.test.api

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.events.ChannelMessageEvent
import org.slf4j.LoggerFactory
import java.io.File

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
            }
        }
    }
    while (true) {
        var cmd = readln()
        if (cmd == "status") {
            println("${client.status} ${client.pingStatus} ${client.pingDelay}")
        }
    }
}
