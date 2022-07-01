# Kooky Bot

[简体中文](README.md) | **English**

An SDK to build bot on KOOK (formally KaiHeiLa) for JVM platforms.

## Quick Starting

1. Download the latest jar at [Releases](https://github.com/zly2006/KookyBot/releases) or clone this repo and build.
2. Create a java or kotlin project, and import it as library.
3. Write your code like this.

kotlin:
```kotlin
import io.github.zly2006.kookybot.client.Client
import io.github.zly2006.kookybot.events.ChannelMessageEvent

fun main() {
    val client = Client("token")
    val self = client.start()
    client.eventManager.addListener<ChannelMessageEvent> {
        if (content.contains("hello")) {
            channel.sendCardMessage {
                Card {
                    HeaderModule(PlainTextElement("Hello"))
                    Divider()
                }
            }
        }
    }
    awaitCancellation()
}
```
java:

```java
```
4. Compile and run! 

## Open Source License Notation

This project is open-source under AGPL v3 license.

**Without permission**, ***closed source use is prohibited, all **indirect contact** (including but not limited to jar dependencies, http, websocket and any other network technologies) programs of this project must be open source.

## Contract me

email：<mailto:return65530@qq.com>

Kook：Steve47876#0001