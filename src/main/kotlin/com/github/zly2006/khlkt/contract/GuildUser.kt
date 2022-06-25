package com.github.zly2006.khlkt.contract

import com.github.zly2006.khlkt.utils.Updatable

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
    @field:Transient
    val roles: List<GuildRole>,
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
    activeTime), Updatable {
}