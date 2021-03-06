package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

public class RequestOnlinePlayers extends Intent {

    public RequestOnlinePlayers(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.online-players";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        if (!message.getChannelType().isGuild()) {
            MessageFactory.makeWarning(message, "Right now it's just me and you online ;)").queue();
            return;
        }

        int online = 0;
        for (Member member : message.getGuild().getMembers()) {
            if (!member.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                online++;
            }
        }

        MessageFactory.makeInfo(message, "There are **:online** people online out of **:total** people on the server.")
            .set("online", online)
            .set("total", message.getGuild().getMembers().size())
            .queue();
    }
}
