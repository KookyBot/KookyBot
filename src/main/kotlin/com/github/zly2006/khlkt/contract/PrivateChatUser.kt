package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.utils.Updatable

class PrivateChatUser(
    name: String,
    identifyNumber: String,
    status: UserState,
    bot: Boolean,
    mobilePhoneVerified: Boolean,
    avatarUrl: String,
    vipAvatarUrl: String,
    isVip: Boolean,
    id: String,
    oline: Boolean,
    joinTime: Int,
    activeTime: Int
): User(id,
    oline,
    name,
    identifyNumber,
    status,
    bot,
    mobilePhoneVerified,
    avatarUrl,
    vipAvatarUrl,
    isVip,
    joinTime,
    activeTime), Updatable {
    override fun update() {
        TODO("Not yet implemented")
    }
}
