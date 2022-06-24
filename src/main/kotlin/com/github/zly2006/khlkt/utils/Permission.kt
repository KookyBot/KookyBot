package com.github.zly2006.khlkt.utils

class Permission(
    private var num: Long = 0
) {
    val unsafe = num

    enum class Permissions {

    }

    fun get(perm: Long): Boolean {
        return num.and(perm) == perm
    }
    fun set(perm: Long, value: Boolean) {
        num = num.and(
            if (value) perm
            else perm.inv()
        )
    }
}