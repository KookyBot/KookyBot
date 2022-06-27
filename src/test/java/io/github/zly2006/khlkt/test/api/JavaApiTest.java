package io.github.zly2006.khlkt.test.api;

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
