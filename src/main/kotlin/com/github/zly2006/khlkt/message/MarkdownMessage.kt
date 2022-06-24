package com.github.zly2006.khlkt.message

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.contract.MessageReceiver

class MarkdownMessage(
    client: Client,
    content: String,
    receiver: MessageReceiver) : Message(client, receiver) {
}