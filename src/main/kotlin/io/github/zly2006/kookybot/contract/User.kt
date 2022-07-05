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

import com.google.gson.annotations.SerializedName
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.message.Message

/**
 * 警告：
 * 此类型不会自动更新。
 * 每次使用都应该重新获取一遍，如
 *
 * @see com.github.zly2006.kookybot.contract.Self#getUser(String kotlin.userId)
 */
enum class UserState {
    NORMAL,
    BANNED,
}

open class User(
    @Transient
    override var client: Client,
    override val id: String,
    val online: Boolean,
    val name: String,
    @field:SerializedName("identify_num")
    val identifyNumber: String,
    @field:Transient
    var status: UserState,
    val bot: Boolean,
    @field:SerializedName("mobile_verified")
    val mobilePhoneVerified: Boolean,
    @field:SerializedName("avatar")
    val avatarUrl: String,
    @field:SerializedName("vip_avatar")
    val vipAvatarUrl: String,
    /**
     * 不要使用，正在试图修复兼容性问题
     */
    @field:SerializedName("is_vip")
    val isVip: Boolean,
): MessageReceiver {
    override fun sendMessage(message: Message) {
        client.sendUserMessage(
            target = talkTo(),
            content = message.content()
        )
    }
    fun talkTo(): PrivateChatUser {
        return (client.self!!.chattingUsers.find { it.id == id }) ?: let {
            client.sendRequest(client.requestBuilder(Client.RequestType.CREATE_CHAT, "target_id" to id))
            return client.self!!.updatePrivateChatUser(id)
        }
    }
    fun atGuild(guild: Guild): GuildUser? {
        return client.self!!.getGuildUser(id, guild.id)
    }
}
