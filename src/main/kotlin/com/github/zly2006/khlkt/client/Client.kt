package com.github.zly2006.khlkt.client

import com.github.zly2006.khlkt.client.Client.RequestType.*
import com.github.zly2006.khlkt.contract.Self
import com.github.zly2006.khlkt.contract.User
import com.github.zly2006.khlkt.contract.UserState
import com.github.zly2006.khlkt.data
import com.github.zly2006.khlkt.message.Message
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.TimeUnit

class Client (var token : String) {
    val debug = true
    var lastSn = -1
    var sessionId = ""
    var messageHandler: (Client.(Message) -> Unit)? = null
    var loginHandler: (Client.(Boolean) -> Unit)? = null
    var status = State.Initialized
    var pingStatus = PingState.Success
    var self: Self? = Self(this)
    var webSocketClient: WebSocketClient? = null

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
    public enum class RequestType {
        GATEWAY,
        GATEWAY_RESUME,
        GUILD_LIST,

        /**
         * require: guild_id
         */
        GUILD_VIEW,

        /**
         * requires: user_id
         */
        USER_VIEW,
    }

    private suspend fun ping() {
        if (debug) println("ping")
        pingStatus = PingState.Pinging
        webSocketClient?.send("{\"s\":2,\"sn\":$lastSn}")
        GlobalScope.launch {
            delay(6000)
            if (pingStatus == PingState.Pinging) {
                pingStatus = PingState.Timeout
                status = State.Disconnected
            }
        }
    }

    fun apiOf(path: String): String {
        return data.baseApiUrl + path;
    }

    fun requestBuilder(requestType: RequestType, values: Map<String, Any>? = null): HttpRequest {
        var builder = HttpRequest.newBuilder().header("Authorization", "Bot $token");
        when (requestType) {
            GATEWAY -> {
                builder.uri(URI(apiOf("/gateway/index?compress=0"))).GET()
            }
            GATEWAY_RESUME -> {
                builder.uri(URI(apiOf("/gateway/index?compress=0&sn=$lastSn&resume=1&session_id=$sessionId"))).GET()
            }
            GUILD_LIST -> {
                builder.uri(URI(apiOf("/guild/list"))).GET()
            }
            GUILD_VIEW -> {
                builder.uri(URI(apiOf("/guild/view?guild_id=${values!!["guild_id"]}"))).GET()
            }
            USER_VIEW -> {
                builder.uri(URI(apiOf("/user/view?user_id=${values!!["user_id"]}"))).GET()
            }
            else -> throw Exception("illegal type")
        }
        return builder.build()
    }

    fun sendRequest(request: HttpRequest): JsonElement {
        var response = httpClient.send(requestBuilder(GATEWAY), BodyHandlers.ofString())
        var json = Gson().fromJson(response.body(), JsonObject::class.java)
        when (json.get("code").asInt) {
            0 -> {
                return json.get("data")
            }
            else -> {
                throw Exception("khl server returned an error[${json.get("code").asInt}]: ${json.get("message").asString}")
            }
        }
    }

    private fun resume() {
        if (webSocketClient == null) return
        if (webSocketClient!!.isClosed) return
        webSocketClient!!.send("{\"s\":4,\"sn\":$lastSn}")
    }

    suspend fun connect() {
        while (true) {
            try {
                var url = sendRequest(requestBuilder(GATEWAY)).asJsonObject.get("url").asString
                println("get gateway success: the address is [$url]")
                webSocketClient = object : WebSocketClient(URI(url), mapOf("Authorization" to "Bot $token")) {
                    override fun onOpen(handshakedata: ServerHandshake) {
                        println("websocket opened: http status=${handshakedata.httpStatus}")
                    }

                    override fun onMessage(message: String) {
                        if (debug) {
                            println("[Message received] $message")
                        }
                        var json = Gson().fromJson(message, JsonObject::class.java)
                        when (json.get("s").asInt) {
                            0 -> {
                                lastSn = json.get("sn").asInt
                            }
                            1 -> {
                                var code = json["d"].asJsonObject["code"].asInt
                                when (code) {
                                    0 -> {
                                        println("hello received: ok")
                                        status = State.Connected
                                        loginHandler?.let { it(false) }
                                        sessionId = json.get("d").asJsonObject.get("session_id").asString
                                    }
                                    else -> {
                                        throw Exception("khl login failed: code is $code\n  @see <https://developer.kaiheila.cn/doc/websocket>")
                                    }
                                }
                            }
                            3 -> {
                                pingStatus = PingState.Success
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
                    override fun onError(ex: java.lang.Exception) {}
                }
                status = State.Connecting
                if ((webSocketClient as WebSocketClient).connectBlocking(6000, TimeUnit.MILLISECONDS)) {
                    println("websocket connected!")
                    break
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
        var user = User(
            avatarUrl = jsonObject.get("avatar").asString,
            bot = jsonObject.get("bot").asBoolean,
            identifyNumber = jsonObject.get("identify_num").asInt,
            mobilePhoneVerified = jsonObject.get("mobile_verified").asBoolean,
            status = when (jsonObject.get("atus").asInt) {
                10 -> UserState.BANNED
                else -> UserState.NORMAL
            },
            name = jsonObject.get("username").asString,
            vipAvatarUrl = jsonObject.get("vip_avatar").asString,
            id = jsonObject.get("id").asInt,
            oline = jsonObject.get("online").asBoolean,
            isVip = false
        )
        return user
    }
}
