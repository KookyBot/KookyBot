package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.utils.Updatable

data class GuildRole (
    val id: Int,
    var name: String,
    var color: Int,
    var position: Int,
    var hoist: Int,
    var mentionable: Int,
    var permissions: Int,
): Updatable {
    override fun update() {
        TODO("Not yet implemented")
    }
}