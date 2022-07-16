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

import io.github.kookybot.client.Client
import org.slf4j.LoggerFactory

class Self(
    val client: Client,
) {
    val logger = LoggerFactory.getLogger(this::class.java)
    val id: String
    val guilds: List<Guild> =
        with(client) {
            sendRequest(requestBuilder(Client.RequestType.GUILD_LIST))
                .asJsonObject.get("items").asJsonArray.map { item ->
                    try {
                        val guild = Guild(client, item.asJsonObject.get("id").asString)
                        guild.update()
                        guild
                    } catch (e: Exception) {
                        logger.error("初始化服务器缓存对象时发生异常，请检查错误或提交issue", e)
                        null
                    }
                }.filterNotNull().toMutableList()
        }
    val chattingUsers: List<PrivateChatUser> =
        with(client) {
            sendRequest(requestBuilder(Client.RequestType.USER_CHAT_LIST))
                .asJsonObject.get("items").asJsonArray.map { item ->
                    try {
                        val user = PrivateChatUser(code = item.asJsonObject.get("code").asString, client = client)
                        user.update()
                        return@map user
                    } catch (e: Exception) {
                        logger.error("初始化私聊会话缓存对象时发生异常，请检查错误或提交issue", e)
                        return@map null
                    }
                }.filterNotNull().toMutableList()
        }
    fun getUser(userId: String): User {
        return client.getUser(userId)
    }

    fun getChannel(id: String): Channel? {
        return guilds.map { guild -> guild.channels.firstOrNull { channel -> channel.id == id } }.firstOrNull { it != null }
    }

    fun getGuildUser(id: String, guild: String): GuildUser? {
        return guilds.firstOrNull { it.id == guild }?.getGuildUser(id)
    }

    internal fun updatePrivateChatUser(userId: String): PrivateChatUser {
        val jsonObject = with(client){sendRequest(requestBuilder(Client.RequestType.USER_CHAT_LIST))}
        val code = jsonObject.asJsonObject.get("items").asJsonArray.find {
            it.asJsonObject.get("target_info").asJsonObject.get("id").asString == userId
        }!!.asJsonObject.get("code").asString
        val user = PrivateChatUser(code = code, client = client, id = userId)
        user.update()
        (chattingUsers as MutableList).add(user)
        return user
    }

    init {
        val json = client.sendRequest(client.requestBuilder(Client.RequestType.USER_ME))
            .asJsonObject
        id = json.get("id").asString
        logger.info("${json.get("username").asString}#${json.get("identify_num").asString} $id")
    }
}
