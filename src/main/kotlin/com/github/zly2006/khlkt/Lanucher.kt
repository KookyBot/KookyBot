package com.github.zly2006.khlkt
import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.events.ChannelMessageEvent
import kotlinx.coroutines.awaitCancellation
import java.io.File

suspend fun main() {
    if (!File("data/").exists())
        File("data/").mkdir()
    if (!File("data/token.txt").isFile) {
        File("data/token.txt").createNewFile()
        println("please fill your token in data/token/txt")
        return
    }
    val token = File("data/token.txt").readText()
    val client = Client(token)
    val self = client.start()
    client.eventManager.addListener<ChannelMessageEvent> {
        if (content.contains("hello")) {
            channel.sendCardMessage {
                Card {
                    HeaderModule(anElement { PlainTextElement("Hello") })
                    Divider()
                }
            }
        }
    }
    self.id
    awaitCancellation()
}
