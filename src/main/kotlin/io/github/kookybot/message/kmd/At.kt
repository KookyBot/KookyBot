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

package io.github.kookybot.message.kmd

import io.github.kookybot.contract.GuildRole
import io.github.kookybot.contract.GuildUser

class AtAll : MessageComponent() {
    override fun toMarkdown(): String {
        return "(met)all(met)"
    }
}

class AtHere : MessageComponent() {
    override fun toMarkdown(): String {
        return "(met)here(met)"
    }
}

class AtUser(val user: GuildUser) : MessageComponent() {
    override fun toMarkdown(): String {
        return "(met)${user.id}(met)"
    }
}

class AtRole(val role: GuildRole) : MessageComponent() {
    override fun toMarkdown(): String {
        return "(rol)${role.id}(rol)"
    }
}