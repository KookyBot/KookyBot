package io.github.kookybot.message.kmd

class CodeComponent(
    val language: String,
    val code: String,
) : MessageComponent() {
    override fun toMarkdown(): String =
        if (!code.contains('\n') && language == "") "`$code`"
        else "\n```$language\n$code\n```\n"

}