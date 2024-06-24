package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.core.entities.InputUser;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//TODO remove wildcard once fixed
@SuppressWarnings("unchecked")
@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:bulk_ban-java]
@Command
public class SlashBulkBan extends ApplicationCommand {
    @JDASlashCommand(name = "bulk_ban", description = "Ban users in bulk")
    public void onSlashBulkBan(
            GuildSlashEvent event,
            @SlashOption(description = "Users to ban") @MentionsString List<? extends InputUser> users,
            @SlashOption(description = "Time frame of messages to delete") Long timeframe,
            @SlashOption(description = "Unit of the time frame", usePredefinedChoices = true) TimeUnit unit
    ) {
        // Check if any member cannot be banned
        final var higherMembers = new ArrayList<Member>();
        for (var user : users) {
            final Member member = user.getMember();
            if (member == null) continue;

            if (!event.getGuild().getSelfMember().canInteract(member)) {
                higherMembers.add(member);
            }
        }

        if (!higherMembers.isEmpty()) {
            final String mentions = higherMembers.stream().map(IMentionable::getAsMention).collect(Collectors.joining());
            event.reply("Cannot ban " + mentions + " as they have equal/higher roles")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue();

        event.getGuild().ban((List<UserSnowflake>) (List<? extends UserSnowflake>) users, Duration.of(timeframe, unit.toChronoUnit()))
                .queue(response -> {
                    event.getHook().sendMessageFormat("Banned %s users, %s failed", response.getBannedUsers().size(), response.getFailedUsers().size()).queue();
                }, new ErrorHandler()
                        .handle(ErrorResponse.MISSING_PERMISSIONS, exception -> {
                            event.getHook().sendMessage("Could not ban users due to missing permissions").queue();
                        })
                        .handle(ErrorResponse.FAILED_TO_BAN_USERS, exception -> {
                            event.getHook().sendMessage("Could not ban anyone").queue();
                        })
                );
    }
}
// --8<-- [end:bulk_ban-java]
