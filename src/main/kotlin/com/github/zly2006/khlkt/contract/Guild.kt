package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.utils.Cache
import com.google.gson.annotations.SerializedName

data class Guild(
    @Transient
    val client: Client,
    @SerializedName("id")
    val id: String,
    @SerializedName("master_id")
    val masterId: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("topic")
    var topic: String,
    @SerializedName("icon")
    var iconUrl: String,
    @SerializedName("notify_type")
    var notifyType: NotifyType,
    @SerializedName("region")
    var region: String,
    @Transient
    var defaultChannel: Cache<Channel>?,
    @Transient
    var welcomeChannel: Cache<Channel>?,
    @SerializedName("open_id")
    /**
     * 公开邀请链接id，为null说明不公开
     */
    var openId: Int?,
    @SerializedName("level")
    var level: Int,
    @SerializedName("boost_num")
    var boostCount: Int,
    @Transient
    var channels: List<Cache<Channel>>,
    @Transient
    var user: Cache<List<Cache<GuildUser>>>,
    @Transient
    var roleMap: Cache<Map<Int, GuildRole>>,
) {
    val owner: User get() = client.getUser(masterId)
    val botPermission: Int get() {
        return 0;
        //TODO
    }
}
