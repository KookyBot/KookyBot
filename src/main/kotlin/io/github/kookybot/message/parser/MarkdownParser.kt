package io.github.kookybot.message.parser

import io.github.kookybot.events.MessageEvent
import io.github.kookybot.message.kmd.*
import org.slf4j.LoggerFactory

class MarkdownParser(val content: String, private val event: MessageEvent) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    var cursor = 0
    private var component: MessageComponent = MessageComponent.Empty
    fun parse(): MessageComponent {
        val component = parseNext()
        while (cursor != content.length) {
            parseNext()
        }
        return component
    }

    private fun parseNext(): MessageComponent {
        fun tryParse(regex: Regex, func: (MatchResult) -> MessageComponent?) {
            regex.matchAt(content, cursor)?.let {
                try {
                    func(it)
                } catch (e: Exception) {
                    logger.debug("parsing error", e)
                    null
                }
                    ?.let {
                        if (component == MessageComponent.Empty) component = it
                        else component.append(it)
                    }
            }
        }
        tryParse(Regex("\\(met\\)\\d+\\(met\\)")) {
            return@tryParse event.self.getChannel(event.targetId)?.guild
                ?.getGuildUser(it.value.replace("(met)", ""))
                ?.let { uid -> AtUser(uid) }
        }
        tryParse(Regex("\\(role\\)\\d+\\(role\\)")) {
            return@tryParse event.self.getChannel(event.targetId)?.guild
                ?.roleMap?.get(it.value.replace("(role)", "").toInt())
                ?.let { uid -> AtRole(uid) }
        }
        tryParse(Regex.fromLiteral("(met)here(met)")) { AtHere() }
        tryParse(Regex.fromLiteral("(met)all(met)")) { AtAll() }

        return PlainTextComponent(
            kotlin.run {
                if (content[cursor] == '\\') {
                    cursor += 1
                }
                cursor++
                content[cursor].toString()
            }
        )
    }
}