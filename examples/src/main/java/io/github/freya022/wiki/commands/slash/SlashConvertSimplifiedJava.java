package io.github.freya022.wiki.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.wiki.switches.WikiCommandProfile;

import java.util.concurrent.TimeUnit;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:convert_simplified-java]
@Command
public class SlashConvertSimplifiedJava extends ApplicationCommand {
    @JDASlashCommand(name = "convert_simplified", description = "Convert time to another unit")
    public void onSlashTimeInSimplified(
            GuildSlashEvent event,
            @SlashOption(description = "The time to convert") long time,
            @SlashOption(description = "The unit to convert from", usePredefinedChoices = true) TimeUnit from,
            @SlashOption(description = "The unit to convert to", usePredefinedChoices = true) TimeUnit to
    ) {
        event.reply(to.convert(time, from) + " " + to.toString().toLowerCase()).queue();
    }
}
// --8<-- [end:convert_simplified-java]
