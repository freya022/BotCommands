package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:word_command-java]
@Command
public class SlashWord extends ApplicationCommand {
    @JDASlashCommand(name = "word", description = "Autocompletes a word")
    public void onSlashWord(GuildSlashEvent event,
                            @SlashOption(description = "The word", autocomplete = SlashWordAutocomplete.WORD_AUTOCOMPLETE_NAME) String word) {
        event.reply("Your word was " + word).setEphemeral(true).queue();
    }
}
// --8<-- [end:word_command-java]
