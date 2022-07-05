package io.github.zly2006.kookybot.events

import com.google.gson.JsonObject
import io.github.zly2006.kookybot.contract.TextChannel
import io.github.zly2006.kookybot.contract.User

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