package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.utils.Cache

class GuildUser(
    id: String,
    oline: Boolean,
    name: String,
    val nickname: String,
    identifyNumber: String,
    status: UserState,
    bot: Boolean,
    mobilePhoneVerified: Boolean,
    avatarUrl: String,
    vipAvatarUrl: String,
    isVip: Boolean,
    @Transient
    val roles: Cache<List<Cache<GuildRole>>>,
    joinTime: Int,
    activeTime: Int,
) : User(id,
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
    activeTime) {
}