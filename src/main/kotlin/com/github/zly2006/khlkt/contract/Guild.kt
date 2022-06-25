package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.client.State
import com.github.zly2006.khlkt.utils.Cache
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Guild(
    @Transient
    val client: Client,
    @SerializedName("id")
    val id: String,
    @SerializedName("master_id")
    val masterId: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("topic")
    var topic: String,
    @SerializedName("icon")
    var iconUrl: String,
    @SerializedName("notify_type")
    var notifyType: NotifyType,
    @SerializedName("region")
    var region: String,
    @Transient
    var defaultChannel: Cache<Channel>?,
    @Transient
    var welcomeChannel: Cache<Channel>?,
    @SerializedName("open_id")
    /**
     * 公开邀请链接id，为null说明不公开
     */
    var openId: Int?,
    @SerializedName("level")
    var level: Int,
    @SerializedName("boost_num")
    var boostCount: Int,
    @Transient
    var channels: List<Cache<Channel>>,
    @Transient
    var user: Cache<List<Cache<GuildUser>>>,
    @Transient
    var roleMap: Cache<Map<Int, GuildRole>>,
) {
    val owner: User get() = client.getUser(masterId)
    val botPermission: Int get() {
        return 0;
        //TODO
    }
    fun update() {
        var guild: Guild
        with(client) {
            if (status != State.Connected) return
            val g = client.sendRequest(requestBuilder(Client.RequestType.GUILD_VIEW,
                mapOf("guild_id" to id)))

            guild = Gson().fromJson(g, Guild::class.java)
            if (!g.get("enable_open").asBoolean)
                openId = null

            channels = mutableListOf()
            val list = g.get("channels").asJsonArray.map { it.asJsonObject.get("id").asString }.forEach {

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
    }
}
