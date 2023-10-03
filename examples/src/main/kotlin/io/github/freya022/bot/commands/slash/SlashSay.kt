package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.bot.commands.WikiProfile

@WikiProfile(WikiProfile.Profile.KOTLIN)
// --8<-- [start:say-kotlin]
@Command
class SlashSayKotlin : ApplicationCommand() {
    @JDASlashCommand(name = "say", description = "Says something")
    fun onSlashSay(event: GuildSlashEvent, @SlashOption(description = "What to say") content: String) {
        event.reply(content).queue()
    }
}
// --8<-- [end:say-kotlin]

@WikiProfile(WikiProfile.Profile.KOTLIN_DSL)
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