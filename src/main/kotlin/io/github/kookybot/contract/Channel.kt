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

package io.github.kookybot.contract

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.kookybot.client.Client
import io.github.kookybot.utils.DontUpdate
import io.github.kookybot.utils.Updatable

enum class ChannelType {
    UNKNOWN,
    TEXT,
    VOICE,
}

abstract class Channel(
    @field:Transient
    val client: Client,
    val id: String,
    @field:Transient
    val guild: Guild,
) : PermissionOverwritten(), Updatable {
    var name: String = ""
        internal set

    @field:Transient
    var parent: Category? = null
        internal set
    var topic: String = ""
        internal set

    @field:SerializedName("permission_sync")
    var permissionSync: Int = 0
        internal set
    var level: Int = 0
        internal set

    @field:DontUpdate
    var type: ChannelType = ChannelType.UNKNOWN
        internal set

    override fun updateByJson(jsonElement: JsonElement) {
        super<Updatable>.updateByJson(jsonElement)
        type = when (jsonElement.asJsonObject.get("type").asInt) {
            1 -> ChannelType.TEXT
            2 -> ChannelType.VOICE
            else -> ChannelType.UNKNOWN
        }
        val perm = client.sendRequest(client.requestBuilder(Client.RequestType.CHANNEL_ROLE_INDEX, "channel_id" to id))
        updateByJson(perm, guild)
    }

    override fun update() {
        with(client) {
            val channel = Gson().fromJson(sendRequest(requestBuilder(Client.RequestType.VIEW_CHANNEL,
                mapOf("channel_id" to id))), JsonObject::class.java)
            updateByJson(channel)
        }
    }
}