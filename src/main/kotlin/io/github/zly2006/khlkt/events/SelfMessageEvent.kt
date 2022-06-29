package io.github.zly2006.khlkt.events

import io.github.zly2006.khlkt.message.SelfMessage

/**
 * 自己
 */
class SelfMessageEvent(
    val selfMessage: SelfMessage
): Event