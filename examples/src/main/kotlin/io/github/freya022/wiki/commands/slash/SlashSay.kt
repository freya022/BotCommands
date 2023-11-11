package io.github.freya022.wiki.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.wiki.switches.WikiCommandProfile

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:say-kotlin]
@Command
class SlashSayKotlin : ApplicationCommand() {
    @JDASlashCommand(name = "say", description = "Says something")
    fun onSlashSay(event: GuildSlashEvent, @SlashOption(description = "What to say") content: String) {
        event.reply(content).queue()
    }
}
// --8<-- [end:say-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:say-kotlin_dsl]
@Command
class SlashSayKotlinDsl {
    fun onSlashSay(event: GuildSlashEvent, content: String) {
        event.reply(content).queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say", function = ::onSlashSay) {
            description = "Says something"

            option("content") {
                description = "What to say"
            }
        }
    }
}
// --8<-- [end:say-kotlin_dsl]