package io.github.kookybot.contract

import com.google.gson.annotations.SerializedName
import io.github.kookybot.client.Client
import io.github.kookybot.message.CardMessage
import io.github.kookybot.message.MarkdownMessage
import io.github.kookybot.message.Message
import io.github.kookybot.message.SelfMessage

class TextChannel(client: Client, id: String, guild: Guild) : Channel(client, id, guild), MessageReceiver {
    @field:SerializedName("slow_mode")
    var slowMode: Int = 0
        internal set

    override fun sendMessage(message: Message): SelfMessage {
        var type = 9
        when (message) {
            is CardMessage -> type = 10
            is MarkdownMessage -> type = 9
        }
        return client.sendChannelMessage(
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