package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.message.Message

abstract class MessageReceiver {
    open fun sendMessage(message: Message) {

    }
    abstract val id: String
}