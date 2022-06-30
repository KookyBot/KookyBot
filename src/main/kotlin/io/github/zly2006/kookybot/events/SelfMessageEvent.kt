package io.github.zly2006.kookybot.events

import io.github.zly2006.kookybot.message.SelfMessage

/**
 * 自己
 */
class SelfMessageEvent(
    val selfMessage: SelfMessage
): Event