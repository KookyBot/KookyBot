package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.message.CardMessage
import com.github.zly2006.khlkt.message.MarkdownMessage
import com.github.zly2006.khlkt.message.Message
import com.github.zly2006.khlkt.utils.Cache
import com.google.gson.annotations.SerializedName

/*
topic	string	频道简介
is_category	boolean	是否为分组，事件中为 int 格式
parent_id	string	上级分组的 id
level	int	排序 level
slow_mode	int	慢速模式下限制发言的最短时间间隔, 单位为秒(s)
type	int	频道类型: 1 文字频道, 2 语音频道
permission_overwrites	Array	针对角色在该频道的权限覆写规则组成的列表
permission_users	array	针对用户在该频道的权限覆写规则组成的列表
permission_sync	int	权限设置是否与分组同步, 1 or 0
has_password	bool	是否有密码
 */
class Channel(
    @Transient
    var client: Client,
    override val id: String,
    val name: String,
    @SerializedName("parent_id")
    var parentId: String,
    var topic: String,
    @SerializedName("permission_sync")
    val permissionSync: Int,
    @Transient
    var guild: Cache<Guild>,
    @Transient//TODO
    var category: String?,
    val level: Int,
    @SerializedName("slow_mode")
    var slowMode: Int,
    @SerializedName("type")
    private val _type: Int,
    @Transient//TODO
    var permissionOverwrites: List<RolePermissionOverwrite>,
    @Transient//TODO
    var permissionUsers: List<UserPermissionOverwrite>
) : MessageReceiver() {
    @Transient
    val type: ChannelType = when(_type) {
        1 -> ChannelType.TEXT
        2 -> ChannelType.VOICE
        else -> ChannelType.UNKNOWN
    }

    data class RolePermissionOverwrite(
        val role: Cache<GuildRole>,
        var value: Boolean?
    )

    data class UserPermissionOverwrite(
        val role: Cache<User>,
        var value: Boolean?
    )


    override fun sendMessage(message: Message) {
        if (client != message.client) return
        if (message is MarkdownMessage) {
            client.sendChannelMessage(
                type = 9,
                content = message.content(),
                target = this
            )
        }
        if (message is CardMessage) {
            client.sendChannelMessage(
                type = 10,
                content = message.content(),
                target = this
            )
        }
    }
    fun sendMessage(message: String) {
        client.sendChannelMessage(
            content = message,
            target = this
        )
    }

    fun sendMarkdownMessage(message: String) {
        sendMessage(MarkdownMessage(client, message, this))
    }

    fun sendCardMessage(content: CardMessage.MessageScope.() -> Unit) {
        var msg = CardMessage(client = client,
            primaryReceiver = this,
            contentBuilder = content)
        sendMessage(msg)
    }
}