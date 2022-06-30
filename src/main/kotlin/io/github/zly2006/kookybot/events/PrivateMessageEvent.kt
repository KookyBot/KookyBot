package io.github.zly2006.kookybot.events

import io.github.zly2006.kookybot.contract.PrivateChatUser

class PrivateMessageEvent (
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