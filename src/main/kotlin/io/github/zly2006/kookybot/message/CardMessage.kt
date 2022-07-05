/* KookyBot - a SDK of <https://www.kookapp.cn> for JVM platform
Copyright (C) 2022, zly2006 & contributors

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

@file:Suppress("FunctionName")

package io.github.zly2006.kookybot.message

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.contract.TextChannel
import io.github.zly2006.kookybot.events.CardButtonClickEvent
import org.slf4j.LoggerFactory
import java.util.*

class CardMessage(client: Client, contentBuilder: MessageScope.() -> Unit) : Message(
    client) {

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
        fun PlainTextElement(content: String): CardComponent {
            return object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "plain-text")
                    obj.addProperty("content", content)
                    return obj
                }
            }
        }
        fun MarkdownElement(content: String): CardComponent {
            return object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "kmarkdown")
                    obj.addProperty("content", content)
                    return obj
                }
            }
        }
        fun ImageElement(src: String,
                         alt: String,
                         size: Size,
                         circle: Boolean): CardComponent {
            return object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "image")
                    obj.addProperty("circle", circle)
                    obj.addProperty("alt", alt)
                    obj.addProperty("src", src)
                    obj.addProperty("size", size.name.lowercase())
                    return obj
                }
            }
        }
        fun ButtonElement(
            theme: Theme = Theme.Primary,
            value: String = "",
            click: ClickType = ClickType.None,
            text: CardComponent,
            onclick: ((CardButtonClickEvent) -> Unit)? = null
        ): CardComponent {
            val id = if (value == "") UUID.randomUUID().toString() else value
            var clickType = click
            if (onclick != null) {
                client.eventManager.clickEvents.add(id to onclick)
                clickType = ClickType.ReturnValue
            }
            return object : CardComponent() {
                override fun toJson(): JsonElement {
                    val obj = JsonObject()
                    obj.addProperty("type", "button")
                    obj.addProperty("theme", theme.name.lowercase())
                    obj.addProperty("value", id)
                    obj.addProperty("click", when (clickType) {
                        ClickType.None -> ""
                        ClickType.Link -> "link"
                        ClickType.ReturnValue -> "return-val"
                    })
                    obj.add("text", text.toJson())
                    return obj
                }
            }
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
                    obj.addProperty("mode", (
                            if (accessory.toJson().asJsonObject.get("type").asString == "button")
                                LeftRight.Right
                            else
                                mode
                            ).name.lowercase())
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
        fun PlainTextElement(content: String) = component.add(MessageScope().PlainTextElement(content))
        fun MarkdownElement(content: String) = component.add(MessageScope().MarkdownElement(content))
        fun ImageElement(src: String,
                         alt: String,
                         size: Size,
                         circle: Boolean) = component.add(MessageScope().ImageElement(src, alt, size, circle))
        fun ButtonElement(
            theme: Theme = Theme.Primary,
            value: String = "",
            click: ClickType = ClickType.None,
            text: CardComponent,
            onclick: ((CardButtonClickEvent) -> Unit)? = null
        ) = component.add(MessageScope().ButtonElement(theme, value, click, text, onclick))
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

    override fun send2Channel(channel: TextChannel) {
        client.sendChannelMessage(
            type = 10,
            target = channel,
            content = content()
        )
    }
    override val type: Int = 10
    init {
        MessageScope().contentBuilder()
        LoggerFactory.getLogger(this::class.java).debug(content())
    }
}
