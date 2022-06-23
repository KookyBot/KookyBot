package com.github.zly2006.khlkt.contract

class PrivateChatUser(
    name: String,
    identifyNumber: Int,
    status: UserState,
    bot: Boolean,
    mobilePhoneVerified: Boolean,
    avatarUrl: String,
    vipAvatarUrl: String,
    isVip: Boolean,
    id: Int,
    oline: Boolean
): User(id, oline, name, identifyNumber, status, bot, mobilePhoneVerified, avatarUrl, vipAvatarUrl, isVip) {

}
