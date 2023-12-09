package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:say-java]
@Command
public class SlashSay extends ApplicationCommand {
    @JDASlashCommand(name = "say", description = "Says something")
    public void onSlashSay(GuildSlashEvent event, @SlashOption(description = "What to say") String content) {
        event.reply(content).queue();
    }
}
// --8<-- [end:say-java]
