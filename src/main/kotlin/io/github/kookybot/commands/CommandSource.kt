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

package io.github.kookybot.commands

import io.github.kookybot.contract.PrivateChatUser
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.contract.User
import org.slf4j.LoggerFactory

class CommandSource(
    val type: Type = Type.Console,
    val channel: TextChannel? = null,
    val private: PrivateChatUser? = null,
    val user: User? = null,
    val timestamp: Long,
) {
    enum class Type {
        Console,
        Private,
        Channel
    }

    fun sendMessage(message: String) {
        when (type) {
            Type.Console -> LoggerFactory.getLogger("Command").info(message)
            Type.Channel -> channel!!.sendMessage("(met)${user!!.id}(met)$message")
            Type.Private -> private!!.sendMessage(message)
        }
    }

    fun hasPermission(permission: String): Boolean {
        return when (type) {
            Type.Console -> true
            Type.Channel -> channel!!.client.permissionManager.hasPermission(
                permission,
                user!!.id,
                channel.guild.id,
                channel.id
            )
            Type.Private -> private!!.client.permissionManager.hasPermission(permission, user!!.id)
        }
    }

    fun setGlobalPermission(permission: String, value: Boolean?) {
        if (type == Type.Console) {
            throw Exception("Cannot set console permissions.")
        }
        user!!.client.permissionManager.setPermission(
            perm = permission,
            user = user.id,
            value = value
        )
    }

    fun setChannelPermission(permission: String, value: Boolean?) {
        if (type != Type.Channel) {
            throw Exception("Cannot set user permissions.")
        }
        user!!.client.permissionManager.setPermission(
            perm = permission,
            user = user.id,
            value = value,
            channelId = channel!!.id
        )
    }

    fun setGuildPermission(permission: String, value: Boolean?) {
        if (type != Type.Channel) {
            throw Exception("Cannot set user permissions.")
        }
        user!!.client.permissionManager.setPermission(
            perm = permission,
            user = user.id,
            value = value,
            guildId = channel!!.guild.id
        )
    }
}