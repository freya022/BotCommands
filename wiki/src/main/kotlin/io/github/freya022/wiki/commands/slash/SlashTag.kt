package io.github.freya022.wiki.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:slash_subcommands-kotlin]
@Command
class SlashTag : ApplicationCommand() {
    // Data for /tag create
    @JDASlashCommand(name = "tag", subcommand = "create", description = "Creates a tag")
    // Data for /tag
    @TopLevelSlashCommandData(description = "Manage tags")
    fun onSlashTagCreate(event: GuildSlashEvent) {
        // ...
    }

    // Data for /tag delete
    @JDASlashCommand(name = "tag", subcommand = "delete", description = "Deletes a tag")
    fun onSlashTagDelete(event: GuildSlashEvent) {
        // ...
    }
}
// --8<-- [end:slash_subcommands-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:slash_subcommands-kotlin_dsl]
@Command
class SlashTagDsl : GlobalApplicationCommandProvider {
    fun onSlashTagCreate(event: GuildSlashEvent) {
        // ...
    }

    fun onSlashTagDelete(event: GuildSlashEvent) {
        // ...
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        // Pass a null function as this is not a top-level command
        manager.slashCommand("tag", function = null) {
            description = "Manage tags"

            subcommand("create", ::onSlashTagCreate) {
                description = "Creates a tag"
            }

            subcommand("delete", ::onSlashTagDelete) {
                description = "Deletes a tag"
            }
        }
    }
}
// --8<-- [end:slash_subcommands-kotlin_dsl]