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
    val name: String
    val identifyNumber: String
    val fullName: String get() = "$name#$id"

    init {
        val json = client.sendRequest(client.requestBuilder(Client.RequestType.USER_ME))
            .asJsonObject
        id = json.get("id").asString
        client.selfId = id
        name = json.get("username").asString
        identifyNumber = json["identify_num"].asString
        logger.info("Bot logged in as $fullName, uid $id.")
    }

    val guilds: Map<String, Guild> =
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
                    }?.let {
                        it.id to it
                    }
                }.filterNotNull().toMap().toMutableMap()
        }
    val chattingUsers: Map<String, Lazy<PrivateChatUser>> =
        with(client) {
            sendRequest(requestBuilder(Client.RequestType.USER_CHAT_LIST))
                .asJsonObject.get("items").asJsonArray.map { item ->
                    val id = item.asJsonObject["target_info"].asJsonObject["id"].asString
                    id to lazy {
                        try {
                            val user = PrivateChatUser(
                                code = item.asJsonObject.get("code").asString,
                                client = client,
                                id = id
                            )
                            user.update()
                            user
                        } catch (e: Exception) {
                            logger.error("初始化私聊会话缓存对象时发生异常，请检查错误或提交issue", e)
                            throw e
                        }
                    }
                }.toMap().toMutableMap()
        }
    fun getUser(userId: String): User {
        return client.getUser(userId)
    }

    fun getChannel(id: String): Channel? {
        return guilds.values.map { guild -> guild.lazyChannels.entries.firstOrNull { it.key == id }?.value?.value }
            .filterNotNull().firstOrNull()
    }

    fun getGuildUser(id: String, guild: String): GuildUser? {
        return guilds[guild]?.getGuildUser(id)
    }

    internal fun updatePrivateChatUser(userId: String): PrivateChatUser {
        val jsonObject = with(client) { sendRequest(requestBuilder(Client.RequestType.USER_CHAT_LIST)) }
        val code = jsonObject.asJsonObject.get("items").asJsonArray.find {
            it.asJsonObject.get("target_info").asJsonObject.get("id").asString == userId
        }!!.asJsonObject.get("code").asString
        val user = PrivateChatUser(code = code, client = client, id = userId)
        user.update()
        (chattingUsers as MutableMap) += (userId to lazyOf(user))
        return user
    }

    enum class MusicProvider {
        CloudMusic,
        QqMusic,
        Kugou
    }

    fun setListening(software: MusicProvider = MusicProvider.CloudMusic, singer: String, name: String) {
        client.run {
            sendRequest(requestBuilder(
                Client.RequestType.ACTIVITY,
                "data_type" to 2,
                "software" to software.name.lowercase(),
                "singer" to singer,
                "music_name" to name,
            ))
        }
    }
}
