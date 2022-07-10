package io.github.kookybot.events.guild

import io.github.kookybot.contract.Guild
import io.github.kookybot.contract.Self
import io.github.kookybot.events.Event

class GuildUpdateEvent (
    val guild: Guild, override val self: Self
): Event