package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "self-assignable-role-command")
public class RemoveSelfAssignableRoleCommand extends Command {

    public RemoveSelfAssignableRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Self Assignable Role Command";
    }

    @Override
    public String getDescription() {
        return "Removes a role from the self-assignable roles list, any role on the list can be claimed by users when they use `:prefixiam <role>`.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <role>` - Removes the mentioned role from the self-assignable roles list.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command DJ`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rsar");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument, the `role` argument is required.");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(), args[0]);
        if (role == null) {
            context.makeWarning(":user Invalid role, I couldn't find any role called **:role**")
                .set("role", args[0])
                .queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(context.getMessage(), role)) {
            return false;
        }

        try {
            GuildTransformer transformer = GuildController.fetchGuild(avaire, context.getMessage());
            if (transformer == null) {
                return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
            }

            transformer.getSelfAssignableRoles().remove(role.getId());
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", AvaIre.GSON.toJson(transformer.getSelfAssignableRoles()));
                });

            context.makeSuccess("Role **:role** role has been removed from the self-assignable list.")
                .set("role", role.getName()).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
