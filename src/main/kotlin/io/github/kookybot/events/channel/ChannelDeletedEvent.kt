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

import io.github.kookybot.contract.Channel
import io.github.kookybot.contract.Self
import io.github.kookybot.events.Event

class ChannelDeletedEvent(
    val id: String,
    /**
     * 这表示已经缓存的数据
     *
     * 如果没有被缓存过，这将会是null
     *
     * 对于任何频道，您都可以使用[Channel.update]缓存之
     */
    val channel: Channel?,
    val channelId: String,
    override val self: Self,
) : Event