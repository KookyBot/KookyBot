package io.github.kookybot.kookybot.events.guild

import io.github.kookybot.kookybot.contract.Guild
import io.github.kookybot.kookybot.events.Event

/**
 * 注意：
 * 此时服务器已删除，不要通过其他方式尝试访问。
 */
class GuildDeleteEvent (
    val guild: Guild
): Event