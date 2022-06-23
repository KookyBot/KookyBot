package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.client.State
import com.github.zly2006.khlkt.utils.Cache

data class Guild(
    val client: Client,
    val id: String,
    val masterId: Int,
    var name: Cache<String> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var topic: Cache<String> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var iconUrl: Cache<String> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var notifyType: Cache<NotifyType> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var region: Cache<String> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var defaultChannel: Cache<Channel> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var welcomeChannel: Cache<Channel> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    /**
     * 公开邀请链接id，为null说明不公开
     */
    var openId: Cache<Int?> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var owner: Cache<User> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var level: Cache<Int> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var boostCount: Cache<Int> = Cache(uploader = {

    }, updater = {
        return@Cache null
    }),
    var channels: Cache<List<Channel>> = Cache(uploader = {

    }, updater = {
        var channels: List<Channel> = listOf()
        with(client) {
            if (status != State.Connected) {
                return@Cache null
            }
            var jObj = sendRequest(requestBuilder(Client.RequestType.GUILD_VIEW, mapOf("guild_id" to id)))

        }
        return@Cache channels
    })
)
