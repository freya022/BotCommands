package io.github.freya022.bot.commands.slash;

import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.bot.commands.WikiProfile;

@WikiProfile(WikiProfile.Profile.JAVA)
@Command
public class SlashPingJava extends ApplicationCommand {
    @JDASlashCommand(name = "ping", description = "Pong!")
    public void onSlashPing(GuildSlashEvent event) {
        event.deferReply(true).queue();

        event.getJDA().getRestPing().queue(ping -> {
            event.getHook().editOriginal("Pong! " + ping + " ms").queue();
        });
    }
}
