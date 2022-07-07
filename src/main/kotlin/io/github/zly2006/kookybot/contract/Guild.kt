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

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.client.State
import io.github.zly2006.kookybot.utils.DontUpdate
import io.github.zly2006.kookybot.utils.Permission
import io.github.zly2006.kookybot.utils.PermissionImpl
import io.github.zly2006.kookybot.utils.Updatable

class Guild(
    @field:DontUpdate
    @field:Transient
    val client: Client,
    @field:SerializedName("id")
    val id: String,
): Updatable {
    @field:SerializedName("master_id")
    var masterId: String = ""
    @field:SerializedName("name")
    var name: String = ""
    @field:SerializedName("topic")
    var topic: String = ""
    @field:SerializedName("icon")
    var iconUrl: String = ""
    @field:SerializedName("notify_type")
    var notifyType: NotifyType = NotifyType.NONE
    @field:SerializedName("region")
    var region: String = ""
    @field:Transient
    var defaultChannel: TextChannel? = null
    @field:Transient
    var welcomeChannel: TextChannel? = null
    @field:SerializedName("open_id")
    /**
     * 公开邀请链接id，为null说明不公开
     */
    var openId: Int? = null
    @field:SerializedName("level")
    var level: Int = 0
    @field:SerializedName("boost_num")
    var boostCount: Int = 0
    @field:Transient
    var channels: List<Channel> = listOf()
    @field:Transient
    var categories: List<Category> = listOf()
    @field:DontUpdate
    var users: Lazy<List<GuildUser>> = lazy {
        client.sendRequest(client.requestBuilder(Client.RequestType.GUILD_USER_LIST, "guild_id" to id))
            .get("items").asJsonArray.map { it.asJsonObject.get("id").asString }.map {
                val user = GuildUser(client, it, this@Guild)
                user.update()
                return@map user
            }.toList()
    }
    @field:DontUpdate
    var roleMap: Map<Int, GuildRole> = mapOf()

    val owner: User get() = client.getUser(masterId)
    val botPermission: Permission = PermissionImpl.Permissions.None

    override fun updateByJson(jsonElement: JsonElement) {
        super.updateByJson(jsonElement)
        if (!jsonElement.asJsonObject.get("enable_open").asBoolean)
            openId = null
        channels = channels + jsonElement.asJsonObject.get("channels")
            .asJsonArray
            .map { it.asJsonObject }
            .filter { !it.get("is_category").asBoolean }
            .filter { channels.map { it.id }.contains(it.get("id").asString) }
            .map {
            val id = it.get("id").asString
            val channel = when (it.get("type").asInt) {
                1 -> TextChannel(client, id, this@Guild)
                2 -> VoiceChannel(client, id, this@Guild)
                else -> throw Exception("Invalid channel type.")
            }
            channel.update()
            return@map channel
        }
        defaultChannel = channels.firstOrNull { it.id == jsonElement.asJsonObject.get("default_channel_id").asString && it.type == ChannelType.TEXT } as TextChannel?
        welcomeChannel = channels.firstOrNull { it.id == jsonElement.asJsonObject.get("welcome_channel_id").asString && it.type == ChannelType.TEXT } as TextChannel?
    }

    override fun update() {
        with(client) {
            if (status != State.Connected) return
            val g = client.sendRequest(requestBuilder(Client.RequestType.GUILD_VIEW,
                mapOf("guild_id" to id)))

            updateByJson(g)

            // bot permission


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
        (channels as MutableList).add(channel)
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
        (channels as MutableList).add(channel)
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
            guild = this,
            name = name
        )
        category.update()
        (categories as MutableList).add(category)
        return category
    }
    fun getGuildUser(userId: String): GuildUser? {
        return client.self!!.getGuildUser(userId, id)
    }
}
