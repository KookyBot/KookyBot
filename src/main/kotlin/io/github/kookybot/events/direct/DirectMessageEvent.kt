package io.github.kookybot.events.direct

import io.github.kookybot.contract.PrivateChatUser
import io.github.kookybot.events.MessageEvent

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