package com.github.zly2006.khlkt
import com.github.zly2006.khlkt.client.Client
import kotlinx.coroutines.awaitCancellation
import java.io.File

suspend fun main() {


    if (!File("data/").exists())
        File("data/").mkdir()
    if (!File("data/token.txt").isFile) {
        File("data/token.txt").createNewFile()
        println("please fill your token in data/token/txt")
        return
    }
    val token = File("data/token.txt").readText()
    val client = Client(token)
    println(token)
    val self = client.start()
    self.id
    awaitCancellation()
}
