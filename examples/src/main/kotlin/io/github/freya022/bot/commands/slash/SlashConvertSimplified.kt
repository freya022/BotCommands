package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.bot.switches.WikiCommandProfile
import java.util.concurrent.TimeUnit

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:convert_simplified-kotlin]
@Command
class SlashConvertSimplifiedKotlin : ApplicationCommand() {
    @JDASlashCommand(name = "convert_simplified", description = "Convert time to another unit")
    fun onSlashConvertSimplified(
        event: GuildSlashEvent,
        @SlashOption(description = "The time to convert") time: Long,
        @SlashOption(description = "The unit to convert from", usePredefinedChoices = true) from: TimeUnit,
        @SlashOption(description = "The unit to convert to", usePredefinedChoices = true) to: TimeUnit
    ) {
        event.reply("${to.convert(time, from)} ${to.name.lowercase()}").queue()
    }
}
// --8<-- [end:convert_simplified-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:convert_simplified-kotlin_dsl]
@Command
class SlashConvertSimplifiedKotlinDsl {
    fun onSlashConvertSimplified(event: GuildSlashEvent, time: Long, from: TimeUnit, to: TimeUnit) {
        event.reply("${to.convert(time, from)} ${to.name.lowercase()}").queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
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