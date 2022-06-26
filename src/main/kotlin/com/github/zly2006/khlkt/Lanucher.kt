/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
Copyright (C) <year>  <name of author>

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

package com.github.zly2006.khlkt
import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.events.ChannelMessageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import java.io.File

suspend fun main() {
    if (!File("data/").exists())
        File("data/").mkdir()
    if (!File("data/token.txt").isFile) {
        withContext(Dispatchers.IO) {
            File("data/token.txt").createNewFile()
        }
        println("please fill your token in data/token/txt")
        return
    }
    val token = File("data/token.txt").readText()
    val client = Client(token)
    val self = client.start()
    client.eventManager.addListener<ChannelMessageEvent> {
        if (content.contains("hello")) {
            channel.sendCardMessage {
                Card {
                    HeaderModule(PlainTextElement("Hello"))
                    Divider()
                }
            }
        }
    }
    awaitCancellation()
}
