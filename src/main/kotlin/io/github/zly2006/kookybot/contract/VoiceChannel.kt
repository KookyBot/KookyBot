package io.github.zly2006.kookybot.contract

import io.github.zly2006.kookybot.client.Client

class VoiceChannel(
    client: Client,
    id: String,
    guild: Guild
) : Channel(client, id, guild) {
}