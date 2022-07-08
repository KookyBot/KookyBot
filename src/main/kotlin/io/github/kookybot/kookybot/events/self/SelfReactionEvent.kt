package io.github.kookybot.kookybot.events.self

import io.github.kookybot.kookybot.client.Client
import io.github.kookybot.kookybot.events.MessageEvent
import io.github.kookybot.kookybot.utils.Emoji

class SelfReactionEvent(
    client: Client,
    messageEvent: MessageEvent,
    emoji: Emoji
) {
    fun cancel() {

    }
}