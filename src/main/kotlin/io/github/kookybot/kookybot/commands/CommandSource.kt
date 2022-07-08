package io.github.kookybot.kookybot.commands

import io.github.kookybot.kookybot.contract.PrivateChatUser
import io.github.kookybot.kookybot.contract.TextChannel
import io.github.kookybot.kookybot.contract.User
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
            Type.Console -> LoggerFactory.getLogger("Command ").info(message)
            Type.Channel -> channel!!.sendMessage("(met)${user!!.id}(met)$message")
            Type.Private -> private!!.sendMessage(message)
        }
    }
}