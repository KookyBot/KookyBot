package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.message.Message

abstract class MessageReceiver {
    abstract fun sendMessage(message: Message)
    abstract val id: String
}