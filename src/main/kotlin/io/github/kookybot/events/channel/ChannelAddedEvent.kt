package io.github.kookybot.events.channel

import io.github.kookybot.contract.Channel
import io.github.kookybot.events.Event

class ChannelAddedEvent (
    val channel: Channel
): Event