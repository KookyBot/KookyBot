package io.github.kookybot.utils

import io.github.kookybot.contract.Guild

data class Emoji(
    val name: String,
    val id: String,
    val guild: Guild? = null,
)
