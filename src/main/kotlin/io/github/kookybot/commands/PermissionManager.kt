package io.github.kookybot.commands

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.kookybot.client.Client
import java.io.File

class PermissionTreeNode(
    val name: String,
    val parent: PermissionTreeNode? = null,
    val children: MutableList<PermissionTreeNode> = mutableListOf()
) {
    val root
        get() = let {
            var cur = this
            while (cur.parent != null) cur = cur.parent!!
            return@let cur
        }

    fun addChild(name: String): PermissionTreeNode {
        val node = PermissionTreeNode(name = name, parent = this)
        children.add(node)
        return node
    }

    fun printChildren(indent: Int): String {
        val prefix = (1..indent).map { ' ' }.joinToString("");
        return buildString {
            append(prefix)
            append(name)
            append("\n")
            for (child in children) {
                append(child.printChildren(indent + 1))
            }
        }
    }
}

class PermissionManager(
    val client: Client
) {
    private val root = PermissionTreeNode("root")
    private val nodes = mutableMapOf("root" to root)
    var configFile: File = File("data/perm.json")
    var json: JsonObject

    fun printAll(node: PermissionTreeNode = root): String {
        return node.printChildren(0)
    }

    fun searchNode(perm: String): PermissionTreeNode? = nodes[perm]

    init {
        if (configFile.isFile) {
            json = Gson().fromJson(configFile.readText(), JsonObject::class.java)
        } else {
            json = JsonObject()
        }
        if (!json.has("global")) json.add("global", JsonObject())
        if (!json.has("guild")) json.add("guild", JsonObject())
        if (!json.has("channel")) json.add("channel", JsonObject())
        configFile.writeText(json.toString())
    }

    /**
     * map(id, map(perm, bool))
     */
    var global: MutableMap<String, MutableMap<String, Boolean>> = let {
        json.get("global").asJsonObject.entrySet().associate {
            it.key to it.value.asJsonObject.entrySet().associate {
                it.key to it.value.asBoolean
            }.toMutableMap()
        }.toMutableMap()
    }

    /**
     * map(id, map(guild, map(perm, bool)))
     */
    var guild: MutableMap<String, MutableMap<String, MutableMap<String, Boolean>>> = let {
        json.get("guild").asJsonObject.entrySet().associate {
            it.key to it.value.asJsonObject.entrySet().associate {
                it.key to it.value.asJsonObject.entrySet().associate {
                    it.key to it.value.asBoolean
                }.toMutableMap()
            }.toMutableMap()
        }.toMutableMap()
    }

    /**
     * map(id, map(channel, map(perm, bool)))
     */
    var channel: MutableMap<String, MutableMap<String, MutableMap<String, Boolean>>> = let {
        json.get("channel").asJsonObject.entrySet().associate {
            it.key to it.value.asJsonObject.entrySet().associate {
                it.key to it.value.asJsonObject.entrySet().associate {
                    it.key to it.value.asBoolean
                }.toMutableMap()
            }.toMutableMap()
        }.toMutableMap()
    }

    fun hasPermission(perm: String, user: String, guildId: String? = null, channelId: String? = null): Boolean {
        fun check(perm: String): Boolean? {
            with(channel[user]?.get(channelId)?.get(perm)) {
                if (this != null) return this
            }
            with(guild[user]?.get(guildId)?.get(perm)) {
                if (this != null) return this
            }
            with(global[user]?.get(perm)) {
                if (this != null) return this
            }
            return null
        }
        if (searchNode(perm) == null) throw Exception("Permission not found.")
        var cur = searchNode(perm)!!
        while (cur.parent != null) {
            with(check(cur.name)) {
                if (this != null) return this
            }
            cur = cur.parent!!
        }
        return false
    }

    fun setPermission(perm: String, user: String, guildId: String? = null, channelId: String? = null, value: Boolean?) {
        if (searchNode(perm) == null) throw Exception("Permission not found.")
        if (guildId != null) {
            if (!guild.containsKey(user)) {
                guild[user] = mutableMapOf()
                if (!guild[user]!!.containsKey(guildId)) {
                    guild[user]!![guildId] = mutableMapOf()
                    if (value == null) {
                        guild[user]!![guildId]!!.remove(perm)
                    } else guild[user]!![guildId]!![perm] = value
                }
            }
        } else if (channelId != null) {
            if (!channel.containsKey(user)) {
                channel[user] = mutableMapOf()
                if (!channel[user]!!.containsKey(channelId)) {
                    channel[user]!![channelId] = mutableMapOf()
                    if (value == null) {
                        channel[user]!![channelId]!!.remove(perm)
                    } else channel[user]!![channelId]!![perm] = value
                }
            }
        } else {
            if (!global.containsKey(user)) {
                global[user] = mutableMapOf()
                if (value == null) {
                    global[user]!!.remove(perm)
                } else global[user]!![perm] = value
            }
        }
        save()
    }

    fun save() {
        json = Gson().toJsonTree(mapOf(
            "global" to global,
            "channel" to channel,
            "guild" to guild
        )) as JsonObject
        configFile.writeText(json.toString())
    }

    init {
        val owner = root.addChild("kooky.owner")
        val operator = owner.addChild("kooky.operator")
        nodes["kooky.owner"] = owner
        nodes["kooky.operator"] = operator
    }
}