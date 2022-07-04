package io.github.zly2006.kookybot.events.direct

import io.github.zly2006.kookybot.contract.PrivateChatUser
import io.github.zly2006.kookybot.events.MessageEvent

class DirectMessageEvent (
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