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
// --8<-- [start:say_default_description-kotlin]
@Command
class SlashSayDefaultDescriptionKotlin : ApplicationCommand() {
    @JDASlashCommand(name = "say_default_description")
    fun onSlashSayDefaultDescription(event: GuildSlashEvent, @SlashOption content: String) {
        event.reply(content).queue()
    }
}
// --8<-- [end:say_default_description-kotlin]

@WikiProfile(WikiProfile.Profile.KOTLIN_DSL)
// --8<-- [start:say_default_description-kotlin_dsl]
@Command
class SlashSayDefaultDescriptionKotlinDsl {
    fun onSlashSayDefaultDescription(event: GuildSlashEvent, content: String) {
        event.reply(content).queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say_default_description", function = ::onSlashSayDefaultDescription) {
            option("content")
        }
    }
}
// --8<-- [end:say_default_description-kotlin_dsl]