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
import com.google.gson.annotations.SerializedName
import io.github.kookybot.client.Client
import io.github.kookybot.utils.DontUpdate
import io.github.kookybot.utils.Permission
import io.github.kookybot.utils.PermissionImpl
import io.github.kookybot.utils.Updatable

class Guild(
    @field:DontUpdate
    @field:Transient
    val client: Client,
    @field:SerializedName("id")
    val id: String,
): Updatable {
    @field:SerializedName("master_id")
    var masterId: String = ""
        internal set
    @field:SerializedName("name")
    var name: String = ""
        internal set
    @field:SerializedName("topic")
    var topic: String = ""
        internal set
    @field:SerializedName("icon")
    var iconUrl: String = ""
        internal set
    @field:SerializedName("notify_type")
    var notifyType: NotifyType = NotifyType.NONE
        internal set
    @field:SerializedName("region")
    var region: String = ""
        internal set
    @field:Transient
    var defaultChannel: TextChannel? = null
        internal set
    @field:Transient
    var welcomeChannel: TextChannel? = null
        internal set
    @field:SerializedName("open_id")
            /**
             * 公开邀请链接id，为null说明不公开
             */
    var openId: Int? = null
        internal set
    @field:SerializedName("level")
    var level: Int = 0
        internal set
    @field:SerializedName("boost_num")
    var boostCount: Int = 0
        internal set
    @field:Transient
    var categories: Map<String, Category> = mutableMapOf()
        internal set
    var lazyUsers: MutableMap<String, Lazy<GuildUser>> =
        client.sendRequest(client.requestBuilder(Client.RequestType.GUILD_USER_LIST, "guild_id" to id))
            .get("items").asJsonArray.map { it.asJsonObject.get("id").asString }.map {
                it to lazy {
                    val user = GuildUser(client, it, this@Guild)
                    user.update()
                    return@lazy user
                }
            }.toMap().toMutableMap()
        internal set
    var lazyChannels: Map<String, Lazy<Channel>> = mutableMapOf()
        internal set
    @field:DontUpdate
    var roleMap: Map<Int, GuildRole> = mapOf()
        internal set
    val owner: User get() = client.getUser(masterId)
    val botPermission: Permission = PermissionImpl.Permissions.None

    override fun updateByJson(jsonElement: JsonElement) {
        super.updateByJson(jsonElement)
        if (!jsonElement.asJsonObject.get("enable_open").asBoolean)
            openId = null

        jsonElement.asJsonObject.get("channels").asJsonArray.map { it.asJsonObject }
            .filter {
                val id = it.get("id").asString
                (!lazyChannels.containsKey(id)) && (!categories.containsKey(id))
            }
            .forEach {
                val id = it.get("id").asString
                if (it["is_category"].asBoolean) {
                    (categories as MutableMap)[id] = Category(client, id, this@Guild).updateAndGet()
                } else {
                    try {
                        (lazyChannels as MutableMap)[id] = lazy {
                            val channel = when (it.get("type").asInt) {
                                1 -> TextChannel(client, id, this@Guild)
                                2 -> VoiceChannel(client, id, this@Guild)
                                else -> throw Exception("Invalid channel type.")
                            }
                            channel.update()
                            channel
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        defaultChannel =
            lazyChannels[jsonElement.asJsonObject.get("default_channel_id").asString]?.value as TextChannel?
        welcomeChannel =
            lazyChannels[jsonElement.asJsonObject.get("welcome_channel_id").asString]?.value as TextChannel?
    }

    override fun update() {
        with(client) {
            val g = client.sendRequest(requestBuilder(Client.RequestType.GUILD_VIEW,
                mapOf("guild_id" to id)))

            updateByJson(g)

            if (true) {
                roleMap = mutableMapOf()
                g.get("roles").asJsonArray.forEach {
                    val role = Gson().fromJson(it, GuildRole::class.java)
                    roleMap = roleMap + (role.id to role)
                }
            } else {
                // always false
                try {
                    with(client) {
                        val list = sendRequest(requestBuilder(Client.RequestType.LIST_GUILD_ROLE,
                            mapOf("guild_id" to id))).get("items").asJsonArray
                        roleMap = mutableMapOf()
                        list.forEach {
                            val role = Gson().fromJson(it, GuildRole::class.java)
                            roleMap = roleMap + (role.id to role)
                        }
                    }
                } catch (e: Exception) {
                    // TODO: 判定权限，等以后吧
                }
            }
        }
    }
    fun createTextChannel(name: String, category: Category? = null): TextChannel{
        val json = with(client) {
            sendRequest(requestBuilder(Client.RequestType.CREATE_CHANNEL,
                "guild_id" to id,
                "name" to name,
                "parent_id" to category?.id,
            ))
        }

        val channel = TextChannel(
            client = client,
            id = json.get("id").asString,
            guild = this
        )
        channel.update()
        (lazyChannels as MutableMap) += (json.get("id").asString to lazyOf(channel))
        category?.children?.add(channel)
        return channel
    }
    fun createVoiceChannel(name: String, category: Category? = null): VoiceChannel{
        val json = with(client) {
            sendRequest(requestBuilder(Client.RequestType.CREATE_CHANNEL,
                "guild_id" to id,
                "name" to name,
                "parent_id" to category?.id,
                "type" to 2
            ))
        }
        val channel = VoiceChannel(
            client = client,
            id = json.get("id").asString,
            guild = this
        )
        channel.update()
        (lazyChannels as MutableMap) += (json.get("id").asString to lazyOf(channel))
        category?.children?.add(channel)
        return channel
    }
    fun createCategory(name: String): Category{
        val json = with(client) {
            sendRequest(requestBuilder(Client.RequestType.CREATE_CHANNEL,
                "guild_id" to id,
                "name" to name,
                "is_category" to 1
            ))
        }
        val category = Category(
            client = client,
            id = json.get("id").asString,
            guild = this
        )
        category.update()
        (categories as MutableMap) += (json.get("id").asString to category)
        return category
    }

    fun updateUser(userId: String) {
        client.run {
            sendRequest(requestBuilder(Client.RequestType.USER_VIEW, "guild_id" to id, "user_id" to userId))
        }
    }

    fun getGuildUser(userId: String) = lazyUsers[userId]?.value
    fun getChannel(channelId: String) = lazyChannels[channelId]?.value
}
