package io.github.kookybot.test.api;

import io.github.kookybot.JavaBaseClass;
import io.github.kookybot.annotation.Filter;
import io.github.kookybot.client.Client;
import io.github.kookybot.commands.CommandSource;
import io.github.kookybot.contract.Self;
import io.github.kookybot.events.EventHandler;
import io.github.kookybot.events.Listener;
import io.github.kookybot.events.channel.ChannelMessageEvent;
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

        @EventHandler
        @Filter(pattern = ".echo num {num,\\d+}")
        public void channelQC(String num, CommandSource commandSource) {
            commandSource.sendMessage(num);
        }

        @EventHandler
        public void onHello(ChannelMessageEvent event) {
            if (event.getContent().contains("hello")) {
                event.getChannel().sendCardMessage(null, messageScope -> {
                    messageScope.Card(CardMessage.Size.LG, CardMessage.Theme.Primary, "#aaaaaa",
                            cardScope -> {
                                cardScope.SectionModule(messageScope.PlainTextElement("hello"),
                                        messageScope.ButtonElement(CardMessage.Theme.Primary, "", CardMessage.ClickType.ReturnValue,
                                                messageScope.PlainTextElement("hi"), cardButtonClickEvent -> {
                                                    assert cardButtonClickEvent.getChannel() != null;
                                                    cardButtonClickEvent.getChannel().sendMessage("hi!!", null);
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
        Client client = new Client(token, null);
        Self self = utils.connectWebsocket(client);
        client.getEventManager().addClassListener(new MyListener());
    }
}
