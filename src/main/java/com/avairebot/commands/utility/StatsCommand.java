package com.avairebot.commands.utility;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.Statistics;
import com.avairebot.audio.AudioHandler;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;
import com.google.gson.internal.LinkedTreeMap;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatsCommand extends Command {

    private final DecimalFormat decimalNumber;

    public StatsCommand(AvaIre avaire) {
        super(avaire);

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');

        decimalNumber = new DecimalFormat("#,##0.00", decimalFormatSymbols);
    }

    @Override
    public String getName() {
        return "Stats Command";
    }

    @Override
    public String getDescription() {
        return "Displays information about Ava and some related stats.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows some stats about the bot.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("stats", "about");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        StringBuilder description = new StringBuilder("Created by [Senither#0001](https://senither.com/) using the [JDA](https://github.com/DV8FromTheWorld/JDA) framework!");
        if (avaire.getCache().getAdapter(CacheType.FILE).has("github.commits")) {
            description = new StringBuilder("**" + context.i18n("latestChanges") + "**\n");
            List<LinkedTreeMap<String, Object>> items = (List<LinkedTreeMap<String, Object>>) avaire.getCache().getAdapter(CacheType.FILE).get("github.commits");

            for (int i = 0; i < 3; i++) {
                LinkedTreeMap<String, Object> item = items.get(i);
                LinkedTreeMap<String, Object> commit = (LinkedTreeMap<String, Object>) item.get("commit");

                description.append(String.format("[`%s`](%s) %s\n",
                    item.get("sha").toString().substring(0, 7),
                    item.get("html_url"),
                    commit.get("message").toString().split("\n")[0].trim()
                ));
            }
        }

        context.makeEmbeddedMessage(MessageType.INFO,
            new MessageEmbed.Field(context.i18n("fields.author"), "Senither#0001", true),
            new MessageEmbed.Field(context.i18n("fields.botId"), context.getJDA().getSelfUser().getId(), true),
            new MessageEmbed.Field(context.i18n("fields.library"), "[JDA](https://github.com/DV8FromTheWorld/JDA)", true),
            new MessageEmbed.Field(context.i18n("fields.database"), getDatabaseQueriesStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.messages"), getMessagesReceivedStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.shard"), "" + context.getJDA().getShardInfo().getShardId(), true),
            new MessageEmbed.Field(context.i18n("fields.commands"), NumberUtil.formatNicely(Statistics.getCommands()), true),
            new MessageEmbed.Field(context.i18n("fields.memory"), memoryUsage(context), true),
            new MessageEmbed.Field(context.i18n("fields.uptime"), applicationUptime(), true),
            new MessageEmbed.Field(context.i18n("fields.members"), NumberUtil.formatNicely(avaire.getShardEntityCounter().getUsers()), true),
            new MessageEmbed.Field(context.i18n("fields.channels"), NumberUtil.formatNicely(avaire.getShardEntityCounter().getChannels()), true),
            new MessageEmbed.Field(context.i18n("fields.servers"), NumberUtil.formatNicely(avaire.getShardEntityCounter().getGuilds()), true)
        )
            .setTitle(context.i18n("title"), "https://discordapp.com/invite/gt2FWER")
            .setAuthor("AvaIre v" + AppInfo.getAppInfo().VERSION, "https://discordapp.com/invite/gt2FWER", avaire.getSelfUser().getEffectiveAvatarUrl())
            .setFooter(String.format(
                context.i18n("footer"),
                NumberUtil.formatNicely(AudioHandler.getTotalListenersSize()),
                NumberUtil.formatNicely(AudioHandler.getTotalQueueSize())
            ))
            .setDescription(description.toString())
            .queue();

        return true;
    }

    private String applicationUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long seconds = rb.getUptime() / 1000;
        long d = (long) Math.floor(seconds / 86400);
        long h = (long) Math.floor((seconds % 86400) / 3600);
        long m = (long) Math.floor(((seconds % 86400) % 3600) / 60);
        long s = (long) Math.floor(((seconds % 86400) % 3600) % 60);

        if (d > 0) {
            return String.format("%sd %sh %sm %ss", d, h, m, s);
        }

        if (h > 0) {
            return String.format("%sh %sm %ss", h, m, s);
        }

        if (m > 0) {
            return String.format("%sm %ss", m, s);
        }
        return String.format("%ss", s);
    }

    private String getDatabaseQueriesStats(CommandMessage context) {
        return String.format(
            context.i18n("formats.database"),
            NumberUtil.formatNicely(Statistics.getQueries()),
            decimalNumber.format(Statistics.getQueries() / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / (double) (1000 * 60)))
        );
    }

    private String getMessagesReceivedStats(CommandMessage context) {
        return String.format(
            context.i18n("formats.messages"),
            NumberUtil.formatNicely(Statistics.getMessages()),
            decimalNumber.format(Statistics.getMessages() / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / (double) (1000)))
        );
    }

    private String memoryUsage(CommandMessage context) {
        return String.format(
            context.i18n("formats.memory"),
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
            Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }
}
