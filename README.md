# Kooky Bot

**简体中文** | [English](README_en.md)

[![](https://img.shields.io/github/contributors/KookyBot/KookyBot)](https://github.com/KookyBot/KookyBot/graphs/contributors)
[![github stars](https://img.shields.io/github/stars/KookyBot/KookyBot)](https://github.com/KookyBot/KookyBot/stargazers)
![Build Status](https://www.travis-ci.org/KookyBot/KookyBot.svg?branch=master)
[![KookyBot](https://www.kaiheila.cn/api/v3/badge/guild?guild_id=6435808750354421&style=3)](https://kaihei.co/wnWOP9)
[![Release](https://jitpack.io/v/KookyBot/KookyBot.svg)](https://jitpack.io/#KookyBot/KookyBot)
[![GitHub issues by-label](https://img.shields.io/github/issues/KookyBot/KookyBot)](https://github.com/KookyBot/KookyBot/issues?q=is%3Aissue+is%3Aopen)


JVM 平台上的 KOOK (原开黑啦) Bot SDK

## 开发计划

- [ ] 完整缓存模型
- [ ] 缓存模型自动更新
- [ ] 登录状态机稳定的自动重连

## Quick Starting

1. 在本项目[JitPack页面](https://jitpack.io/#KookyBot/KookyBot) 找到最新发布版并使用maven等工具导入（推荐），或者clone本仓库并运行`gradle jar`然后手动导入生成的库（不推荐）
2. 写下你的第一行代码

Kotlin:

```kotlin
import io.github.kookybot.client.Client
import io.github.kookybot.events.channel.ChannelMessageEvent

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
package io.github.kookybot.test.api;

import io.github.kookybot.JavaBaseClass;
import io.github.kookybot.client.Client;
import io.github.kookybot.contract.Self;
import io.github.kookybot.events.channel.ChannelMessageEvent;
import events.io.github.kookybot.EventHandler;
import events.io.github.kookybot.Listener;
import io.github.kookybot.message.CardMessage;

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

4. 编译，运行！现在，把你的机器人拉进服务器，发一条hello吧

## 开源协议提示

本项目由AGPL v3协议开源。

**未经允许**的情况下，***禁止***闭源使用，所有**间接接触**（包括但不限于jar依赖、http和ws等网络技术）本项目的程序必须开源。

## 文档

[Docs](docs/zh-cn/index.md)

还在做，不要急的啦

## 贡献

本项目期待你的参与！不管是完善事件系统还是增加尚无的HTTP API，不管是帮忙写文档还是添加自己的示例代码，我都衷心期待每一位的贡献！

## 衍生项目

优秀的衍生项目可以再次得到展示！欢迎把你的项目提交给我。

- [KookyGithub](https://github.com/zly2006/KookyGithub) - 将GithubWebhook的信息同步到Kook

## 联系我

邮箱：<mailto:return65530@qq.com>

Kook：Steve47876#0001