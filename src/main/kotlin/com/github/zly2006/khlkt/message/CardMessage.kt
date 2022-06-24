package com.github.zly2006.khlkt.message

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.contract.MessageReceiver

class CardMessage (
    client: Client,
    primaryReceiver: MessageReceiver,
    var contentBuilder: CardMessage.() -> Unit,
): Message(client, primaryReceiver) {
    override fun content(): String {
        return super.content()
    }
}