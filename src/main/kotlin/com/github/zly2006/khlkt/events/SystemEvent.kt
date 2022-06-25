package com.github.zly2006.khlkt.events

class SystemEvent(
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String
) : Event(_channelType, _type, targetId, authorId, content, sid, timestamp) {
}