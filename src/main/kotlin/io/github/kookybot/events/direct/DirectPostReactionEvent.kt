package io.github.kookybot.events.direct

import io.github.kookybot.contract.PrivateChatUser
import io.github.kookybot.utils.Emoji

class DirectPostReactionEvent (
    @field:Transient
    var emoji: Emoji,
    sender: PrivateChatUser,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String,
): DirectMessageEvent(sender, _channelType, _type, targetId, authorId, content, sid, timestamp)