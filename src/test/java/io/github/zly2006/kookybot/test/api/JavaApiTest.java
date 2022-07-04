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
        @EventHandler
        public void onHello(ChannelMessageEvent event) {
            if (event.getContent().contains("hello")) {
                event.getChannel().sendCardMessage(messageScope -> {
                    messageScope.Card(CardMessage.Size.LG, CardMessage.Theme.Primary, "#aaaaaa",
                            cardScope -> {
                                cardScope.SectionModule(messageScope.PlainTextElement("hello"),
                                        messageScope.ButtonElement(CardMessage.Theme.Primary, "", CardMessage.ClickType.ReturnValue,
                                                messageScope.PlainTextElement("hi"), cardButtonClickEvent -> {
                                                    assert cardButtonClickEvent.getChannel() != null;
                                                    cardButtonClickEvent.getChannel().sendMessage("hi!!");
                                                    return null;
                                                }), CardMessage.LeftRight.Right);
                                return null;
                            });
                    return null;
                });
            }
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        String token = new BufferedReader(new InputStreamReader(new FileInputStream("data/token.txt"))).lines().toList().get(0);
        Client client = new Client(token);
        Self self = utils.connectWebsocket(client);
        client.getEventManager().addClassListener(new MyListener());
    }
}
