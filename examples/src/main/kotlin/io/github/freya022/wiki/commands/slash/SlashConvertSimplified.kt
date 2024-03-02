package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile
import java.util.concurrent.TimeUnit

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:convert_simplified-kotlin]
@Command
class SlashConvertSimplifiedKotlin : ApplicationCommand() {
    @JDASlashCommand(name = "convert_simplified", description = "Convert time to another unit")
    suspend fun onSlashConvertSimplified(
        event: GuildSlashEvent,
        @SlashOption(description = "The time to convert") time: Long,
        @SlashOption(description = "The unit to convert from", usePredefinedChoices = true) from: TimeUnit,
        @SlashOption(description = "The unit to convert to", usePredefinedChoices = true) to: TimeUnit
    ) {
        event.reply("${to.convert(time, from)} ${to.name.lowercase()}").await()
    }
}
// --8<-- [end:convert_simplified-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:convert_simplified-kotlin_dsl]
@Command
class SlashConvertSimplifiedKotlinDsl : GlobalApplicationCommandProvider {
    suspend fun onSlashConvertSimplified(event: GuildSlashEvent, time: Long, from: TimeUnit, to: TimeUnit) {
        event.reply("${to.convert(time, from)} ${to.name.lowercase()}").await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("convert_simplified", function = ::onSlashConvertSimplified) {
            description = "Convert time to another unit"

            option("time") {
                description = "The time to convert"
            }

            option("from") {
                description = "The unit to convert from"

                usePredefinedChoices = true
            }

            option("to") {
                description = "The unit to convert to"

                usePredefinedChoices = true
            }
        }
    }
}
// --8<-- [end:convert_simplified-kotlin_dsl]