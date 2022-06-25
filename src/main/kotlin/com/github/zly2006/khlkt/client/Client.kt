@file:OptIn(DelicateCoroutinesApi::class)

package com.github.zly2006.khlkt.client

import com.github.zly2006.khlkt.client.Client.RequestType.*
import com.github.zly2006.khlkt.contract.*
import com.github.zly2006.khlkt.data
import com.github.zly2006.khlkt.events.ChannelMessageEvent
import com.github.zly2006.khlkt.events.Event
import com.github.zly2006.khlkt.exception.KhlRemoteException
import com.github.zly2006.khlkt.message.Message
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.*
import java.util.concurrent.TimeUnit

class Client (var token : String) {
    val debug = true
    var lastSn = -1
    var sessionId = ""
    var pingDelay = 0L
    var messageHandler: (Client.(Message) -> Unit)? = null
    var loginHandler: (Client.(Boolean) -> Unit)? = null
    var status = State.Initialized
    var pingStatus = PingState.Success
    var self: Self? = null
    var webSocketClient: WebSocketClient? = null

    private var pingStart = Calendar.getInstance().timeInMillis
    private var pingJob: Job = GlobalScope.launch {
        try {
            while (status != State.FatalError) {
                delay(30000)
                ping()
            }
        } catch (_: Exception) {
        }
    }
    private val httpClient = HttpClient.newHttpClient()

    /**
     * 警告：
     * 强烈不建议使用，请使用封装好的内容
     * 使用不当，后果自负
     */
    enum class RequestType {
        GATEWAY,
        GATEWAY_RESUME,
        GUILD_LIST,

        /**
         * require: guild_id
         */
        GUILD_VIEW,

        /**
         * requires: user_id
         * optional: guild_id
         */
        USER_VIEW,
        USER_ME,
        SEND_CHANNEL_MESSAGE,
        EDIT_CHANNEL_MESSAGE,

        /**
         * requires: channel_id
         */
        VIEW_CHANNEL,
        LIST_CHANNEL,
        LIST_GUILD_ROLE,
    }

    private suspend fun ping() {
        if (debug) println("ping")
        pingStatus = PingState.Pinging
        webSocketClient?.send("{\"s\":2,\"sn\":$lastSn}")
        pingStart = Calendar.getInstance().timeInMillis
        GlobalScope.launch {
            delay(6000)
            if (pingStatus == PingState.Pinging) {
                pingStatus = PingState.Timeout
                status = State.Disconnected
            }
        }
    }

    private fun apiOf(path: String): URI {
        return URI(data.baseApiUrl + path)
    }

    private fun postAll(values: Map<String, Any?>): HttpRequest.BodyPublisher? {
        val ret = JsonObject()
        values.entries.forEach {
            if (it.value == null) {
                return@forEach
            }
            else if (it.value is Number) {
                ret.addProperty(it.key, it.value as Number)
            }
            else if (it.value is Char) {
                ret.addProperty(it.key, it.value as Char)
            }
            else if (it.value is String) {
                ret.addProperty(it.key, it.value as String)
            }
            else if (it.value is Boolean) {
                ret.addProperty(it.key, it.value as Boolean)
            }
        }
        println(ret.toString())
        return BodyPublishers.ofString(ret.toString())
    }

