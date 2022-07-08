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

package io.github.kookybot.kookybot.message

import io.github.kookybot.kookybot.client.Client
import io.github.kookybot.kookybot.contract.MessageReceiver
import io.github.kookybot.kookybot.contract.TextChannel
import io.github.kookybot.kookybot.contract.User

/**
 * 自己发出的消息，可以编辑
 *
 * 目前仅限于频道（由于官方api）
 */
class SelfMessage(
    private val client: Client,
    val id: String,
    val timestamp: Int,
    val target: MessageReceiver,
    val content: String
) {
    fun edit(content: String) {
        when (target) {
            is TextChannel -> {
                client.sendRequest(client.requestBuilder(Client.RequestType.EDIT_CHANNEL_MESSAGE,
                "content" to content,
                "msg_id" to id))
            }
            is User -> {
                client.sendRequest(client.requestBuilder(Client.RequestType.EDIT_PRIVATE_MESSAGE,
                    "content" to content,
                    "msg_id" to id))
            }
        }
    }
    fun delete() {
        when (target) {
            is TextChannel -> {
                client.sendRequest(client.requestBuilder(Client.RequestType.EDIT_CHANNEL_MESSAGE,
                    "msg_id" to id))
            }
            is User -> {
                client.sendRequest(client.requestBuilder(Client.RequestType.EDIT_PRIVATE_MESSAGE,
                    "msg_id" to id))
            }
        }
    }

    /**
     * 这会更改message的值，建议构造一次性message
     */
    fun reply(message: Message) {
        val msg = message.copy()
        msg.quote = id
        target.sendMessage(msg)
    }

    fun reply(message: String) {
        val msg = MarkdownMessage(client, message)
        msg.quote = id
        target.sendMessage(msg)
    }
}