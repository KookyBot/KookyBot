package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.message.Message
import com.google.gson.annotations.SerializedName

/**
 * 警告：
 * 此类型不会自动更新。
 * 每次使用都应该重新获取一遍，如
 *
 * @see com.github.zly2006.khlkt.contract.Self#getUser(String kotlin.userId)
 */
open class User(
    override val id: String,
    val oline: Boolean,
    val name: String,
    @SerializedName("identify_num")
    val identifyNumber: String,
    @Transient
    var status: UserState,
    val bot: Boolean,
    @SerializedName("mobile_verified")
    val mobilePhoneVerified: Boolean,
    @SerializedName("avatar")
    val avatarUrl: String,
    @SerializedName("vip_avatar")
    val vipAvatarUrl: String,
    /**
     * 不要使用，正在试图修复兼容性问题
     */
    @SerializedName("is_vip")
    val isVip: Boolean,
    @SerializedName("joined_at")
    val joinTime: Int,
    @SerializedName("active_time")
    val activeTime: Int,
): MessageReceiver() {
    override fun sendMessage(message: Message) {
        TODO("Not yet implemented")
    }
}
