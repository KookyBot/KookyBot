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

package io.github.kookybot

import io.github.kookybot.client.Client
import io.github.kookybot.client.State
import io.github.kookybot.contract.Self
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class JavaBaseClass() {
    @OptIn(DelicateCoroutinesApi::class)
    companion object utils {
        @JvmStatic
        fun connectWebsocket(client: Client): Self {
            GlobalScope.launch {
                client.start()
            }
            while (client.self == null || client.status != State.Connected) {
                Thread.sleep(100)
            }
            return client.self!!
        }
    }
}

fun JavaBaseClass.cnt(client: Client) {
    JavaBaseClass.connectWebsocket(client)
}