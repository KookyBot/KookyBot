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

package io.github.kookybot.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kfc.exceptions.CrazyThursdayMoneyNotEnoughException
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.kookybot.client.Client.RequestType.*
import io.github.kookybot.client.ResumeStatus.*
import io.github.kookybot.commands.CommandSource
import io.github.kookybot.commands.PermissionManager
import io.github.kookybot.commands.UserArgumentType
import io.github.kookybot.contract.*
import io.github.kookybot.data
import io.github.kookybot.events.EventManager
import io.github.kookybot.events.MessageEvent
import io.github.kookybot.events.self.SelfOnlineEvent
import io.github.kookybot.exception.KookRemoteException
import io.github.kookybot.message.SelfMessage
import io.github.kookybot.utils.Updatable
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
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.InflaterInputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess

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

class Client(var token: String, var configure: (ConfigureScope.() -> Unit)? = null) {
    private val logger = LoggerFactory.getLogger(Client::class.java)
    private val context: CoroutineContext = EmptyCoroutineContext
    private var lastSn = 0
    private var sessionId = ""
    var resumeStatus: ResumeStatus = None
    var pingDelay = 0L
    var status = State.Initialized
    var pingStatus = PingState.Success
    var self: Self? = null
    private var webSocketClient: WebSocketClient? = null
    private val httpClient = HttpClient.newHttpClient()
    internal val config = ConfigureScope(this)
    var currentVoiceChannel: VoiceChannel? = null

    // for init
    internal var selfId = ""

    // configure client
    init {
        configure?.let {
            config.it()
        }
        if (!config.dataFolder.isDirectory)
            config.dataFolder.mkdir()
        if (config.botMarketUUID != "") {
            GlobalScope.launch {
                while (true) {
                    httpClient.send(
                        HttpRequest.newBuilder()
                            .uri(URI("http://bot.gekj.net/api/v1/online.bot"))
                            .header("uuid", config.botMarketUUID)
                            .build(), BodyHandlers.ofString()
                    ).body()
                    delay(30 * 60)
                }
            }
        }
    }

    val permissionManager = PermissionManager(this)
    val eventManager = EventManager(this)

