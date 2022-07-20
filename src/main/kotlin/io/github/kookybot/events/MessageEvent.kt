/* KookyBot - a SDK of <https://www.kookapp.cn> for JVM platform
Copyright (C) 2022, zly2006 & contributors

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

package io.github.kookybot.events

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.kookybot.client.Client
import io.github.kookybot.contract.Self
import io.github.kookybot.contract.User
import io.github.kookybot.events.self.SelfReactionEvent
import io.github.kookybot.utils.Emoji
import org.apache.directory.api.ldap.model.cursor.Tuple

open class MessageEvent(
    self: Self,
    @field:SerializedName("channel_type")
    private var _channelType: String,
    @field:SerializedName("type")
    private var _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    messageId: String,
    timestamp: String,
    extra: JsonObject = JsonObject(),
) : Event {
    @field:Transient
    override var self: Self = self
        internal set

    @field:SerializedName("target_id")
    var targetId: String = targetId
        internal set

    @field:SerializedName("author_id")
            /**
             * 非必要不建议使用，需要请issue
             */
    var authorId: String = authorId
        internal set

    @field:SerializedName("content")
            /**
             * 非必要不建议使用，需要请issue
             */
    var content: String = content
        internal set

    @field:SerializedName("message_id")
    var messageId: String = messageId
        internal set

    @field:SerializedName("msg_timestamp")
    var timestamp: String = timestamp
        internal set
    var extra: JsonObject = extra
        internal set

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

    fun postReaction(emoji: Emoji): SelfReactionEvent {
        with(self.client) {
            sendRequest(requestBuilder(Client.RequestType.ADD_REACTION, "emoji" to emoji.id, "msg_id" to messageId))
        }
        return SelfReactionEvent(this,emoji,self)
    }

    val reactions: List<Tuple<out User, Emoji>>
        get() = TODO()
}