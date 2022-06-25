package updatable

import com.github.zly2006.khlkt.utils.Updatable
import com.google.gson.Gson

class UpdatableTest(
    var ccc: Int = 0,
    val ddd: Int = 0
):Updatable(){
    var aaa: Int = 0
    val bbb: Int = 0
    var eee = false
    var fff = 1.0
    var ggg = ""
}
fun main() {
    var t = UpdatableTest()
    t.updateByJson(
        Gson().toJsonTree(mapOf(
            "aaa" to 1,
            "bbb" to 2,
            "ccc" to 3,
            "ddd" to 4,
            "eee" to true,
            "fff" to -1,
            "ggg" to "test success"
        ))
    )
    println(t.aaa)
    println(t.bbb)
    println(t.ccc)
    println(t.ddd)
    println(t.eee)
    println(t.fff)
    println(t.ggg)
}