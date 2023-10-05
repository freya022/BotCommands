package io.github.freya022.bot.commands.slash

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.parameters.Resolvers
import io.github.freya022.bot.switches.WikiCommandProfile
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import java.util.concurrent.TimeUnit

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:convert-kotlin]
@Command
class SlashConvertKotlin : ApplicationCommand() {
    override fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionName: String): List<Choice> {
        if (commandPath.name == "convert") {
            if (optionName == "from" || optionName == "to") {
                return listOf(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
                    // The Resolvers class helps us by providing resolvers for any enum type.
                    // We're just using the helper method to change an enum value to a more natural name.
                    .map { Choice(Resolvers.toHumanName(it), it.name) }
            }
        }

        return super.getOptionChoices(guild, commandPath, optionName)
    }

    @JDASlashCommand(name = "convert", description = "Convert time to another unit")
    fun onSlashConvert(
        event: GuildSlashEvent,
        @SlashOption(description = "The time to convert") time: Long,
        @SlashOption(description = "The unit to convert from") from: TimeUnit,
        @SlashOption(description = "The unit to convert to") to: TimeUnit
    ) {
        event.reply("${to.convert(time, from)} ${to.name.lowercase()}").queue()
    }
}
// --8<-- [end:convert-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:convert-kotlin_dsl]
@Command
class SlashConvertKotlinDsl {
    fun onSlashConvert(event: GuildSlashEvent, time: Long, from: TimeUnit, to: TimeUnit) {
        event.reply("${to.convert(time, from)} ${to.name.lowercase()}").queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("convert", function = ::onSlashConvert) {
            description = "Convert time to another unit"

            option("time") {
                description = "The time to convert"
            }

            option("from") {
                description = "The unit to convert from"

                choices = listOf(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
                    // The Resolvers class helps us by providing resolvers for any enum type.
                    // We're just using the helper method to change an enum value to a more natural name.
                    .map { Choice(Resolvers.toHumanName(it), it.name) }
            }

            option("to") {
                description = "The unit to convert to"

                choices = listOf(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
                    // The Resolvers class helps us by providing resolvers for any enum type.
                    // We're just using the helper method to change an enum value to a more natural name.
                    .map { Choice(Resolvers.toHumanName(it), it.name) }
            }
        }
    }
}
// --8<-- [end:convert-kotlin_dsl]