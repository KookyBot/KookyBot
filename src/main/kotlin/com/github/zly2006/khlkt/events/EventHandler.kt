package com.github.zly2006.khlkt.events

class EventHandler<T> (
    var handler: (T) -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    fun handle(event: Event) {
        handler(event as T)
    }
}
