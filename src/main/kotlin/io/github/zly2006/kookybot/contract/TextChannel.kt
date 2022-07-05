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

    fun sendMessage(message: Message, tempTarget: GuildUser) {
        var type = 9
        when (message) {
            is CardMessage -> type = 10
            is MarkdownMessage -> type = 9
        }
        client.sendChannelMessage(
            type = type,
            content = message.content(),
            target = this,
            quote = message.quote,
            tempTarget = tempTarget.id
        )
    }

    fun sendMessage(message: String, tempTarget: GuildUser? = null) {
        if (tempTarget == null)
            sendMessage(MarkdownMessage(client, message))
        else
            sendMessage(MarkdownMessage(client, message), tempTarget)
    }

    fun sendCardMessage(tempTarget: GuildUser? = null, content: CardMessage.MessageScope.() -> Unit) {
        val msg = CardMessage(client = client,
            contentBuilder = content)
        if (tempTarget == null)
            sendMessage(msg)
        else
            sendMessage(msg, tempTarget)
    }
}