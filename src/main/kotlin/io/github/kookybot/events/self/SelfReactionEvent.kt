package io.github.kookybot.events.self

import io.github.kookybot.client.Client
import io.github.kookybot.contract.Self
import io.github.kookybot.events.Event
import io.github.kookybot.events.MessageEvent
import io.github.kookybot.utils.Emoji

class SelfReactionEvent(
    val messageEvent: MessageEvent,
    val emoji: Emoji,
    override val self: Self
): Event {
    fun cancel() {
        with(self.client) {
            sendRequest(requestBuilder(Client.RequestType.ADD_REACTION, "emoji" to emoji.id, "msg_id" to messageEvent.messageId))
        }
    }
}