    fun requestBuilder(requestType: RequestType, values: Map<String, Any?>? = null): HttpRequest {
        var builder = HttpRequest.newBuilder().header("Authorization", "Bot $token");
        when (requestType) {
            GATEWAY -> builder.uri(apiOf("/gateway/index?compress=0")).GET()
            GATEWAY_RESUME -> builder.uri(apiOf("/gateway/index?compress=0&sn=$lastSn&resume=1&session_id=$sessionId")).GET()
            GUILD_LIST -> builder.uri(apiOf("/guild/list")).GET()
            GUILD_VIEW -> builder.uri(apiOf("/guild/view?guild_id=${values!!["guild_id"]}")).GET()
            USER_VIEW -> builder.uri(apiOf("/user/view?user_id=${values!!["user_id"]}")).GET()
            SEND_CHANNEL_MESSAGE -> builder.uri(apiOf("/message/create"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            LIST_GUILD_ROLE -> builder.uri(apiOf("/guild-role/list?guild_id=${values!!["guild_id"]}")).GET()
            USER_ME -> builder.uri(apiOf("/user/me")).GET()
            VIEW_CHANNEL -> builder.uri(apiOf("/channel/view?target_id=${values!!["channel_id"]}")).GET()
            else -> throw Exception("todo")
        }
        return builder.build()
    }

    fun sendRequest(request: HttpRequest): JsonObject {
        val response = httpClient.send(request, BodyHandlers.ofString())
        val json = Gson().fromJson(response.body(), JsonObject::class.java)
        when (json.get("code").asInt) {
            0 -> {
                return json.get("data").asJsonObject
            }
            else -> {
                throw KhlRemoteException(json.get("code").asInt, json.get("message").asString, request)
            }
        }
    }

    private fun resume() {
        if (webSocketClient == null) return
        if (webSocketClient!!.isClosed) return
        webSocketClient!!.send("{\"s\":4,\"sn\":$lastSn}")
    }

    suspend fun connect(): Self {
        while (true) {
            try {
                var url = sendRequest(requestBuilder(GATEWAY)).asJsonObject.get("url").asString
                println("get gateway success: the address is [$url]")
                webSocketClient = object : WebSocketClient(URI(url), mapOf("Authorization" to "Bot $token")) {
                    override fun onOpen(handshakedata: ServerHandshake) {
                        println("websocket opened: http status=${handshakedata.httpStatus}")
                    }

                    override fun onMessage(message: String) {
                        var json = Gson().fromJson(message, JsonObject::class.java)
                        when (json.get("s").asInt) {
                            0 -> {
                                if (debug) {
                                    println("[Event received] $message")
                                }
                                lastSn = json.get("sn").asInt
                                json = json["d"].asJsonObject
                                val event = Gson().fromJson(json, Event::class.java)
                                if (event.eventType == Event.EventType.SYSTEM) {
                                    // TODO
                                }
                                if (event.authorId == self?.id)
                                    return
                                else if (event.channelType == Event.ChannelType.GROUP) {
                                    val channelMessageEvent = Gson().fromJson(json, ChannelMessageEvent::class.java)
                                    var guild = self!!.guilds.cachedValue?.filter { it.cachedValue?.id == json["extra"].asJsonObject["guild_id"].asString }?.firstOrNull()
                                    channelMessageEvent.guild = guild!!
                                    var channel = guild.cachedValue!!.channels.filter { it.cachedValue?.id == json["target_id"].asString }.firstOrNull()
                                    channelMessageEvent.channel = channel!!
                                    if (channelMessageEvent.content == "hello") {
                                        channel.cachedValue!!.sendMessage("hello")
                                    }
                                }
                            }
                            1 -> {
                                var code = json["d"].asJsonObject["code"].asInt
                                when (code) {
                                    0 -> {
                                        println("hello received: ok")
                                        status = State.Connected
                                        loginHandler?.let { it(false) }
                                        sessionId = json.get("d").asJsonObject.get("session_id").asString
                                        self = Self(client = this@Client)
                                    }
                                    else -> {
                                        throw Exception("khl login failed: code is $code\n  @see <https://developer.kaiheila.cn/doc/websocket>")
                                    }
                                }
                            }
                            3 -> {
                                pingDelay = Calendar.getInstance().timeInMillis - pingStart
                                pingStatus = PingState.Success
                                if (debug) println("pong, delay = $pingDelay ms")
                            }
                            5 -> {
                                pingJob.cancel()
                                webSocketClient?.close()
                                sessionId = ""
                                lastSn = -1
                                self = null
                                connect()
                            }
                            6 -> {
                                pingStatus = PingState.Success
                                status = State.Connected
                                sessionId = json.get("d").asJsonObject.get("session_id").asString
                            }
                        }
                    }

                    override fun onClose(code: Int, reason: String, remote: Boolean) {}
                    override fun onError(ex: java.lang.Exception) {
                        ex.printStackTrace()
                    }
                }
                status = State.Connecting
                if ((webSocketClient as WebSocketClient).connectBlocking(6000, TimeUnit.MILLISECONDS)) {
                    println("websocket connected!")
                    while (status != State.Connected || self == null) {
                        delay(100)
                    }
                    ping()
                    return self!!
                }
            } catch (e: IOException) {
                status = State.FatalError
                e.printStackTrace()
            }
        }
    }

    fun getUser(userId: String): User {
        var jsonObject = sendRequest(requestBuilder(USER_VIEW, mapOf("user_id" to userId))).asJsonObject
        if (!jsonObject.has("vip_avatar")) {
            jsonObject.addProperty("vip_avatar", "")
        }
        val user = Gson().fromJson(jsonObject, User::class.java)
        user.status = when (jsonObject.get("atus").asInt) {
            10 -> UserState.BANNED
            else -> UserState.NORMAL
        }
        return user
    }

    fun getServerUser(userId: String, guildId: String): GuildUser {
        var jsonObject = sendRequest(requestBuilder(USER_VIEW, mapOf("user_id" to userId, "guild_id" to guildId))).asJsonObject
        if (!jsonObject.has("vip_avatar")) {
            jsonObject.addProperty("vip_avatar", "")
        }
        val guildUser = Gson().fromJson(jsonObject, GuildUser::class.java)
        guildUser.status = when (jsonObject.get("atus").asInt) {
            10 -> UserState.BANNED
            else -> UserState.NORMAL
        }
        return guildUser
    }

    fun sendChannelMessage(
        type: Int = 9,
        target: Channel,
        tempTarget: String? = null,
        nonce: String? = null,
        content: String,
        quote: String? = null
    ) {
        sendRequest(requestBuilder(SEND_CHANNEL_MESSAGE, mapOf (
            "type" to type,
            "target_id" to target.id,
            "temp_target_id" to tempTarget,
            "nonce" to nonce,
            "content" to content,
            "quote" to quote,
        )))
    }
}
