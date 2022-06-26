package com.github.zly2006.khlkt.events

import com.github.zly2006.khlkt.contract.Channel
import com.github.zly2006.khlkt.contract.Guild
import com.github.zly2006.khlkt.contract.GuildUser

class ChannelMessageEvent(
    @field:Transient
    var channel: Channel,
    @field:Transient
    var sender: GuildUser,
    @field:Transient
    var guild: Guild,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String
): MessageEvent(_channelType, _type, targetId, authorId, content, sid, timestamp) {

}