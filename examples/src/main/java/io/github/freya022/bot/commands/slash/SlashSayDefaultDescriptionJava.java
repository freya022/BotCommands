package io.github.freya022.bot.commands.slash;

import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.bot.switches.WikiCommandProfile;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:say_default_description-java]
@Command
public class SlashSayDefaultDescriptionJava extends ApplicationCommand {
    @JDASlashCommand(name = "say_default_description")
    public void onSlashSayDefaultDescription(GuildSlashEvent event, @SlashOption String content) {
        event.reply(content).queue();
    }
}
// --8<-- [end:say_default_description-java]
