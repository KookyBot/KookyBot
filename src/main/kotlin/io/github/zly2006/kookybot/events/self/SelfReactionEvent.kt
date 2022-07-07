package io.github.zly2006.kookybot.events.self

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.events.MessageEvent
import io.github.zly2006.kookybot.utils.Emoji

class SelfReactionEvent(
    client: Client,
    messageEvent: MessageEvent,
    emoji: Emoji
) {
    fun cancel() {

    }
}