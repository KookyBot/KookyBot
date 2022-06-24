package com.github.zly2006.khlkt.events

import com.google.gson.annotations.SerializedName

open class Event(
    @SerializedName("channel_type")
    private var _channelType: String,
    @SerializedName("type")
    private var _type: Int,
    @SerializedName("target_id")
    var targetId: String,
    @SerializedName("author_id")
    var authorId: String,
    @SerializedName("content")
    var content: String,
    @SerializedName("message_id")
    var sid: String,
    @SerializedName("msg_timestamp")
    var timestamp: String
) {
    enum class EventType(i: Int) {
        UNKNOWN(0),
        PLAIN_TEXT(1),
        IMAGE(2),
        VIDEO(3),
        FILE(4),
        VOICE(8),
        MARKDOWN(9),
        CARD(10),
        SYSTEM(255)
    }
    enum class ChannelType(t: String) {
        GROUP("GROUP"),
        BROADCAST("BROADCAST"),
        PERSON("PERSON"),
        UNKNOWN("?")
    }
    val eventType get() = when (_type) {
        1 -> EventType.PLAIN_TEXT
        2 -> EventType.IMAGE
        3 -> EventType.VIDEO
        4 -> EventType.FILE
        8 -> EventType.VOICE
        9 -> EventType.MARKDOWN
        10 -> EventType.CARD
        255 -> EventType.SYSTEM
        else -> EventType.UNKNOWN
    }
    val channelType get() = when (_channelType) {
        "GROUP" -> ChannelType.GROUP
        "BROADCAST" -> ChannelType.BROADCAST
        "PERSON" -> ChannelType.PERSON
        else -> ChannelType.UNKNOWN
    }
}