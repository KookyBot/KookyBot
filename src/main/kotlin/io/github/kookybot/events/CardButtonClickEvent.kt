package io.github.kookybot.events

import com.google.gson.JsonObject
import io.github.kookybot.contract.Self
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.contract.User

/**
 * TargetId此处用来指代被点击的card的message id
 */
class CardButtonClickEvent(
    @field:Transient
    override var self: Self,
    var value: String,
    @Transient
    var channel: TextChannel?,
    @Transient
    var sender: User,
    var clickedMessageId: String,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String,
    extra: JsonObject = JsonObject(),
): MessageEvent(self ,_channelType, _type, targetId, authorId, content, sid, timestamp, extra) {
}