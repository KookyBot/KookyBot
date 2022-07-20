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

package io.github.kookybot.events.channel

import io.github.kookybot.contract.Guild
import io.github.kookybot.contract.GuildUser
import io.github.kookybot.contract.Self
import io.github.kookybot.contract.TextChannel
import io.github.kookybot.utils.Emoji

class ChannelCancelReactionEvent(
    self: Self,
    emoji: Emoji,
    channel: TextChannel,
    sender: GuildUser,
    guild: Guild,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String,
) : ChannelMessageEvent(
    self,
    channel,
    sender,
    guild,
    _channelType,
    _type,
    targetId,
    authorId,
    content,
    sid,
    timestamp
) {
    @field:Transient
    var emoji: Emoji = emoji
        internal set
}