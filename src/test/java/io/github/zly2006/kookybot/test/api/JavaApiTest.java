package io.github.zly2006.kookybot.test.api;

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
