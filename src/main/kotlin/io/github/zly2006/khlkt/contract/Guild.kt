/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
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

package io.github.zly2006.khlkt.contract

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.zly2006.khlkt.client.Client
import io.github.zly2006.khlkt.client.State
import io.github.zly2006.khlkt.utils.DontUpdate
import io.github.zly2006.khlkt.utils.Permission
import io.github.zly2006.khlkt.utils.PermissionImpl
import io.github.zly2006.khlkt.utils.Updatable

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
    var defaultChannel: Channel? = null
    @field:Transient
    var welcomeChannel: Channel? = null
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
    @field:DontUpdate
    var users: List<GuildUser> = listOf()
    @field:DontUpdate
    var roleMap: Map<Int, GuildRole> = mapOf()

    val owner: User get() = client.getUser(masterId)
    val botPermission: Permission = PermissionImpl.Permissions.None

    override fun update() {
        with(client) {
            if (status != State.Connected) return
            val g = client.sendRequest(requestBuilder(Client.RequestType.GUILD_VIEW,
                mapOf("guild_id" to id)))

            updateByJson(g)
            if (!g.get("enable_open").asBoolean)
                openId = null

            channels = mutableListOf()
            g.get("channels").asJsonArray.map { it.asJsonObject.get("id").asString }.forEach {
                val channel = Channel(client, it, this@Guild)
                channel.update()
                channels = channels + channel
            }
            // channels
            /*
                {
                    var channel = Gson().fromJson(sendRequest(requestBuilder(Client.RequestType.VIEW_CHANNEL,
                        mapOf("channel_id" to channelId))), Channel::class.java)
                    channel.guild = guild
                    channel.client = client
                    return@Cache channel
                }
            }
*/
            defaultChannel =
                channels.filter { it.id == g.asJsonObject.get("default_channel_id").asString }
                    .firstOrNull()
            welcomeChannel =
                channels.filter { it.id == g.asJsonObject.get("welcome_channel_id").asString }
                    .firstOrNull()


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
                    // TODO:
                    // 等待官方
                }
            }
            // users
            users = mutableListOf()
            
        }
    }
    init {
        update()
    }
}
