package io.github.kookybot.contract

import com.google.gson.JsonObject
import io.github.kookybot.utils.DontUpdate
import io.github.kookybot.utils.Permission
import io.github.kookybot.utils.PermissionImpl

open class PermissionOverwritten protected constructor(

) {
    @field:DontUpdate
    var rolePermissionOverwrites: List<RolePermissionOverwrite> = listOf()
        internal set

    fun updateByJson(perm: JsonObject, guild: Guild) {
        rolePermissionOverwrites = perm["permission_overwrites"].asJsonArray.map { it.asJsonObject }.map {
            RolePermissionOverwrite(guild.roleMap[it["role_id"].asInt]!!).run {
                deny = PermissionImpl(it["deny"].asLong)
                allow = PermissionImpl(it["allow"].asLong)
                this
            }
        }
        userPermissionOverwrites = perm["permission_users"].asJsonArray.map { it.asJsonObject }.map {
            UserPermissionOverwrite(guild.getGuildUser(it["user"].asJsonObject["id"].asString)!!).run {
                deny = PermissionImpl(it["deny"].asLong)
                allow = PermissionImpl(it["allow"].asLong)
                this
            }
        }
    }

    class RolePermissionOverwrite(
        val role: GuildRole,
    ) {
        var allow: Permission = PermissionImpl.Permissions.None
            internal set
        var deny: Permission = PermissionImpl.Permissions.None
            internal set
    }

    @field:DontUpdate
    var userPermissionOverwrites: List<UserPermissionOverwrite> = listOf()
        internal set

    class UserPermissionOverwrite(
        val user: GuildUser,
    ) {
        var allow: Permission = PermissionImpl.Permissions.None
            internal set
        var deny: Permission = PermissionImpl.Permissions.None
            internal set
    }
}