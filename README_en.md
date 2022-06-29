# KhlKt

[简体中文](README.md) | **English**

AN SDK to build bot on KHL for JVM platforms.

## Quick Starting

1. Download the latest jar at [Releases](https://github.com/zly2006/KhlKt/releases) or clone this repo and build.
2. Create a java or kotlin project, and import it as library.
3. Write your code like this.

kotlin:
```kotlin
import io.github.zly2006.khlkt.client.Client
import io.github.zly2006.khlkt.events.ChannelMessageEvent

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
import io.github.zly2006.khlkt.JavaBaseClass;
import io.github.zly2006.khlkt.client.Client;
import io.github.zly2006.khlkt.contract.Self;
import io.github.zly2006.khlkt.events.ChannelMessageEvent;
import io.github.zly2006.khlkt.events.JavaEventHandler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static io.github.zly2006.khlkt.JavaBaseClassKt.connectWebsocket;

public class JavaApiTest extends JavaBaseClass {
    public JavaApiTest() {
        super();
    }

    public static void main(String[] args) throws FileNotFoundException {
        String token = new BufferedReader(new InputStreamReader(new FileInputStream("data/token.txt"))).lines().toList().get(0);
        Self self = connectWebsocket(new Client(token));
        ((JavaEventHandler<ChannelMessageEvent>) event -> {
            if (event.getContent().contains("hello")) {
                event.getChannel().sendMessage("hello");
            }
        }).addTo(self.getClient().getEventManager());
    }
}
```
4. Compile and run! 

## Open Source License Notation

This project is open-source under AGPL v3 license.

**Without permission**, ***closed source use is prohibited, all **indirect contact** (including but not limited to jar dependencies, http, websocket and any other network technologies) programs of this project must be open source.

## Contract me

email：<mailto:return65530@qq.com>

KHL：Steve47876#0001