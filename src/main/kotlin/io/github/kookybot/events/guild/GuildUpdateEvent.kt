package io.github.kookybot.events.guild

import io.github.kookybot.contract.Guild
import io.github.kookybot.events.Event

class GuildUpdateEvent (
    val guild: Guild
): Event