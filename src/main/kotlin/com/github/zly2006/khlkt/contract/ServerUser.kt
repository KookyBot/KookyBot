package com.github.zly2006.khlkt.contract

class ServerUser(
    id: Int,
    oline: Boolean,
    name: String,
    identifyNumber: Int,
    status: UserState,
    bot: Boolean,
    mobilePhoneVerified: Boolean,
    avatarUrl: String,
    vipAvatarUrl: String,
    isVip: Boolean
) : User(id, oline, name, identifyNumber, status, bot, mobilePhoneVerified, avatarUrl, vipAvatarUrl, isVip) {
}