package io.github.kookybot.kookybot.events.guild

import io.github.kookybot.kookybot.contract.Guild
import io.github.kookybot.kookybot.events.Event

class GuildUpdateEvent (
    val guild: Guild
): Event