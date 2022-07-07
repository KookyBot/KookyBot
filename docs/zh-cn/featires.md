# KookyBot的特性

> 这些内容是KookyBot独有的，并不是官方实现，请不要向关方寻求相关支持。

### ClickType.ExecuteCommand

来自：`io.github.zly2006.kookybot.message.CardMessage.ClickType`

用途：模拟用户执行命令（注意：此时用户没有发出命令消息，这是模拟的结果。）

### onclick

来自：`io.github.zly2006.kookybot.message.CardMessage.MessageScope.ButtonElement`

用途：相当于`return-val`，SDK自动生成value并且自动监听对应的点击事件。

示例：
```kotlin
SectionModule(
    text = MarkdownElement("**Click Me!**"),
    accessory = ButtonElement(
        text = PlainTextElement("hi"),
        onclick = {
            it.channel?.sendMessage("hi~")
        })
)
```

## Happy Coding~