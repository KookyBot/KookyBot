package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.client.State
import com.github.zly2006.khlkt.utils.Cache

class Self(
    val client: Client
) {
    val guilds: Cache<List<Guild>> = Cache(
        uploader = {},
        updater = {
            var guilds: List<Guild> = listOf()
            with (client) {
                if (status != State.Connected) return@Cache null
                var jsonObject = sendRequest(requestBuilder(Client.RequestType.GUILD_LIST))
                jsonObject.asJsonObject.get("items").asJsonArray.forEach { item ->

                }
            }
            return@Cache guilds
        }
    )
    val chattingUsers: Cache<List<PrivateChatUser>> = Cache(
        uploader = {},
        updater = {
            // TODO
            return@Cache null;
        }
    )
    fun getUser(userId: String): User {
        return client.getUser(userId)
    }
}
