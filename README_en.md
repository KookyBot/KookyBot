# Kooky Bot

[简体中文](README.md) | **English**

[![](https://img.shields.io/github/contributors/KookyBot/KookyBot)](https://github.com/KookyBot/KookyBot/graphs/contributors)
[![github stars](https://img.shields.io/github/stars/KookyBot/KookyBot)](https://github.com/KookyBot/KookyBot/stargazers)
![Build Status](https://www.travis-ci.org/KookyBot/KookyBot.svg?branch=master)
[![KookyBot](https://www.kaiheila.cn/api/v3/badge/guild?guild_id=6435808750354421&style=3)](https://kaihei.co/wnWOP9)
[![Release](https://jitpack.io/v/KookyBot/KookyBot.svg)](https://jitpack.io/#KookyBot/KookyBot)
[![GitHub issues by-label](https://img.shields.io/github/issues/KookyBot/KookyBot)](https://github.com/KookyBot/KookyBot/issues?q=is%3Aissue+is%3Aopen)


An SDK to build bot on KOOK (formally KaiHeiLa) for JVM platforms.

## Development Plan

- [ ] Complete cache model
- [ ] Automatic cache model update
- [ ] Stable automatic reconnection of login state machine

## Quick Starting

1. Go to the [JitPack page](https://jitpack.io/#KookyBot/KookyBot) of our project and import the newest version with tools like maven (recommended), or clone this repo and run `gradle jar`, then import manually the generated jar (not recommended).
2. Write your first line of code

Kotlin:

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

Java:

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

4. Compile and run! Now, invite your bot to your server and send `hello`

## Open Source License Notation

This project is open-source under AGPL v3 license.

Closed source use are prohibited **without permission**, all **indirect contact** (including but not limited to jar dependencies, http, websocket and any other network technologies) programs of this project must be open source.

## Documentation

[Docs (only available in Chinese for now)](docs/zh-cn/index.md)

It is working in progress, please wait.

## Contributing

We look forward to your contributions to this project! You can help us improve our even, add the HTTP API that doesn't exist yet, help us to complete our documentation, or add your own sample code!

## Derived Projects

Nice derived projects will be shown here! You can submit your project to us!

- [KookyGithub](https://github.com/zly2006/KookyGithub) - Sync GitHub webhook messages to KOOK

## Contract me

email：<mailto:return65530@qq.com>

Kook：Steve47876#0001