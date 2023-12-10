package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:say_default_description-kotlin]
@Command
class SlashSayDefaultDescriptionKotlin : ApplicationCommand() {
    @JDASlashCommand(name = "say_default_description")
    suspend fun onSlashSayDefaultDescription(event: GuildSlashEvent, @SlashOption content: String) {
        event.reply(content).await()
    }
}
// --8<-- [end:say_default_description-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:say_default_description-kotlin_dsl]
@Command
class SlashSayDefaultDescriptionKotlinDsl {
    suspend fun onSlashSayDefaultDescription(event: GuildSlashEvent, content: String) {
        event.reply(content).await()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("say_default_description", function = ::onSlashSayDefaultDescription) {
            option("content")
        }
    }
}
// --8<-- [end:say_default_description-kotlin_dsl]