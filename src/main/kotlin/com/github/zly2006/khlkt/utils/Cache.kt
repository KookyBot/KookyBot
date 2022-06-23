package com.github.zly2006.khlkt.utils

class Cache<T>(var updater: (Cache<T>.() -> T?)? = null, var uploader: (Cache<T>.(T) -> Unit)) {
    var cachedValue: T? = updater?.let { it(this) }
        set(value) {
            field = value
            value?.let { uploader(it) }
        }

    fun update() {
        val temp = updater?.let { it() }
        if (temp != null) cachedValue = temp;
    }
}
