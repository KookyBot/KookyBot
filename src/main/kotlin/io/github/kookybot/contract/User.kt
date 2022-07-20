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

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.github.kookybot.client.Client
import io.github.kookybot.message.Message
import io.github.kookybot.message.SelfMessage
import io.github.kookybot.utils.DontUpdate
import io.github.kookybot.utils.Updatable

/**
 * 警告：
 * 此类型不会自动更新。
 * 每次使用都应该重新获取一遍，如
 *
 * @see com.github.kookybot.kookybot.contract.Self#getUser(String kotlin.userId)
 */
enum class UserState {
    NORMAL,
    BANNED,
}

/**
 * 这个类无法自动更新，因为根据官方文档，我们无法保证他是最新的。
 *
 * 请使用：[PrivateChatUser]和[GuildUser]，他们可以保证最新
 */
open class User(
    client: Client,
    override val id: String,
    online: Boolean = false,
    name: String = "",
    idNum: String = "",
    status: UserState = UserState.NORMAL,
    bot: Boolean = false,
    mobilePhoneVerified: Boolean = false,
    avatarUrl: String = "",
    vipAvatarUrl: String = "",
    isVip: Boolean = false,
) : MessageReceiver, Updatable {
    @Transient
    override var client: Client = client
        internal set
    var online: Boolean = online
        internal set

    @SerializedName("username")
    var name: String = name
        internal set
    @field:SerializedName("identify_num")
    var identifyNumber: String = idNum
        internal set
    @field:Transient
    @DontUpdate
    var status: UserState = status
        internal set
    var bot: Boolean = bot
        internal set
    @field:SerializedName("mobile_verified")
    var mobilePhoneVerified: Boolean = mobilePhoneVerified
        internal set
    @field:SerializedName("avatar")
    var avatarUrl: String = avatarUrl
        internal set
    @field:SerializedName("vip_avatar")
    var vipAvatarUrl: String = vipAvatarUrl
        internal set
    @field:SerializedName("is_vip")
    var isVip: Boolean = isVip
        internal set

    override fun sendMessage(message: Message): SelfMessage {
        return client.sendUserMessage(
            target = this,
            content = message.content()
        )
    }

    fun talkTo(): PrivateChatUser {
        return ((client.self!!.chattingUsers[id]) ?: let {
            client.sendRequest(client.requestBuilder(Client.RequestType.CREATE_CHAT, "target_id" to id))
            return client.self!!.updatePrivateChatUser(id)
        }).value
    }

    fun atGuild(guild: Guild): GuildUser? {
        return client.self!!.getGuildUser(id, guild.id)
    }

    val fullName get() = "$name#$identifyNumber"
    override fun update() {
        throw Exception("No impl.")
    }

    override fun updateByJson(jsonElement: JsonElement) {
        super.updateByJson(jsonElement)
        if (jsonElement.asJsonObject["status"] != null) {
            status = when (jsonElement.asJsonObject["status"].asInt) {
                10 -> UserState.BANNED
                else -> UserState.NORMAL
            }
        }
    }
}
