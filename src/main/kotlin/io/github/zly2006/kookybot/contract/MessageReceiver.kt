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

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.message.CardMessage
import io.github.zly2006.kookybot.message.MarkdownMessage
import io.github.zly2006.kookybot.message.Message

interface MessageReceiver {
    fun sendMessage(message: Message) {
        when (this) {
            is TextChannel -> {
                when (message) {
                    is CardMessage -> {
                        client.sendChannelMessage(
                            type = 10,
                            target = this,
                            content = message.content()
                        )
                    }
                    is MarkdownMessage -> {
                        client.sendChannelMessage(
                            target = this,
                            content = message.content()
                        )
                    }
                }
            }
        }
    }
    abstract val id: String
    abstract val client: Client
}