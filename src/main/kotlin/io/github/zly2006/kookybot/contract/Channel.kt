/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
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

package io.github.zly2006.kookybot.contract

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.message.CardMessage
import io.github.zly2006.kookybot.message.MarkdownMessage
import io.github.zly2006.kookybot.message.Message
import io.github.zly2006.kookybot.utils.DontUpdate
import io.github.zly2006.kookybot.utils.Updatable

enum class ChannelType {
    UNKNOWN,
    TEXT,
    VOICE,
}

class Channel(
    @field:Transient
    override val client: Client,
    override val id: String,
    @field:Transient
    val guild: Guild,
) : MessageReceiver(), Updatable {
    var name: String = ""
    @field:Transient
    var parent: Channel? = null
    var topic: String = ""
    @field:SerializedName("permission_sync")
    var permissionSync: Int = 0
    @field:SerializedName("is_category")
    var category: Boolean = false
    var level: Int = 0
    @field:SerializedName("slow_mode")
    var slowMode: Int = 0
    @field:DontUpdate
    var permissionOverwrites: List<RolePermissionOverwrite> = listOf()
    @field:DontUpdate
    var permissionUsers: List<UserPermissionOverwrite> = listOf()

    @field:DontUpdate
    var type: ChannelType = ChannelType.UNKNOWN

    data class RolePermissionOverwrite(
        val role: GuildRole,
        var value: Boolean
    )

    data class UserPermissionOverwrite(
        val role: User,
        var value: Boolean
    )


    override fun sendMessage(message: Message) {
        var type = 9
        when (message) {
            is CardMessage -> type = 10
            is MarkdownMessage -> type = 9
        }
        client.sendChannelMessage(
            type = type,
            content = message.content(),
            target = this,
            quote = message.quote
        )
    }
    fun sendMessage(message: String) {
        client.sendChannelMessage(
            content = message,
            target = this
        )
    }

    fun sendMarkdownMessage(message: String) {
        sendMessage(MarkdownMessage(client, message))
    }

    fun sendCardMessage(content: CardMessage.MessageScope.() -> Unit) {
        val msg = CardMessage(client = client,
            contentBuilder = content)
        sendMessage(msg)
    }

    override fun updateByJson(jsonElement: JsonElement) {
        super.updateByJson(jsonElement)
        type = when(jsonElement.asJsonObject.get("type").asInt) {
            1 -> ChannelType.TEXT
            2 -> ChannelType.VOICE
            else -> ChannelType.UNKNOWN
        }
    }

    override fun update() {
        with(client) {
            val channel = Gson().fromJson(sendRequest(requestBuilder(Client.RequestType.VIEW_CHANNEL,
                mapOf("channel_id" to id))), JsonObject::class.java)
            updateByJson(channel)
        }
    }

    init {
    }
}