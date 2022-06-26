/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
Copyright (C) <year>  <name of author>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package com.github.zly2006.khlkt.events

import com.google.gson.annotations.SerializedName

open class MessageEvent(
    @field:SerializedName("channel_type")
    private var _channelType: String,
    @field:SerializedName("type")
    private var _type: Int,
    @field:SerializedName("target_id")
    var targetId: String,
    @field:SerializedName("author_id")
    var authorId: String,
    @field:SerializedName("content")
    var content: String,
    @field:SerializedName("message_id")
    var sid: String,
    @field:SerializedName("msg_timestamp")
    var timestamp: String
): Event {
    enum class EventType {
        UNKNOWN,
        PLAIN_TEXT,
        IMAGE,
        VIDEO,
        FILE,
        VOICE,
        MARKDOWN,
        CARD,
        SYSTEM
    }
    enum class ChannelType {
        GROUP,
        BROADCAST,
        PERSON,
        UNKNOWN
    }
    val eventType get() = when (_type) {
        1 -> EventType.PLAIN_TEXT
        2 -> EventType.IMAGE
        3 -> EventType.VIDEO
        4 -> EventType.FILE
        8 -> EventType.VOICE
        9 -> EventType.MARKDOWN
        10 -> EventType.CARD
        255 -> EventType.SYSTEM
        else -> EventType.UNKNOWN
    }
    val channelType get() = when (_channelType) {
        "GROUP" -> ChannelType.GROUP
        "BROADCAST" -> ChannelType.BROADCAST
        "PERSON" -> ChannelType.PERSON
        else -> ChannelType.UNKNOWN
    }
}