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

package io.github.kookybot.message.kmd

abstract class MessageComponent {
    object Empty : MessageComponent() {
        override fun toMarkdown(): String = ""
    }

    /**
     * 注意：
     */
    abstract fun toMarkdown(): String
    open var next: MessageComponent? = null
    open fun append(component: MessageComponent) {
        var cur = this
        while (cur.next != null) cur = cur.next!!
        cur.next = component
    }

    open fun append(text: String) {
        var cur = this
        while (cur.next != null) cur = cur.next!!
        if (cur is PlainTextComponent)
            cur.text += text
        else
            cur.next = PlainTextComponent(text)
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()
        var cur: MessageComponent? = this
        while (cur != null) {
            builder.append(cur.toMarkdown())
            cur = cur.next
        }
        return builder.toString()
    }
}