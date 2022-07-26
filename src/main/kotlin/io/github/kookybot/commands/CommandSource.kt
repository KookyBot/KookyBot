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

import io.github.kookybot.commands.CommandSource.Type.*
import io.github.kookybot.contract.PrivateChatUser
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.contract.User
import org.slf4j.LoggerFactory

class CommandSource(
    val type: Type = Console,
    val channel: TextChannel? = null,
    val private: PrivateChatUser? = null,
    val user: User? = null,
    val timestamp: Long,
) {
    /** [_All]等以下划线为前缀类型只用来选择快捷命令何时被执行，不回作为参数传递
     */
    enum class Type(val v: Int) {
        Console(1),
        Private(2),
        Channel(4),
        _Console_Private(3),
        _Console_Channel(5),
        _Private_Channel(6),
        _All(255)
    }

    fun sendMessage(message: String) {
        when (type) {
            Console -> LoggerFactory.getLogger("Command").info(message)
            Channel -> channel!!.sendMessage("(met)${user!!.id}(met)$message")
            Private -> private!!.sendMessage(message)
            else -> throw NotImplementedError("Invalid type.")
        }
    }

    fun hasPermission(permission: String): Boolean {
        return when (type) {
            Console -> true
            Channel -> channel!!.client.permissionManager.hasPermission(
                permission,
                user!!.id,
                channel.guild.id,
                channel.id
            )
            Private -> private!!.client.permissionManager.hasPermission(permission, user!!.id)
            else -> throw NotImplementedError("Invalid type.")
        }
    }

    fun setGlobalPermission(permission: String, value: Boolean?) {
        if (type == Console) {
            throw Exception("Cannot set console permissions.")
        }
        user!!.client.permissionManager.setPermission(
            perm = permission,
            user = user.id,
            value = value
        )
    }

    fun setChannelPermission(permission: String, value: Boolean?) {
        if (type != Channel) {
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
        if (type != Channel) {
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