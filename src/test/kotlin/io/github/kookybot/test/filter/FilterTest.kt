package io.github.kookybot.test.filter

import com.google.gson.Gson
import io.github.kookybot.annotation.Filter
import io.github.kookybot.events.parse

fun main() {
    val ret = Filter(pattern = ".help {command}").parse(".help abc")
    println(Gson().toJson(ret))
}