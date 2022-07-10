package io.github.kookybot.events.self

import io.github.kookybot.contract.Self
import io.github.kookybot.events.Event
import io.github.kookybot.message.SelfMessage

/**
 * 自己
 */
class SelfMessageEvent(
    val selfMessage: SelfMessage,
    override val self: Self,
): Event