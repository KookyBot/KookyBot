package io.github.zly2006.kookybot.test.me

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.events.ChannelMessageEvent
import io.github.zly2006.kookybot.message.At
import java.io.File

suspend fun main() {
    val client = Client(File("data/token.txt").readText())
    val self = client.start()
    client.eventManager.addListener<ChannelMessageEvent> {
        if (channel.id == "8640383454681406" || guild.id == "9958078697384496") {
            // process
            if (content == "hello") {
                channel.sendMessage("hello" + At(sender))
            }
        }
    }
}