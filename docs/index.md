# KhlKt Docs
`Kotlin`

### 登录你的Bot

这非常简单！

我们支持两种方式进行登录，其中包括Websocket和WebHook

首先，你需要输入自己的token，并用它初始化Client对象

```kotlin
val client = Client("token")
```

然后，使用`client.start()`可以使用websocket登录，而使用`client.start(host: String, port: Int, path: String, verifyToken: String = "")`可以用webHook登录。

`client.start`会尝试登录直到登录成功，这期间会阻塞当前进程，并在完成后返回一个`Self`对象。

登录后触发`SelfOnlineEvent`

websocket链接会在ping超时后自动尝试重连，同时触发`SelfReLoginEvent`

**注意：** `Self`和`Client`是一一对应的，一般来说，网络相关的api会放在`Client`，缓存的的数据会放在`Self`，如guild、private chat等数据。

### 添加事件监听器

一个事件监听器只能监听一种事件，这通过泛型参数实现。

您可以使用`client.eventManager.addListener<T> { ... }`监听一个事件。

***注意：`T`必须是一个非open或abstract的类型，因为EventManager查找监听器时的根据是事件对象的最终类型***

### 发送消息

`Channel`和`User`(`GuildUser`等均继承此类)派生于`MessageReceiver`，通常来说，你可以构造一个实现了`Messsage`类的对象，并通过`MessageReceiver.sendMessage`发送消息，但是通常情况下`Channel`等类提供了更多发送消息的方式，可以减少使用中的错误。

例如`Channel.sendMessage(message: String)`发送markdown消息，也可以`Channel.sendCardMessage`发送card消息。

### Card

在这里，我们提供了关于生成Card消息的教程。

通常情况下，我们推荐使用`User.sendCardMessage` & `Channel.sendCardMessage`来发送卡片消息，而不是新建一个CardMessage对象。

