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
package io.github.zly2006.kookybot.events

import io.github.zly2006.kookybot.contract.Channel
import io.github.zly2006.kookybot.contract.Guild
import io.github.zly2006.kookybot.contract.GuildUser
import io.github.zly2006.kookybot.utils.Emoji

class ChannelCancelReactionEvent (
    var emoji: Emoji,
    channel: Channel,
    sender: GuildUser,
    guild: Guild,
    _channelType: String,
    _type: Int,
    targetId: String,
    authorId: String,
    content: String,
    sid: String,
    timestamp: String
): ChannelMessageEvent(channel, sender, guild, _channelType, _type, targetId, authorId, content, sid, timestamp)