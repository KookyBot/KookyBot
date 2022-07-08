package io.github.kookybot.kookybot.events.channel

import io.github.kookybot.kookybot.contract.Channel
import io.github.kookybot.kookybot.events.Event

class ChannelUpdateEvent (
    val channel: Channel
): Event