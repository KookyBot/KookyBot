package io.github.zly2006.kookybot.commands

abstract class Command(
    val name: String,
    val alias: List<String> = listOf(),
    val permission: String? = null,
    val description: String? = null
) {
    abstract fun onExecute(context: CommandContext)
}