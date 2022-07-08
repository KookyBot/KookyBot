package io.github.kookybot.kookybot.events.direct

import io.github.kookybot.kookybot.contract.PrivateChatUser
import io.github.kookybot.kookybot.events.MessageEvent

open class DirectMessageEvent (
    @field:Transient
    var sender: PrivateChatUser,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String
): MessageEvent(_channelType, _type, targetId, authorId, content, sid, timestamp)