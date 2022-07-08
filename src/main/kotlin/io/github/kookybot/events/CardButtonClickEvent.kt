package io.github.kookybot.events

import com.google.gson.JsonObject
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.contract.User

class CardButtonClickEvent (
    var value: String,
    @Transient
    var channel: TextChannel?,
    @Transient
    var sender: User,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String,
    extra: JsonObject = JsonObject()
): MessageEvent(_channelType, _type, targetId, authorId, content, sid, timestamp, extra) {

}