@file:Suppress("FunctionName")

package com.github.zly2006.khlkt.message

import com.github.zly2006.khlkt.client.Client
import com.github.zly2006.khlkt.contract.MessageReceiver
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class CardMessage(client: Client, primaryReceiver: MessageReceiver, contentBuilder: MessageScope.() -> Unit) : Message(
    client,
    primaryReceiver) {

    enum class Size{
        XS, SM, MD, LG
    }
    enum class Theme{
        Primary, Success, Danger, Warning, Info, Secondary, None
    }
    enum class LeftRight{
        Left, Right
    }
    enum class ClickType{
        None,ReturnValue,Link
    }
    abstract class CardComponent {
        abstract fun toJson(): JsonElement
        open fun add(component: CardComponent) {}
    }
    inner class MessageScope {
        fun anElement(content: CardMessage.ModuleScope.() -> Unit): CardComponent {
            val root = object : CardComponent() {
                var com: CardComponent? = null
                override fun add(component: CardComponent) {
                    com = component
                }
                override fun toJson(): JsonElement {
                    return JsonObject()
                }
            }
            ModuleScope(root).content()
            return root.com ?: throw Exception("no member")
        }
        fun Card (
            /**
             * SM | LG
             */
            size: Size = Size.LG,
            theme: Theme = Theme.Primary,
            color: String = "#aaaaaa",
            content: CardScope.() -> Unit
        ) {
            val card = object : CardComponent() {
                var arr: MutableList<CardComponent> = mutableListOf()
                override fun add(component: CardComponent) {
                    arr.add(component)
                }
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("theme", theme.name.lowercase())
                    obj.addProperty("size", size.name.lowercase())
                    obj.addProperty("type", "card")
                    obj.addProperty("color", color)
                    val a = JsonArray()
                    arr.forEach { a.add(it.toJson()) }
                    obj.add("modules", a)
                    return obj
                }
            }
            CardScope(card).content()
            root.add(card)
        }
    }
    inner class CardScope(private val component: CardComponent) {
        fun HeaderModule(
            text: CardComponent
        ) {
            val obj = object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "header")
                    obj.add("text", text.toJson())
                    return obj
                }
            }
            component.add(obj)
        }
        fun SectionModule(
            text: CardComponent,
            accessory: CardComponent,
            mode: LeftRight = LeftRight.Right
        ) {
            val obj = object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "section")
                    obj.add("text", text.toJson())
                    obj.add("accessory", accessory.toJson())
                    obj.addProperty("mode", mode.name.lowercase())
                    return obj
                }
            }
            component.add(obj)
        }
        fun ImageGroupModule(
            content: ModuleScope.() -> Unit
        ) {
            val module = object : CardComponent() {
                var arr: MutableList<CardComponent> = mutableListOf()
                override fun add(component: CardComponent) {
                    arr.add(component)
                }
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "image-group")
                    val a = JsonArray()
                    arr.forEach { a.add(it.toJson()) }
                    obj.add("elements", a)
                    return obj
                }
            }
            ModuleScope(module).content()
            component.add(module)
        }
        fun ContainerModule(
            content: ModuleScope.() -> Unit
        ) {
            val module = object : CardComponent() {
                var arr: MutableList<CardComponent> = mutableListOf()
                override fun add(component: CardComponent) {
                    arr.add(component)
                }
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "container")
                    val a = JsonArray()
                    arr.forEach { a.add(it.toJson()) }
                    obj.add("elements", a)
                    return obj
                }
            }
            ModuleScope(module).content()
            component.add(module)
        }
        fun ActionGroupModule(
            content: ModuleScope.() -> Unit
        ) {
            val module = object : CardComponent() {
                var arr: MutableList<CardComponent> = mutableListOf()
                override fun add(component: CardComponent) {
                    arr.add(component)
                }
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "action-group")
                    val a = JsonArray()
                    arr.forEach { a.add(it.toJson()) }
                    obj.add("elements", a)
                    return obj
                }
            }
            ModuleScope(module).content()
            component.add(module)
        }
        fun ContextModule(
            content: ModuleScope.() -> Unit
        ) {
            val module = object : CardComponent() {
                var arr: MutableList<CardComponent> = mutableListOf()
                override fun add(component: CardComponent) {
                    arr.add(component)
                }
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "context")
                    val a = JsonArray()
                    arr.forEach { a.add(it.toJson()) }
                    obj.add("elements", a)
                    return obj
                }
            }
            ModuleScope(module).content()
            component.add(module)
        }
        fun FileModule(
            src: String,
            title: String,
            cover: String? = null,
            type: String
        ) {
            component.add(object : CardComponent(){
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("src", src)
                    obj.addProperty("type", type)
                    obj.addProperty("cover", cover)
                    obj.addProperty("title", title)
                    return obj
                }
            })
        }
        fun InviteModule(
            code: String
        ) {
            component.add(object : CardComponent(){
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("code", code)
                    obj.addProperty("type", "invite")
                    return obj
                }
            })
        }
        fun CountdownModule(
            mode: String,
            endTime: Long,
            startTime: Long,
        ) {
            component.add(object : CardComponent(){
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("mode", mode)
                    obj.addProperty("endTime", endTime)
                    obj.addProperty("startTime", startTime)
                    obj.addProperty("type", "countdown")
                    return obj
                }
            })
        }
        fun Divider() {
            component.add(object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "divider")
                    return obj
                }
            })
        }
    }
    inner class ModuleScope(private val component: CardComponent) {
        fun PlainTextElement(content: String) {
            component.add(object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "plain-text")
                    obj.addProperty("content", content)
                    return obj
                }
            })
        }
        fun MarkdownElement(content: String) {
            component.add(object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "kmarkdown")
                    obj.addProperty("content", content)
                    return obj
                }
            })
        }
        fun ImageElement(src: String,
                         alt: String,
                         size: Size,
                         circle: Boolean) {
            component.add(object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "image")
                    obj.addProperty("circle", circle)
                    obj.addProperty("alt", alt)
                    obj.addProperty("src", src)
                    obj.addProperty("size", size.name.lowercase())
                    return obj
                }
            })
        }
        fun ButtonElement(
            theme: Theme = Theme.Primary,
            value: String = "",
            click: ClickType = ClickType.None,
            text: CardComponent,
        ) {
            component.add(object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "button")
                    obj.addProperty("theme", theme.name.lowercase())
                    obj.addProperty("value", value)
                    obj.addProperty("click", when (click) {
                        ClickType.None -> ""
                        ClickType.Link -> "link"
                        ClickType.ReturnValue -> "return-val"
                    })
                    obj.add("text", text.toJson())
                    return obj
                }
            })
        }
    }
    private val root: CardComponent = object : CardComponent() {
        var arr: MutableList<CardComponent> = mutableListOf()
        override fun add(component: CardComponent) {
            arr.add(component)
        }

        override fun toJson(): JsonElement {
            val a = JsonArray()
            arr.forEach { a.add(it.toJson()) }
            return a
        }
    }
    override fun content(): String {
        return root.toJson().toString()
    }

    init {
        MessageScope().contentBuilder()
    }
}
