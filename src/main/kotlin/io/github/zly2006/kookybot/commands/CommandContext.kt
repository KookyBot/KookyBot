/* KookyBot - a SDK of <https://www.kookapp.cn> for JVM platform
Copyright (C) 2022, zly2006 & contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package io.github.zly2006.kookybot.commands

import io.github.zly2006.kookybot.contract.TextChannel
import io.github.zly2006.kookybot.contract.User

data class CommandContext(
    val user: User,
    val channel: TextChannel? = null,
    val label: String,
    val command: Command,
    val args: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandContext

        if (user != other.user) return false
        if (channel != other.channel) return false
        if (label != other.label) return false
        if (command != other.command) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + (channel?.hashCode() ?: 0)
        result = 31 * result + label.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }
}

