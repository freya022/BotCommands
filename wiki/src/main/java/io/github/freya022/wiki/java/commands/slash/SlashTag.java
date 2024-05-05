package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

// --8<-- [start:slash_subcommands-java]
@Command
public class SlashTag extends ApplicationCommand {
    // Data for /tag create
    @JDASlashCommand(name = "tag", subcommand = "create", description = "Creates a tag")
    // Data for /tag
    @TopLevelSlashCommandData(description = "Manage tags")
    public void onSlashTagCreate(GuildSlashEvent event) {
        // ...
    }

    // Data for /tag delete
    @JDASlashCommand(name = "tag", subcommand = "delete", description = "Deletes a tag")
    public void onSlashTagDelete(GuildSlashEvent event) {
        // ...
    }
}
// --8<-- [end:slash_subcommands-java]
