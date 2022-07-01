/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
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
import io.github.zly2006.kookybot.contract.Channel
import io.github.zly2006.kookybot.contract.Self
import io.github.zly2006.kookybot.contract.User
import io.github.zly2006.kookybot.contract.UserState
import io.github.zly2006.kookybot.data
import io.github.zly2006.kookybot.events.EventManager
import io.github.zly2006.kookybot.events.MessageEvent
import io.github.zly2006.kookybot.events.SelfOnlineEvent
import io.github.zly2006.kookybot.exception.KhlRemoteException
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
}

enum class PingState {
    Pinging,
    Success,
    Timeout,
}

class Client (var token : String) {
    private val logger = LoggerFactory.getLogger(Client::class.java)
    private val context: CoroutineContext = EmptyCoroutineContext
    private val debug = true
    private var lastSn = -1
    private var sessionId = ""
    private var pingTimes = 0
    var pingDelay = 0L
    var status = State.Initialized
    var pingStatus = PingState.Success
    var self: Self? = null
    private var webSocketClient: WebSocketClient? = null
    private val httpClient = HttpClient.newHttpClient()
    val eventManager: EventManager = EventManager(this)
    private val updatableList: MutableList<Updatable> = mutableListOf()
    private var updateJob: Job? = null

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
    }

    private suspend fun ping() {
        logger.debug("ping")
        pingStatus = PingState.Pinging
        webSocketClient?.send("{\"s\":2,\"sn\":$lastSn}")
        pingStart = Calendar.getInstance().timeInMillis
        GlobalScope.launch {
            delay(6000)
            if (pingStatus == PingState.Pinging) {
                pingStatus = PingState.Timeout
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
        logger.trace("Client.postAll: returning $ret")
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
            GATEWAY -> builder.uri(apiOf("/gateway/index?compress=0")).GET()
            GATEWAY_RESUME -> builder.uri(apiOf("/gateway/index?compress=0&sn=$lastSn&resume=1&session_id=$sessionId")).GET()
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
            USER_CHAT_VIEW -> builder.uri(apiOf("/user-chat/view?")).GET()
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

    private suspend fun resume() {
        logger.info("resuming")
        if (status == State.Connected) return
        sendRequest(requestBuilder(GATEWAY_RESUME))
        webSocketClient!!.send("{\"s\":4,\"sn\":$lastSn}")
        delay(6000)
        if (webSocketClient?.isOpen != true) {
            logger.info("resume failed.")
            pingTimes ++
            if (pingTimes == 2) {
                connect()
            }
            else{
                resume()
            }
        }
    }

    private fun initWebsocket(url: String) {
        webSocketClient = object : WebSocketClient(URI(url), mapOf("Authorization" to "Bot $token")) {
            override fun onOpen(handshakedata: ServerHandshake) {
                logger.debug("websocket opened: http status=${handshakedata.httpStatus}")
            }

            override fun onMessage(message: String) {
                pingTimes = 0
                var json = Gson().fromJson(message, JsonObject::class.java)
                when (json.get("s").asInt) {
                    0 -> {
                        if (debug) {
                            logger.info("[Event received] $message")
                        }
                        lastSn = json.get("sn").asInt
                        json = json["d"].asJsonObject
                        eventManager.callEventRaw(json)
                    }
                    1 -> {
                        val code = json["d"].asJsonObject["code"].asInt
                        when (code) {
                            0 -> {
                                logger.info("hello received: ok")
                                status = State.Connected
                                sessionId = json.get("d").asJsonObject.get("session_id").asString
                                self = Self(this@Client)
                            }
                            else -> {
                                throw Exception("khl login failed: code is $code\n  @see <https://developer.kaiheila.cn/doc/websocket>")
                            }
                        }
                    }
                    3 -> {
                        pingDelay = Calendar.getInstance().timeInMillis - pingStart
                        pingStatus = PingState.Success
                        logger.debug("pong, delay = $pingDelay ms")
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

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                logger.info("websocket closed")
                status = State.Disconnected
                GlobalScope.launch {
                    this@Client.connect()
                }
            }
            override fun onError(ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }

    private suspend fun connect(): Self {
        while (true) {
            try {
                val url = sendRequest(requestBuilder(GATEWAY)).asJsonObject.get("url").asString
                logger.info("get gateway success: the address is [$url]")
                initWebsocket(url)
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
                logger.info("[Khl] Received WEBHOOK_CHALLENGE request, challenge: $challenge, Responded")
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
            updatableList.forEach { it.update() }
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
        user.status = when (jsonObject.get("atus").asInt) {
            10 -> UserState.BANNED
            else -> UserState.NORMAL
        }
        user.client = this
        return user
    }

    fun sendChannelMessage(
        type: Int = 9,
        target: Channel,
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
}
