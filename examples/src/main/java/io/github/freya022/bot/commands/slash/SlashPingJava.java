package io.github.freya022.bot.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.bot.switches.WikiCommandProfile;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:ping-java]
@Command
public class SlashPingJava extends ApplicationCommand {
    // Default scope is global, guild-only (GUILD_NO_DM)
    @JDASlashCommand(name = "ping", description = "Pong!")
    public void onSlashPing(GuildSlashEvent event) {
        event.deferReply(true).queue();

        event.getJDA().getRestPing().queue(ping -> {
            event.getHook().editOriginal("Pong! " + ping + " ms").queue();
        });
    }
}
// --8<-- [end:ping-java]
