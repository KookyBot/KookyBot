package io.github.kookybot.commands

import io.github.kookybot.contract.PrivateChatUser
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.contract.User
import org.slf4j.LoggerFactory

class CommandSource(
    val type: Type = Type.Console,
    val channel: TextChannel? = null,
    val private: PrivateChatUser? = null,
    val user: User? = null
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