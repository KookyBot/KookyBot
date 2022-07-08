package io.github.kookybot.kookybot.events.direct

import io.github.kookybot.kookybot.contract.PrivateChatUser
import io.github.kookybot.kookybot.utils.Emoji

class DirectCancelReactionEvent (
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