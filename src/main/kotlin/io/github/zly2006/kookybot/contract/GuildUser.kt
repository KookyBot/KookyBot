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

package io.github.zly2006.kookybot.contract

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.utils.DontUpdate
import io.github.zly2006.kookybot.utils.Updatable

class GuildUser(
    client: Client,
    id: String,
    @field:DontUpdate
    val guild: Guild,
    online: Boolean = false,
    name: String = "",
    val nickname: String = "",
    identifyNumber: String = "",
    status: UserState = UserState.NORMAL,
    bot: Boolean = false,
    mobilePhoneVerified: Boolean = false,
    avatarUrl: String = "",
    vipAvatarUrl: String = "",
    isVip: Boolean = false,
    @field:DontUpdate
    val roles: List<GuildRole> = listOf(),
    @field:SerializedName("joined_at")
    val joinTime: Int = 0,
    @field:SerializedName("active_time")
    val activeTime: Int = 0,
) : User(client,
    id,
    online,
    name,
    identifyNumber,
    status,
    bot,
    mobilePhoneVerified,
    avatarUrl,
    vipAvatarUrl,
    isVip), Updatable {
    override fun updateByJson(jsonElement: JsonElement) {
        super.updateByJson(jsonElement)
        status = when (jsonElement.asJsonObject.get("status").asInt) {
            10 -> UserState.BANNED
            else -> UserState.NORMAL
        }

    }
    override fun update() {
        updateByJson(with(client) {
            sendRequest(requestBuilder(Client.RequestType.USER_VIEW, mapOf("user_id" to id, "guild_id" to guild.id))).asJsonObject
        })
    }

}