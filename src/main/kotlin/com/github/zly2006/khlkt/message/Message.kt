package com.github.zly2006.khlkt.message

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.contract.MessageReceiver

open class Message(
    val client: Client,
    val primaryReceiver: MessageReceiver
) {
    open fun content(): String {
        return ""
    }
    fun test() {
        // TODO
        CardMessage (client, primaryReceiver) {
            AtAll
            ""
        }
    }
}