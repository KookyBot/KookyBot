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

package io.github.zly2006.kookybot
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.plugin.Plugin
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
            if (it.name.endsWith(".jar")) run {
                val loader = URLClassLoader(arrayOf(it.toURI().toURL()))
                val content = loader.getResource("plugin.yml").content
            }
            if (it.name.endsWith(".class")) run {
                val loader = URLClassLoader(arrayOf(it.parentFile.toURI().toURL()))
                loader.loadClass(it.nameWithoutExtension)
            }
        }
    }

    override val plugins: MutableList<Plugin>
        get() = TODO("Not yet implemented")
}

suspend fun main() {
    val token = File("data/token.txt").readText()
    val client = Client(token)
    val self = client.start()
    while (true) {
        val cmd = readln().split(' ')
        when (cmd[0]) {
            "license" -> {
                println("KookyBot Console")
                println("This is a free software under AGPL v3.")
                println("Copyright (c) 2022, zly2006 & contributors.")

                println("Reference & thx: khl-voice-API by hank9999 at <https://github.com/hank9999/khl-voice-API>")
            }
        }
    }
}
