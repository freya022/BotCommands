package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.wiki.switches.WikiCommandProfile;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:say_default_description-java]
@Command
public class SlashSayDefaultDescription extends ApplicationCommand {
    @JDASlashCommand(name = "say_default_description")
    public void onSlashSayDefaultDescription(GuildSlashEvent event, @SlashOption String content) {
        event.reply(content).queue();
    }
}
// --8<-- [end:say_default_description-java]
