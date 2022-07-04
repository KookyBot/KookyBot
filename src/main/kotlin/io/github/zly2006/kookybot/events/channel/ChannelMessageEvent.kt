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

package io.github.zly2006.kookybot.events.channel

import io.github.zly2006.kookybot.contract.Guild
import io.github.zly2006.kookybot.contract.GuildUser
import io.github.zly2006.kookybot.contract.TextChannel
import io.github.zly2006.kookybot.events.MessageEvent

open class ChannelMessageEvent(
    @field:Transient
    var channel: TextChannel,
    @field:Transient
    var sender: GuildUser,
    @field:Transient
    var guild: Guild,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String
): MessageEvent(_channelType, _type, targetId, authorId, content, sid, timestamp)