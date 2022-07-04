package io.github.zly2006.kookybot.contract

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.message.CardMessage
import io.github.zly2006.kookybot.message.MarkdownMessage
import io.github.zly2006.kookybot.message.Message

class TextChannel(client: Client, id: String, guild: Guild) : Channel(client, id, guild), MessageReceiver {
    override fun sendMessage(message: Message) {
        var type = 9
        when (message) {
            is CardMessage -> type = 10
            is MarkdownMessage -> type = 9
        }
        client.sendChannelMessage(
            type = type,
            content = message.content(),
            target = this,
            quote = message.quote
        )
    }

    fun sendMessage(message: String) {
        client.sendChannelMessage(
            content = message,
            target = this
        )
    }

    fun sendMarkdownMessage(message: String) {
        sendMessage(MarkdownMessage(client, message))
    }

    fun sendCardMessage(content: CardMessage.MessageScope.() -> Unit) {
        val msg = CardMessage(client = client,
            contentBuilder = content)
        sendMessage(msg)
    }
}