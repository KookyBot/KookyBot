package io.github.kookybot.events.self

import io.github.kookybot.client.Client
import io.github.kookybot.events.MessageEvent
import io.github.kookybot.utils.Emoji

class SelfReactionEvent(
    client: Client,
    messageEvent: MessageEvent,
    emoji: Emoji
) {
    fun cancel() {

    }
}