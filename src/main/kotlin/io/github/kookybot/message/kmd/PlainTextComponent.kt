package io.github.kookybot.message.kmd

class PlainTextComponent(text: String = "") : MessageComponent() {
    var text: String = text
        internal set

    override fun toMarkdown(): String {
        return text
    }
}