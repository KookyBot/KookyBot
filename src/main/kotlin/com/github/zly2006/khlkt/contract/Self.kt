package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.client.State
import com.google.gson.JsonObject

class Self(
    val client: Client,
    val id: String = client.sendRequest(client.requestBuilder(Client.RequestType.USER_ME))
        .asJsonObject.get("id").asString
) {
    private fun parseGuild(obj: JsonObject) {

    }
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
