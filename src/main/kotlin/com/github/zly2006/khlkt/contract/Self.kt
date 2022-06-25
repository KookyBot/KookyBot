package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.client.State
import com.github.zly2006.khlkt.utils.Cache
import com.google.gson.Gson
import com.google.gson.JsonObject

class Self(
    val client: Client,
    val id: String = client.sendRequest(client.requestBuilder(Client.RequestType.USER_ME))
        .asJsonObject.get("id").asString
) {
    private fun cacheChannel(guild: Cache<Guild>, channelId: String): Cache<Channel> {
        with(client) {
            val ret: Cache<Channel> = Cache(
                updater = {
                    var channel = Gson().fromJson(sendRequest(requestBuilder(Client.RequestType.VIEW_CHANNEL,
                        mapOf("channel_id" to channelId))), Channel::class.java)
                    channel.guild = guild
                    channel.client = client
                    return@Cache channel
                }
            )
            return ret;
        }
    }
    private fun cacheGuild(obj: JsonObject): Cache<Guild> {
        val ret: Cache<Guild> = Cache(
            updater = {
                var guild: Guild
                with(client) {
                    if (status != State.Connected) return@Cache null
                    var g = client.sendRequest(requestBuilder(Client.RequestType.GUILD_VIEW,
                        mapOf("guild_id" to obj.get("id").asString)))
                    guild = Gson().fromJson(g, Guild::class.java)
                    if (!g.get("enable_open").asBoolean)
                        guild.openId = null

                    guild.channels = mutableListOf()
                    val list = g.get("channels").asJsonArray.map { it.asJsonObject.get("id").asString }.forEach {
                        guild.channels += cacheChannel(this@Cache, it)
                    }

                    guild.defaultChannel = guild.channels.filter { it.cachedValue?.id == g.asJsonObject.get("default_channel_id").asString }.firstOrNull()
                    guild.welcomeChannel = guild.channels.filter { it.cachedValue?.id == g.asJsonObject.get("welcome_channel_id").asString }.firstOrNull()


                    // bot permission


                    if (true) {
                        val roleMap = mutableMapOf<Int, GuildRole>()
                        val list = g.get("roles").asJsonArray.forEach {
                            val role = Gson().fromJson(it, GuildRole::class.java)
                            roleMap += role.id to role
                        }
                        guild.roleMap = Cache()
                        guild.roleMap.cachedValue = roleMap
                    } else {
                        guild.roleMap = Cache(
                            uploader = {
                                // TODO
                            },
                            updater = {
                                try {
                                    with(client) {
                                        val list = sendRequest(requestBuilder(Client.RequestType.LIST_GUILD_ROLE,
                                            mapOf("guild_id" to guild.id))).get("items").asJsonArray
                                        val roleMap = mutableMapOf<Int, GuildRole>()
                                        list.forEach {
                                            val role = Gson().fromJson(it, GuildRole::class.java)
                                            roleMap += role.id to role
                                        }
                                        return@Cache roleMap
                                    }
                                } catch (e: Exception) {
                                    // TODO:
                                    // 等待官方，此处是避免官方的bug
                                    return@Cache null
                                }
                            }
                        )
                    }

                }
                return@Cache guild
            },
            uploader = {

            }
        )
        return ret
    }
    private fun parseGuild(obj: JsonObject) {

    }
    val guilds: Cache<List<Cache<Guild>>> = Cache(
        uploader = {},
        updater = {
            var guilds: List<Cache<Guild>> = listOf()
            with (client) {
                if (status != State.Connected) return@Cache null
                val jsonObject = sendRequest(requestBuilder(Client.RequestType.GUILD_LIST))
                jsonObject.asJsonObject.get("items").asJsonArray.forEach { item ->
                    val guild = cacheGuild(item.asJsonObject)
                    guilds = guilds.plus(guild)
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
