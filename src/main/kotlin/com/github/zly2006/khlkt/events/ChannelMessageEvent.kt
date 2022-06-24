package com.github.zly2006.khlkt.events

import com.github.zly2006.khlkt.contract.Channel
import com.github.zly2006.khlkt.contract.Guild
import com.github.zly2006.khlkt.contract.GuildUser
import com.github.zly2006.khlkt.utils.Cache

class ChannelMessageEvent(
    @Transient
    var channel: Cache<Channel>,
    @Transient
    var sender: Cache<GuildUser>,
    @Transient
    var guild: Cache<Guild>,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String
): Event(_channelType, _type, targetId, authorId, content, sid, timestamp) {

}