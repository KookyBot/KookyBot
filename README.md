# KhlKt

JVM平台上的开黑啦Bot SDK

## Quick Starting

1. 在Releases下载下载最新版jar或clone本仓库编译jar
2. 新建一个java或kotlin项目，并把jar作为库导入
3. 写下你的第一行代码

kotlin:
```kotlin
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
package api;

import com.github.zly2006.khlkt.JavaBaseClass;
import com.github.zly2006.khlkt.client.Client;
import com.github.zly2006.khlkt.contract.Self;

import static com.github.zly2006.khlkt.JavaBaseClassKt.connectWebsocket;

public class JavaApiTest extends JavaBaseClass {
    public static void main(String[] args) {
        Self self = connectWebsocket(new Client("token"));
    }
}
```
4. 编译，运行！现在，把你的机器人拉进服务器，发一条hello吧

## 开源协议提示

本项目由AGPL v3协议开源。

**未经允许**的情况下，***禁止***闭源使用，所有**间接接触**（包括但不限于jar依赖、http和ws等网络技术）本项目的程序必须开源。

## 文档

文档就是根本没有，计划先用GitHub wiki，有意参与者清联系本人。

## 贡献

本项目期待你的参与！不管是完善事件系统还是增加尚无的HTTP API，不管是帮忙写文档还是添加自己的示例代码，我都衷心期待每一位的贡献！

## 衍生项目

优秀的衍生项目可以再次得到展示！欢迎把你的项目提交给我。

## 联系我

邮箱：<mailto:return65530@qq.com>

开黑啦：Steve47876#0001