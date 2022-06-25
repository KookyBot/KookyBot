package com.github.zly2006.khlkt.message

abstract class MessageComponent {
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

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()
        var cur :MessageComponent? = this
        while (cur != null){
            builder.append(cur.toMarkdown())
            cur=cur.next
        }
        return builder.toString()
    }
}