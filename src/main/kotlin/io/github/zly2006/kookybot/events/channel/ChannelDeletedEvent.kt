package io.github.zly2006.kookybot.events.channel

import io.github.zly2006.kookybot.contract.Channel
import io.github.zly2006.kookybot.events.Event

class ChannelDeletedEvent (
    val channel: Channel
): Event