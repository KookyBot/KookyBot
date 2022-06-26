/* KhlKt - a SDK of <https://kaiheila.cn> for JVM platform
Copyright (C) <year>  <name of author>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package com.github.zly2006.khlkt.utils

class PermissionImpl(override var num: Long): Permission {
    enum class Permissions: Permission {
        None {
            override val num: Long
                get() = 0
        },
        Admin {
            override val num: Long
                get() = 1
        },
        ManageGuild {
            override val num: Long
                get() = 2
        },
        ViewLog {
            override val num: Long
                get() = 4
        },
        CreateInvitation {
            override val num: Long
                get() = 8
        },
        ManageInvitation {
            override val num: Long
                get() = 16
        },
        ManageChannel {
            override val num: Long
                get() = 32
        },
        KickUser {
            override val num: Long
                get() = 64
        },
        BanUser {
            override val num: Long
                get() = 128
        },
        ManageGuildEmoji {
            override val num: Long
                get() = 256
        },
        EditGuildNick {
            override val num: Long
                get() = 512
        },
        ManageGuildRole {
            override val num: Long
                get() = 1024
        },
        ViewTextAndVoiceChannel {
            override val num: Long
                get() = 2048
        },
        SendMessage {
            override val num: Long
                get() = 4096
        },
        ManageMessage {
            override val num: Long
                get() = 8192
        },
        UploadFile {
            override val num: Long
                get() = 16384
        },
        ConnectVoiceChannel {
            override val num: Long
                get() = 32768
        },
        ManageVoiceChannel {
            override val num: Long
                get() = 65536
        },
        AtAll {
            override val num: Long
                get() = 131072
        },
        PostReaction {
            override val num: Long
                get() = 262144
        },
        PutReaction {
            override val num: Long
                get() = 524288
        },
        PassivelyJoinVoiceChannel {
            override val num: Long
                get() = 1048576
        },
        PressSpeakOnly {
            override val num: Long
                get() = 2097152
        },
        FreeSpeak {
            override val num: Long
                get() = 4194304
        },
        Speak {
            override val num: Long
                get() = 8388608
        },
        MuteServer {
            override val num: Long
                get() = 16777216
        },
        MicrophoneOff {
            override val num: Long
                get() = 16777216
        },
        EditOthersNick {
            override val num: Long
                get() = 67108864
        },
        PlayAccompaniment {
            override val num: Long
                get() = 134217728
        }
    }

    fun get(perm: PermissionImpl): Boolean {
        return get(perm.num)
    }

    fun set(perm: PermissionImpl, value: Boolean) {
        set(perm.num, value)
    }

    fun get(perm: Long): Boolean {
        return num.and(perm) == perm
    }
    fun set(perm: Long, value: Boolean) {
        if (value) {
            num = num.or(perm)
        }
        else {
            num = num.and(perm.inv())
        }
    }
}