    init { // initialize default commands.
        if (config.defaultCommand) {
            eventManager.dispatcher.run {
                register(LiteralArgumentBuilder.literal<CommandSource?>("help")
                    .executes { context ->
                        context.source.sendMessage(
                            getSmartUsage(root, context.source).toList().joinToString("") { "\n/${it.second}" }
                        )
                        0
                    }
                    .then(
                        RequiredArgumentBuilder.argument<CommandSource?, String?>(
                        "command",
                        StringArgumentType.greedyString()
                    )
                        .executes { context ->
                            val parseResults: ParseResults<CommandSource> =
                                parse(StringArgumentType.getString(context, "command"), context.source);
                            if (parseResults.getContext().getNodes().isEmpty()) {
                                throw Exception("Command not found.")
                            } else {
                                context.source.sendMessage(buildString {
                                    getSmartUsage(parseResults.context.nodes.last().node, context.source)
                                        .forEach {
                                            append("/" + parseResults.reader.string + it)
                                            context.source
                                        }
                                })

                                0
                            }
                        })
                )
                register(LiteralArgumentBuilder.literal<CommandSource?>("echo")
                    .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("text",
                        StringArgumentType.greedyString())
                        .executes {
                            it.source.sendMessage(StringArgumentType.getString(it, "text"))
                            0
                        }
                    )
                )
                register(LiteralArgumentBuilder.literal<CommandSource?>("stop")
                    .requires { it.hasPermission("kooky.owner") }
                    .executes {
                        this@Client.close()
                        GlobalScope.coroutineContext.cancel()
                        exitProcess(0)
                    }
                )
                register(LiteralArgumentBuilder.literal<CommandSource?>("setowner")
                    .requires { it.hasPermission("kooky.owner") }
                    .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("uid", UserArgumentType.id())
                        .executes {
                            permissionManager.setPermission(
                                perm = "kooky.owner",
                                user = UserArgumentType.getId(it, "uid"),
                                value = true
                            )
                            it.source.sendMessage("Set owner")
                            0
                        }
                    )
                )
                register(LiteralArgumentBuilder.literal<CommandSource?>("op")
                    .requires { it.hasPermission("kooky.operator") }
                    .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("scope",
                        StringArgumentType.word())
                        .then(RequiredArgumentBuilder.argument<CommandSource?, String?>("name",
                            UserArgumentType.id())
                            .executes {
                                val name = UserArgumentType.getId(it, "name")
                                when (StringArgumentType.getString(it, "scope")) {
                                    "global" -> permissionManager.setPermission(
                                        perm = "kooky.operator",
                                        user = name,
                                        value = true
                                    )
                                    "channel" -> permissionManager.setPermission(
                                        perm = "kooky.operator",
                                        user = name,
                                        channelId = it.source.channel!!.id,
                                        value = true
                                    )
                                    "guild" -> permissionManager.setPermission(
                                        perm = "kooky.operator",
                                        user = name,
                                        guildId = it.source.channel!!.guild.id,
                                        value = true
                                    )
                                }
                                it.source.sendMessage("Oped")
                                0
                            }
                        )
                    )
                )
                register(LiteralArgumentBuilder.literal<CommandSource?>("permission")
                    .then(LiteralArgumentBuilder.literal<CommandSource?>("me")
                        .executes {
                            if (it.source.type == CommandSource.Type.Console) {
                                it.source.sendMessage("Console has all permissions.")
                                return@executes 0
                            }
                            val pm = it.source.user!!.client.permissionManager
                            it.source.sendMessage(
                                buildString {
                                    append("global:\n")
                                    pm.global[it.source.user!!.id]?.forEach {
                                        append("  ${it.key} = ${it.value}\n")
                                    }
                                    if (it.source.channel != null) {
                                        append("this guild:\n")
                                        pm.guild[it.source.user!!.id]?.get(it.source.channel!!.guild.id)?.forEach {
                                            append("  ${it.key} = ${it.value}\n")
                                        }
                                        append("this channel:\n")
                                        pm.channel[it.source.user!!.id]?.get(it.source.channel!!.id)?.forEach {
                                            append("  ${it.key} = ${it.value}\n")
                                        }
                                    }
                                }
                            )
                            0
                        }
                    )
                    .then(LiteralArgumentBuilder.literal<CommandSource?>("list")
                        .requires { it.hasPermission("kooky.operator") }
                        .executes {
                            it.source.sendMessage(permissionManager.printAll())
                            0
                        }
                    )
                )
                register(LiteralArgumentBuilder.literal<CommandSource?>("ping")
                    .executes {
                        it.source.sendMessage(
                            "pong, delay is ${Calendar.getInstance().timeInMillis - it.source.timestamp}ms"
                        )
                        0
                    }
                )
            }
        }
    }

    /**
     * Note: this field is for interval updating.
     */
    private val updatableList: MutableList<Updatable> = mutableListOf()
    private var updateJob: Job? = null

    class ConfigureScope(internal val client: Client) {
        var enableCommand = true
        var enablePermission = true
        var botMarketUUID = ""
        var responseCommandExceptions = true
        var enableEasterEggs = false
        internal var commandPrefix = listOf("/")
        internal var dataFolder = File("data/")
        internal var defaultCommand = false

        fun withDataFolder(file: File) {
            dataFolder = file
        }

        fun withDefaultCommands() {
            defaultCommand = true
        }

        fun withOwner(id: String) {
            client.run {
                if (!permissionManager.hasPermission("kooky.owner", id)) {
                    permissionManager.setPermission(
                        perm = "kooky.owner",
                        user = id,
                        value = true
                    )
                }
            }
        }
    }

    private var pingStart = Calendar.getInstance().timeInMillis
    private var pingThread = Thread {
        while (status != State.FatalError &&
            status != State.Closed
        ) {
            Thread.sleep(30000)
            try {
                if (status == State.Initialized) continue
                if (resumeStatus != None) continue
                logger.debug("ping")
                pingStatus = PingState.Pinging
                webSocketClient?.send("{\"s\":2,\"sn\":$lastSn}")
                pingStart = Calendar.getInstance().timeInMillis
                while (pingStatus == PingState.Pinging && Calendar.getInstance().timeInMillis < pingStart + 6000) {
                }
                if (Calendar.getInstance().timeInMillis >= pingStart + 6000) {
                    logger.error("ping timeout")
                    pingStatus = PingState.Timeout
                    resumeStatus = First
                    status = State.Disconnected
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * 警告：
     *
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
        ADD_REACTION,
        CANCEL_REACTION,
        ACTIVITY,
        CHANNEL_ROLE_INDEX,
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

    fun contentTypeOf(ext: String): String {
        return when (ext.lowercase()) {
            "mp3" -> "audio/mp3"
            "mp4" -> "video/mpeg4"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "jpg" -> "image/jpeg"
            "svg" -> "text/xml"
            //TODO: 更多的类型
            else -> throw Exception("unsupported file type, please go to <https://github.com/KookyBot/KookyBot> to raise an issue.")
        }
    }

    fun requestBuilder(requestType: RequestType, values: Map<String, Any?>? = null): HttpRequest {
        val builder = HttpRequest.newBuilder().header("Authorization", "Bot $token")
        when (requestType) {
            GATEWAY -> builder.uri(apiOf("/gateway/index")).GET()
            GATEWAY_RESUME -> builder.uri(apiOf("/gateway/index?sn=$lastSn&resume=1&session_id=$sessionId")).GET()
            GUILD_LIST -> builder.uri(apiOf("/guild/list")).GET()
            GUILD_VIEW -> builder.uri(apiOf("/guild/view?guild_id=${values!!["guild_id"]}")).GET()
            USER_VIEW -> builder.uri(apiOf("/user/view?user_id=${values!!["user_id"]}&guild_id=${values["guild_id"] ?: ""}"))
                .GET()
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
                .run {
                    val boundary = UUID.randomUUID().toString().replace("-", "kooky")
                    header("content-type", "multipart/form-data; boundary=$boundary")
                    POST(BodyPublishers.ofByteArray(
                        "--$boundary\n".toByteArray(Charsets.US_ASCII) +
                                "Content-Disposition: form-data; name=\"file\"; filename=\"${values!!["filename"] as String}\"\n".toByteArray(
                                    Charsets.US_ASCII) +
                                "Content-Type: ${contentTypeOf(values["ext"]!! as String)}\n\n".toByteArray(Charsets.US_ASCII) +
                                values["file"]!! as ByteArray +
                                "\n--$boundary--".toByteArray(Charsets.US_ASCII)
                    ))
                }
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
            ADD_REACTION -> builder.uri(apiOf("/message/add-reaction"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            CANCEL_REACTION -> builder.uri(apiOf("/message/delete-reaction"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            ACTIVITY -> builder.uri(apiOf("/game/activity"))
                .header("content-type", "application/json")
                .POST(postAll(values!!))
            CHANNEL_ROLE_INDEX -> builder.uri(apiOf("/channel-role/index?channel_id=${values!!["channel_id"]}")).GET()
        }
        return builder.build()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sendRequest(request: HttpRequest): JsonObject {
        val response = httpClient.send(request, BodyHandlers.ofString())
        logger.debug(response.body())
        val json = Gson().fromJson(response.body(), JsonObject::class.java)
        when (json.get("code").asInt) {
            0 -> {
                return let {
                    if (json.has("items") && json.get("items").isJsonArray && !request.uri().query.contains("page")) {
                        val meta = json.get("meta").asJsonObject
                        val total = meta.get("page_total").asInt
                        if (total != 1) {
                            for (i in (1 until total)) {
                                var url = request.uri().toString()
                                url +=
                                    if (url.contains('?')) "&page=$i"
                                    else "?page=$i"
                                val req = HttpRequest.newBuilder(URI(url))
                                    .method(request.method(), request.bodyPublisher().getOrNull())
                                for (header in request.headers().map()) {
                                    req.header(header.key, header.value[0])
                                    // TODO
                                }
                                sendRequest(req.build())
                            }
                        }
                    }
                    if (json.has("data") && json.get("data").isJsonArray && json["data"].asJsonArray.size() == 0) {
                        json.add("data", JsonObject())// 官方bug，特殊处理
                    }
                    return@let json["data"].asJsonObject
                }
            }
            else -> {
                throw KookRemoteException(json.get("code").asInt, json.get("message").asString, request)
            }
        }
    }

    private fun initWebsocket(url: String): WebSocketClient {
        return object : WebSocketClient(URI(url), mapOf("Authorization" to "Bot $token")) {
            val id = UUID.randomUUID().toString()
            override fun onOpen(handshakedata: ServerHandshake) {
                logger.debug("websocket opened: http status=${handshakedata.httpStatus}, id=$id")

            }

            override fun onMessage(bytes: ByteBuffer?) {
                if (bytes == null) return
                onMessage(
                    InflaterInputStream(bytes.array().inputStream()).bufferedReader().readText()
                )
            }

            override fun onMessage(message: String) {
                logger.debug("[Event received] $message")
                var json = Gson().fromJson(message, JsonObject::class.java)
                resumeStatus = None
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
                            40103 -> resumeStatus = Reconnect
                            40107 -> resumeStatus = Reconnect
                            40108 -> resumeStatus = Reconnect
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
                        resumeStatus = Reconnect
                        closeBlocking()
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
                if (webSocketClient !== this) return
                if (code == 1002) {
                    logger.error("invalid frame, closed.")
                    status = State.Disconnected
                    resumeStatus = Reconnect
                    return
                }
                logger.info("websocket $id closed, code=$code, reason=$reason")
                if (status == State.Closed) return
                if (resumeStatus != None) return
                status = State.Disconnected
                resumeStatus = First
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
                webSocketClient = initWebsocket(url)
                status = State.Connecting
                if ((webSocketClient as WebSocketClient).connectBlocking(6000, TimeUnit.MILLISECONDS)) {
                    logger.info("websocket connected!")
                    while (status != State.Connected || self == null) {
                        delay(100)
                    }
                    return self!!
                }
            } catch (e: IOException) {
                status = State.FatalError
                logger.error("websocket connect failed, reconnecting in 5 seconds...", e)
            }
            delay(5000)
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
        val user = User(status = when (jsonObject.get("status").asInt) {
            10 -> UserState.BANNED
            else -> UserState.NORMAL
        },
            client = this,
            id = jsonObject["id"].asString
        )
        user.updateByJson(jsonObject)
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
        val ret = sendRequest(requestBuilder(
            SEND_CHANNEL_MESSAGE,
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
        if (self!!.chattingUsers[target.id] == null) {
            sendRequest(requestBuilder(CREATE_CHAT, "target_id" to target.id))
            self!!.updatePrivateChatUser(target.id)
        }
        val ret = sendRequest(requestBuilder(
            SEND_PRIVATE_MESSAGE,
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
        return sendRequest(requestBuilder(CREATE_ASSET,
            "file" to data,
            "ext" to file.extension,
            "filename" to file.name)).get("url").asString
    }

    fun close() {
        sendRequest(requestBuilder(OFFLINE))
        status = State.Closed
        webSocketClient?.closeBlocking()
        pingThread.interrupt()
        updateJob?.cancel()
    }

    fun addCommand(listener: (CommandDispatcher<CommandSource>) -> Unit) {
        listener(eventManager.dispatcher)
    }

    val reconnectThread = Thread {
        while (status != State.FatalError &&
            status != State.Closed
        ) {
            Thread.sleep(1000)
            if (resumeStatus == None) continue
            fun resume(): Boolean {
                try {
                    sendRequest(requestBuilder(OFFLINE))
                    webSocketClient = initWebsocket(sendRequest(requestBuilder(GATEWAY_RESUME))["url"].asString)
                    webSocketClient!!.connectBlocking(6000, TimeUnit.MILLISECONDS)
                    webSocketClient?.send("{\"s\":4,\"sn\":$lastSn}")
                    if (webSocketClient?.isOpen == true) {
                        resumeStatus = None
                        pingStatus = PingState.Success
                        status = State.Connected
                        return true
                    } else return false
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            }
            when (resumeStatus) {
                First -> {
                    logger.error("resume - first")
                    if (!resume())
                        resumeStatus = Second
                }
                Second -> {
                    logger.error("resume - second")
                    if (!resume())
                        resumeStatus = Reconnect
                }
                Reconnect -> {
                    while (true) {
                        try {
                            logger.error("reconnect")
                            if (webSocketClient?.isClosed != true)
                                webSocketClient?.close()
                            status = State.Connecting
                            sendRequest(requestBuilder(OFFLINE))
                            webSocketClient = initWebsocket(sendRequest(requestBuilder(GATEWAY)).get("url").asString)
                            webSocketClient!!.connectBlocking(6000, TimeUnit.MILLISECONDS)
                            if (webSocketClient?.isOpen != true) {
                                status = State.Disconnected
                                resumeStatus = Reconnect
                                webSocketClient?.closeBlocking()
                            }
                            status = State.Connected
                            resumeStatus = None
                            pingStatus = PingState.Success
                            lastSn = 0
                            break
                        } catch (e: Exception) {
                            logger.error("Reconnecting after 5 seconds.", e)
                            Thread.sleep(5000)
                        }
                    }
                }
                None -> {}
            }
        }
    }

    init {
        pingThread.start()
        reconnectThread.start()
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            if (config.enableEasterEggs) {
                CrazyThursdayMoneyNotEnoughException().printStackTrace()
            }
        }
    }
}
