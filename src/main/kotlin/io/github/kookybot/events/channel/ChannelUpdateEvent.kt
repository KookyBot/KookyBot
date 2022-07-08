package io.github.kookybot.events.channel

import io.github.kookybot.contract.Channel
import io.github.kookybot.events.Event

class ChannelUpdateEvent (
    val channel: Channel
): Event