package io.github.kookybot.kookybot.events.self

import io.github.kookybot.kookybot.events.Event
import io.github.kookybot.kookybot.message.SelfMessage

/**
 * 自己
 */
class SelfMessageEvent(
    val selfMessage: SelfMessage
): Event