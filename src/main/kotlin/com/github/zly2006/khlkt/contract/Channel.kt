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

package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.message.CardMessage
import com.github.zly2006.khlkt.message.MarkdownMessage
import com.github.zly2006.khlkt.message.Message
import com.github.zly2006.khlkt.utils.Updatable
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

enum class ChannelType {
    UNKNOWN,
    TEXT,
    VOICE,
}

class Channel(
    @field:Transient
    val client: Client,
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
    @field:Transient//TODO
    var permissionOverwrites: List<RolePermissionOverwrite> = listOf()
    @field:Transient//TODO
    var permissionUsers: List<UserPermissionOverwrite> = listOf()

    @field:Transient
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
        if (message is MarkdownMessage) {
            client.sendChannelMessage(
                type = 9,
                content = message.content(),
                target = this
            )
        }
        if (message is CardMessage) {
            client.sendChannelMessage(
                type = 10,
                content = message.content(),
                target = this
            )
        }
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
            primaryReceiver = this,
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