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
import com.github.zly2006.khlkt.client.State

class Self(
    val client: Client,
    val id: String = client.sendRequest(client.requestBuilder(Client.RequestType.USER_ME))
        .asJsonObject.get("id").asString
) {
    val guilds: List<Guild> = let {
        val list: MutableList<Guild> = mutableListOf()
        with (client) {
            if (status != State.Connected) return@let list
            val jsonObject = sendRequest(requestBuilder(Client.RequestType.GUILD_LIST))
            jsonObject.asJsonObject.get("items").asJsonArray.forEach { item ->
                val guild = Guild(client, item.asJsonObject.get("id").asString)
                guild.update()
                list.add(guild)
            }
        }
        return@let list
    }
    val chattingUsers: List<PrivateChatUser> = listOf()
    fun getUser(userId: String): User {
        return client.getUser(userId)
    }
}
