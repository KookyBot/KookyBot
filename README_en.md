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
import io.github.zly2006.kookybot.events.channel.ChannelMessageEvent

fun main() {
    val client = Client("token")
    val self = client.start()
    val logger = LoggerFactory.getLogger("ApiTest")
    client.eventManager.addListener<ChannelMessageEvent> {
        if (content.contains("hello")) {
            logger.info("hello")
            channel.sendCardMessage {
                Card {
                    HeaderModule(PlainTextElement("Hello"))
                    Divider()
                }
                Card {
                    SectionModule(
                        text = MarkdownElement("**Click Me!**"),
                        accessory = ButtonElement(
                            text = PlainTextElement("hi"),
                            onclick = {
                                it.channel?.sendMessage("hi~")
                            })
                    )
                }
            }
        }
    }
    awaitCancellation()
}
```
java:

```java
package io.github.zly2006.kookybot.test.api;

import io.github.zly2006.kookybot.JavaBaseClass;
import io.github.zly2006.kookybot.client.Client;
import io.github.zly2006.kookybot.contract.Self;
import io.github.zly2006.kookybot.events.channel.ChannelMessageEvent;
import io.github.zly2006.kookybot.events.EventHandler;
import io.github.zly2006.kookybot.events.Listener;
import io.github.zly2006.kookybot.message.CardMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class JavaApiTest extends JavaBaseClass {
    static public class MyListener implements Listener {
        @EventHandler
        public void onChannelMessage(ChannelMessageEvent event) {
            System.out.println(event.getContent());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String token = new BufferedReader(new InputStreamReader(new FileInputStream("data/token.txt"))).lines().toList().get(0);
        Client client = new Client(token);
        Self self = utils.connectWebsocket(client);
        client.getEventManager().addClassListener(new MyListener());
    }
}
```
4. Compile and run! 

## Open Source License Notation

This project is open-source under AGPL v3 license.

**Without permission**, ***closed source use is prohibited, all **indirect contact** (including but not limited to jar dependencies, http, websocket and any other network technologies) programs of this project must be open source.

## Contract me

email：<mailto:return65530@qq.com>

Kook：Steve47876#0001