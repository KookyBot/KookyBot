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

import com.google.gson.annotations.SerializedName
import io.github.kookybot.client.Client
import io.github.kookybot.message.CardMessage
import io.github.kookybot.message.MarkdownMessage
import io.github.kookybot.utils.DontUpdate
import io.github.kookybot.utils.Updatable

class PrivateChatUser(
    @field:DontUpdate
    val code: String,
    id: String,
    client: Client,
    name: String = "",
    identifyNumber: String = "",
    status: UserState = UserState.NORMAL,
    bot: Boolean = false,
    mobilePhoneVerified: Boolean = false,
    avatarUrl: String = "",
    vipAvatarUrl: String = "",
    isVip: Boolean = false,
    online: Boolean = false,
): User(client,
    id,
    online,
    name,
    identifyNumber,
    status,
    bot,
    mobilePhoneVerified,
    avatarUrl,
    vipAvatarUrl,
    isVip), Updatable {

    @field:SerializedName("last_read_time")
    var lastReadTime: Int = 0
        internal set

    @field:SerializedName("latest_msg_time")
    var latestMessageTime: Int = 0
        internal set

    @field:SerializedName("unread_count")
    var unreadCount: Int = 0
        internal set

    override fun update() {
        val json = with(client) {
            sendRequest(requestBuilder(Client.RequestType.USER_CHAT_VIEW, "chat_code" to code))
        }
        updateByJson(json)
        super<User>.updateByJson(json["target_info"].asJsonObject)
    }

    fun sendMessage(message: String, quote: String? = null) {
        val msg = MarkdownMessage(client, message)
        msg.quote = quote
        sendMessage(msg)
    }

    fun sendCardMessage(content: CardMessage.MessageScope.() -> Unit) {
        sendMessage(CardMessage(client, content))
    }
}
