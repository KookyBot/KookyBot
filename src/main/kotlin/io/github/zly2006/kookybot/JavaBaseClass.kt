/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
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

package io.github.zly2006.kookybot

import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.client.State
import io.github.zly2006.kookybot.contract.Self
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun connectWebsocket(client: Client): Self {
    GlobalScope.launch {
        client.start()
    }
    while (client.self == null || client.status != State.Connected) {
        Thread.sleep(100)
    }
    return client.self!!
}
abstract class JavaBaseClass() {
    open suspend fun CoroutineScope.onEnable() {

    }
}