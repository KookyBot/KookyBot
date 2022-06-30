# Kooky Bot

**简体中文** | [English](README_en.md)

JVM平台上的开黑啦Bot SDK

## Quick Starting

1. 在Releases下载下载最新版jar或clone本仓库编译jar
2. 新建一个java或kotlin项目，并把jar作为库导入
3. 写下你的第一行代码

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
import io.github.zly2006.kookybot.JavaBaseClass;
import io.github.zly2006.kookybot.client.Client;
import io.github.zly2006.kookybot.contract.Self;
import io.github.zly2006.kookybot.events.ChannelMessageEvent;
import io.github.zly2006.kookybot.events.JavaEventHandler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static io.github.zly2006.kookybot.JavaBaseClassKt.connectWebsocket;

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
4. 编译，运行！现在，把你的机器人拉进服务器，发一条hello吧

## 开源协议提示

本项目由AGPL v3协议开源。

**未经允许**的情况下，***禁止***闭源使用，所有**间接接触**（包括但不限于jar依赖、http和ws等网络技术）本项目的程序必须开源。

## 文档

[Docs](docs/index.md)

还在做，不要急的啦

## 贡献

本项目期待你的参与！不管是完善事件系统还是增加尚无的HTTP API，不管是帮忙写文档还是添加自己的示例代码，我都衷心期待每一位的贡献！

## 衍生项目

优秀的衍生项目可以再次得到展示！欢迎把你的项目提交给我。

## 联系我

邮箱：<mailto:return65530@qq.com>

开黑啦：Steve47876#0001