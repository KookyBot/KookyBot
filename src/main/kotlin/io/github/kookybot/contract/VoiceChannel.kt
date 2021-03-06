package io.github.kookybot.contract

import io.github.kookybot.client.Client
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class VoiceChannel(
    client: Client,
    id: String,
    guild: Guild
) : Channel(client, id, guild) {
    var voiceWebSocketClient: WebSocketClient = object : WebSocketClient(URI("https://example.com")) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            TODO("Not yet implemented")
        }

        override fun onMessage(message: String?) {
            TODO("Not yet implemented")
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onError(ex: java.lang.Exception?) {
            TODO("Not yet implemented")
        }

    }

    fun disconnect() {

    }

    fun connect() {
        client.currentVoiceChannel?.disconnect()
        client.currentVoiceChannel = this
        voiceWebSocketClient = object : WebSocketClient(
            URI(with(client) {
                sendRequest(
                    requestBuilder(
                        Client.RequestType.VOICE_GATEWAY,
                        "channel_id" to id
                    )
                )
            }.get("data").asJsonObject.get("gateway_url").asString),

            mapOf("Authorization" to "Bot ${client.token}")
        ) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                send(
                    "{\n" +
                            "    \"request\": true,\n" +
                            "    \"id\": 5884630,\n" +
                            "    \"method\": \"getRouterRtpCapabilities\",\n" +
                            "    \"data\": {}\n" +
                        "}")
                send("{\n" +
                        "    \"request\": true,\n" +
                        "    \"id\": 6112963,\n" +
                        "    \"method\": \"createWebRtcTransport\",\n" +
                        "    \"data\": {\n" +
                        "        \"forceTcp\": false,\n" +
                        "        \"producing\": true,\n" +
                        "        \"consuming\": false,\n" +
                        "        \"sctpCapabilities\": {\n" +
                        "            \"numStreams\": {\n" +
                        "                \"OS\": 1024,\n" +
                        "                \"MIS\": 1024\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
            }

            override fun onMessage(message: String?) {
                TODO("Not yet implemented")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                TODO("Not yet implemented")
            }

            override fun onError(ex: Exception?) {
                TODO("Not yet implemented")
            }
        }
    }
}