package io.github.zly2006.kookybot.commands

import io.github.zly2006.kookybot.contract.PrivateChatUser
import io.github.zly2006.kookybot.contract.TextChannel
import io.github.zly2006.kookybot.contract.User
import org.slf4j.LoggerFactory

class CommandSource(

) {
    enum class Type {
        Console,
        Private,
        Channel
    }
    val type: Type = Type.Console
    val channel: TextChannel? = null
    val private: PrivateChatUser? = null
    val user: User? = null
    fun sendMessage(message: String) {
        when (type) {
            Type.Console -> LoggerFactory.getLogger("Command ").info(message)
            Type.Channel -> channel!!.sendMessage("(met)${user!!.id}(met)$message")
            Type.Private -> private!!.sendMessage(message)
        }
    }
}