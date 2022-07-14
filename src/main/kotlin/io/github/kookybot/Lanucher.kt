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
import io.github.kookybot.plugin.Plugin
import java.io.File
import java.net.URLClassLoader

interface Lanucher {
    val plugins: MutableList<Plugin>
}

val launcher = object : Lanucher {
    init {
        if (!File("data/").exists())
            File("data/").mkdir()
        if (!File("plugins/").exists())
            File("plugins/").mkdir()
        if (!File("data/token.txt").isFile) {
            File("data/token.txt").createNewFile()
            println("please fill your token in data/token/txt")
        }
        File("plugins/").listFiles()?.forEach {
            try {
                if (it.name.endsWith(".jar")) run {
                    val loader = URLClassLoader(arrayOf(it.toURI().toURL()))
                    val content = loader.getResource("plugin.yml")!!.content
                }
                if (it.name.endsWith(".class")) run {
                    val loader = URLClassLoader(arrayOf(it.parentFile.toURI().toURL()))
                    loader.loadClass(it.nameWithoutExtension)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override val plugins: MutableList<Plugin> = mutableListOf()
}

suspend fun main() {
    val token = File("data/token.txt").readText()
    val client = Client(token) {
        withDefaultCommands()
    }
    val self = client.start()
    while (true) {
        val cmd = readln()
        client.eventManager.parseCommand(cmd)
    }
}
