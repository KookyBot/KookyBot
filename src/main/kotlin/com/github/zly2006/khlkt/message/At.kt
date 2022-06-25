package com.github.zly2006.khlkt.message

import com.github.zly2006.khlkt.contract.GuildUser

object AtAll : MessageComponent() {
    override fun toMarkdown(): String {
        return "(met)all(met)"
    }
}
object AtHere : MessageComponent() {
    override fun toMarkdown(): String {
        return "(met)here(met)"
    }
}
fun At(user: GuildUser): MessageComponent {
    return object : MessageComponent() {
        override fun toMarkdown(): String {
            return "(met)${user.id}(met)"
        }
    }
}