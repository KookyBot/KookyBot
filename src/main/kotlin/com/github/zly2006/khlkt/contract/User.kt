package com.github.zly2006.khlkt.contract

/**
 * 警告：
 * 此类型不会自动更新。
 * 每次使用都应该重新获取一遍，如
 *
 * @see com.github.zly2006.khlkt.contract.Self#getUser(String kotlin.userId)
 */
open class User(
    val id: Int,
    val oline: Boolean,
    val name: String,
    val identifyNumber: Int,
    val status: UserState,
    val bot: Boolean,
    val mobilePhoneVerified: Boolean,
    val avatarUrl: String,
    val vipAvatarUrl: String,
    /**
     * 不要使用，正在试图修复兼容性问题
     */
    val isVip: Boolean
) {

}
