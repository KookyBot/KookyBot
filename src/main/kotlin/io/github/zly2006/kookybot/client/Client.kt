/* KookyBot - a SDK of <https://www.kookapp.cn> for JVM platform
Copyright (C) 2022, zly2006 & contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

@file:OptIn(DelicateCoroutinesApi::class)

package io.github.zly2006.kookybot.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.zly2006.kookybot.client.Client.RequestType.*
import io.github.zly2006.kookybot.contract.Self
import io.github.zly2006.kookybot.contract.TextChannel
import io.github.zly2006.kookybot.contract.User
import io.github.zly2006.kookybot.contract.UserState
import io.github.zly2006.kookybot.data
import io.github.zly2006.kookybot.events.EventManager
import io.github.zly2006.kookybot.events.MessageEvent
import io.github.zly2006.kookybot.events.self.SelfOnlineEvent
import io.github.zly2006.kookybot.exception.KookRemoteException
import io.github.zly2006.kookybot.message.SelfMessage
import io.github.zly2006.kookybot.utils.Updatable
import io.javalin.Javalin
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.InflaterInputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

enum class State {
    Initialized,
    Connecting,
    Connected,
    Disconnected,
    FatalError,
    Closed,
}

enum class PingState {
    Pinging,
    Success,
    Timeout,
}

enum class ResumeStatus {
    None,
    First,
    Second,
    Reconnect,
}

class Client (var token : String) {
    private val logger = LoggerFactory.getLogger(Client::class.java)
    private val context: CoroutineContext = EmptyCoroutineContext
    private val debug = true
    private var lastSn = 0
    private var sessionId = ""
    private var resumeTimes = 0
    var resumeStatus: ResumeStatus = ResumeStatus.None
    var voiceWebSocketClient: WebSocketClient? = null
    var pingDelay = 0L
    var status = State.Initialized
    var pingStatus = PingState.Success
    var self: Self? = null
    private var webSocketClient: WebSocketClient? = null
    private val httpClient = HttpClient.newHttpClient()
    val eventManager = EventManager(this)
    private val updatableList: MutableList<Updatable> = mutableListOf()
    private var updateJob: Job? = null

    private var pingStart = Calendar.getInstance().timeInMillis
    private var pingThread = Thread {
        while (status != State.FatalError &&
                status != State.Closed) {
            try {
                Thread.sleep(30000)
                ping()
            } catch (_: Exception) {
            }
        }
    }
    init {
        pingThread.start()
    }

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
        DELETE_CHANNEL_MESSAGE,

        /**
         * requires: channel_id
         */
        VIEW_CHANNEL,
        LIST_CHANNEL,
        LIST_GUILD_ROLE,
        USER_CHAT_LIST,
        SEND_PRIVATE_MESSAGE,
        EDIT_PRIVATE_MESSAGE,
        DELETE_PRIVATE_MESSAGE,
        CREATE_CHAT,

        /**
         * requires: chat_code
         */
        USER_CHAT_VIEW,
        CREATE_ASSET,
        GUILD_USER_LIST,
        VOICE_GATEWAY,
        OFFLINE,
        CREATE_CHANNEL,

    }

    private fun ping() {
        logger.debug("ping")
        pingStatus = PingState.Pinging
        webSocketClient?.send("{\"s\":2,\"sn\":$lastSn}")
        pingStart = Calendar.getInstance().timeInMillis
        GlobalScope.launch {
            delay(6000)
            if (pingStatus == PingState.Pinging) {
                pingStatus = PingState.Timeout
                if (status == State.Closed) return@launch
                status = State.Disconnected
                resume()
            }
        }
    }

    private fun apiOf(path: String): URI {
        return URI(data.baseApiUrl + path)
    }

    private fun postAll(values: Map<String, Any?>): HttpRequest.BodyPublisher? {
        val ret = JsonObject()
        values.entries.forEach {
            when (it.value) {
                null -> return@forEach
                is Number -> ret.addProperty(it.key, it.value as Number)
                is Char -> ret.addProperty(it.key, it.value as Char)
                is String -> ret.addProperty(it.key, it.value as String)
                is Boolean -> ret.addProperty(it.key, it.value as Boolean)
            }
        }
        logger.debug("Client.postAll: returning $ret")
        return BodyPublishers.ofString(ret.toString())
    }

    fun requestBuilder(requestType: RequestType, vararg values: Pair<String, Any?>): HttpRequest {
        val map = mutableMapOf<String, Any?>()
        values.forEach {
            map[it.first] = it.second
        }
        return requestBuilder(requestType, map)
    }

    fun requestBuilder(requestType: RequestType, values: Map<String, Any?>? = null): HttpRequest {
        val builder = HttpRequest.newBuilder().header("Authorization", "Bot $token")
        when (requestType) {
            GATEWAY -> builder.uri(apiOf("/gateway/index")).GET()
            GATEWAY_RESUME -> builder.uri(apiOf("/gateway/index?sn=$lastSn&resume=1&session_id=$sessionId")).GET()
            GUILD_LIST -> builder.uri(apiOf("/guild/list")).GET()
            GUILD_VIEW -> builder.uri(apiOf("/guild/view?guild_id=${values!!["guild_id"]}")).GET()
            USER_VIEW -> builder.uri(apiOf("/user/view?user_id=${values!!["user_id"]}")).GET()
            SEND_PRIVATE_MESSAGE -> builder.uri(apiOf("/direct-message/create"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            SEND_CHANNEL_MESSAGE -> builder.uri(apiOf("/message/create"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            LIST_CHANNEL -> builder.uri(apiOf("/channel/list?target_id=${values!!["channel_id"]}")).GET()
            LIST_GUILD_ROLE -> builder.uri(apiOf("/guild-role/list?guild_id=${values!!["guild_id"]}")).GET()
            USER_ME -> builder.uri(apiOf("/user/me")).GET()
            VIEW_CHANNEL -> builder.uri(apiOf("/channel/view?target_id=${values!!["channel_id"]}")).GET()
            USER_CHAT_LIST -> builder.uri(apiOf("/user-chat/list")).GET()
            USER_CHAT_VIEW -> builder.uri(apiOf("/user-chat/view?chat_code=${values!!["chat_code"]}")).GET()
            CREATE_ASSET -> builder.uri(apiOf("/asset/create"))
                .header("content-type", "form-data")
                .POST(postAll(values!!))
            CREATE_CHAT -> builder.uri(apiOf("/user-chat/create"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            DELETE_PRIVATE_MESSAGE -> builder.uri(apiOf("/direct-message/delete"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            EDIT_PRIVATE_MESSAGE -> builder.uri(apiOf("/direct-message/update"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            DELETE_CHANNEL_MESSAGE -> builder.uri(apiOf("/message/delete"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            EDIT_CHANNEL_MESSAGE -> builder.uri(apiOf("/message/update"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            GUILD_USER_LIST -> builder.uri(apiOf("/guild/user-list?guild_id=${values!!["guild_id"]}")).GET()
            VOICE_GATEWAY -> builder.uri(apiOf("/gateway/voice?channel_id=${values!!["channel_id"]}")).GET()
            OFFLINE -> builder.uri(apiOf("/user/offline"))
                .header("content-type", "application/json")
                .POST(postAll(mapOf()))
            CREATE_CHANNEL -> builder.uri(apiOf("/channel/create"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
        }
        return builder.build()
    }

    fun sendRequest(request: HttpRequest): JsonObject {
        val response = httpClient.send(request, BodyHandlers.ofString())
        val json = Gson().fromJson(response.body(), JsonObject::class.java)
        when (json.get("code").asInt) {
            0 -> {
                return try{
                    json.get("data").asJsonObject
                }
                catch (e: Exception) {
                    if (json.get("data").asJsonArray.isEmpty)
                        JsonObject()
                    else
                        throw e
                    // 官方bug，特殊处理
                }
            }
            else -> {
                throw KookRemoteException(json.get("code").asInt, json.get("message").asString, request)
            }
        }
    }

    private suspend fun resume() {
        logger.info("resuming")
        if (status != State.Disconnected) return
        webSocketClient = initWebsocket(sendRequest(requestBuilder(GATEWAY_RESUME)).get("url").asString)
        webSocketClient!!.connectBlocking()
        webSocketClient!!.send("{\"s\":4,\"sn\":$lastSn}")
        delay(6000)
        if (webSocketClient?.isOpen != true) {
            logger.info("resume failed.")
            resumeTimes++
            if (resumeTimes == 2) {
                connect()
            }
            else{
                resume()
            }
        }
    }

    private fun initWebsocket(url: String): WebSocketClient {
        return object : WebSocketClient(URI(url), mapOf("Authorization" to "Bot $token")) {
            override fun onOpen(handshakedata: ServerHandshake) {
                logger.debug("websocket opened: http status=${handshakedata.httpStatus}")
            }

            override fun onMessage(bytes: ByteBuffer?) {
                if (bytes == null) return
                onMessage(
                    InflaterInputStream(bytes.array().inputStream()).bufferedReader().readText()
                )
            }

            override fun onMessage(message: String) {
                logger.debug("[Event received] $message")
                resumeTimes = 0
                var json = Gson().fromJson(message, JsonObject::class.java)
                when (json.get("s").asInt) {
                    0 -> {
                        lastSn = json.get("sn").asInt
                        json = json["d"].asJsonObject
                        eventManager.callEventRaw(json)
                    }
                    1 -> {
                        when (val code = json["d"].asJsonObject["code"].asInt) {
                            0 -> {
                                logger.info("hello received: ok")
                                status = State.Connected
                                sessionId = json.get("d").asJsonObject.get("session_id").asString
                                self = Self(this@Client)
                            }
                            40101 -> throw Error("token无效，请使用正确的token")
                            40102 -> throw Error("token无效，请使用正确的token")
                            40103 -> this@Client.reconnect()
                            40107 -> this@Client.reconnect()
                            40108 -> this@Client.reconnect()
                            else -> {
                                throw Exception("KOOK login failed: code is $code\n  @see <https://developer.kookapp.cn/doc/websocket>")
                            }
                        }
                    }
                    3 -> {
                        pingDelay = Calendar.getInstance().timeInMillis - pingStart
                        pingStatus = PingState.Success
                        logger.debug("pong, delay = $pingDelay ms")
                    }
                    5 -> {
                        this@Client.reconnect()
                    }
                    6 -> {
                        pingStatus = PingState.Success
                        status = State.Connected
                        sessionId = json.get("d").asJsonObject.get("session_id").asString
                        logger.info("resume ok!")
                    }
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                logger.info("websocket closed, code=$code, reason=$reason")
                if (status == State.Closed) return
                status = State.Disconnected
                GlobalScope.launch {
                    this@Client.resume()
                }
            }
            override fun onError(ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun reconnect() {
        logger.info("reconnecting")
        webSocketClient!!.closeBlocking()
        sessionId = ""
        lastSn = -1
        self = null
        GlobalScope.launch {
            delay(10000)
            this@Client.connect()
        }
    }

    private suspend fun connect(): Self {
        while (true) {
            try {
                val url = sendRequest(requestBuilder(GATEWAY)).asJsonObject.get("url").asString
                logger.info("get gateway success: the address is [$url]")
                webSocketClient = initWebsocket(url)
                status = State.Connecting
                if ((webSocketClient as WebSocketClient).connectBlocking(6000, TimeUnit.MILLISECONDS)) {
                    logger.info("websocket connected!")
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
            delay(5000)
            logger.error("websocket connect failed, reconnecting in 5 seconds...")
        }
    }

    private fun whInit(host: String, port: Int, path: String, verifyToken: String = "") {
        val app = Javalin.create().start(host, port)
        status = State.Connecting
        app.post(path) { ctx ->
            val text = InflaterInputStream(ctx.bodyAsBytes().inputStream()).bufferedReader().use { it.readText() }
            val json = Gson().fromJson(text, JsonObject::class.java)
            if (json["s"].asInt != 0) return@post
            json["d"].asJsonObject
            if (verifyToken.isNotEmpty()) {
                if (json["verify_token"].asString != verifyToken) return@post
            }
            val event = Gson().fromJson(json, MessageEvent::class.java)
            if (event.eventType == MessageEvent.EventType.SYSTEM &&
                json["channel_type"].asString == "WEBHOOK_CHALLENGE"
            ) {
                val challenge = json["challenge"].asString
                ctx.contentType("application/json").result("{\"challenge\":\"$challenge\"}")
                logger.info("[KOOK] Received WEBHOOK_CHALLENGE request, challenge: $challenge, Responded")
                status = State.Connected
                self = Self(client = this@Client)
            }
            eventManager.callEventRaw(json)
        }
    }

    private suspend fun getWhSelf(): Self {
        while (true) {
            if (self != null) return self!!
            delay(100L)
        }
    }

    suspend fun start(host: String = "", port: Int = 0, path: String = "", verifyToken: String = ""): Self {
        val self = if (host.isEmpty()) {
            connect()
        } else {
            whInit(host, port, path, verifyToken)
            getWhSelf()
        }
        updateJob = GlobalScope.launch {
         //   updatableList.forEach { it.update() }
            delay(30 * 1000)
        }
        eventManager.callEvent(SelfOnlineEvent(self))
        return self
    }

    fun getUser(userId: String): User {
        val jsonObject = sendRequest(requestBuilder(USER_VIEW, mapOf("user_id" to userId))).asJsonObject
        if (!jsonObject.has("vip_avatar")) {
            jsonObject.addProperty("vip_avatar", "")
        }
        val user = Gson().fromJson(jsonObject, User::class.java)
        user.status = when (jsonObject.get("status").asInt) {
            10 -> UserState.BANNED
            else -> UserState.NORMAL
        }
        user.client = this
        return user
    }

    fun sendChannelMessage(
        type: Int = 9,
        target: TextChannel,
        tempTarget: String? = null,
        nonce: String? = null,
        content: String,
        quote: String? = null
    ): SelfMessage {
        val ret = sendRequest(requestBuilder(SEND_CHANNEL_MESSAGE,
            "type" to type,
            "target_id" to target.id,
            "temp_target_id" to tempTarget,
            "nonce" to nonce,
            "content" to content,
            "quote" to quote,
        ))
        return SelfMessage(
            client = this,
            id = ret.get("msg_id").asString,
            timestamp = ret.get("msg_timestamp").asInt,
            target = target,
            content = content
        )
    }
    fun sendUserMessage(
        type: Int = 9,
        target: User,
        nonce: String? = null,
        content: String,
        quote: String? = null
    ): SelfMessage {
        if (self!!.chattingUsers.find { it.id == target.id } == null) {
            sendRequest(requestBuilder(CREATE_CHAT, "target_id" to target.id))
        }
        val ret = sendRequest(requestBuilder(SEND_PRIVATE_MESSAGE,
            "type" to type,
            "target_id" to target.id,
            "nonce" to nonce,
            "content" to content,
            "quote" to quote,
        ))
        return SelfMessage(
            client = this,
            id = ret.get("msg_id").asString,
            timestamp = ret.get("msg_timestamp").asInt,
            target = target,
            content = content
        )
    }
    fun upload(file: File): String {
        val data = file.readBytes()
        return sendRequest(requestBuilder(CREATE_ASSET, "file" to data.toString(StandardCharsets.US_ASCII))).get("url").asString
    }
    fun close() {
        sendRequest(requestBuilder(OFFLINE))
        status = State.Closed
        webSocketClient?.closeBlocking()
        pingThread.interrupt()
        updateJob?.cancel()
    }


}
