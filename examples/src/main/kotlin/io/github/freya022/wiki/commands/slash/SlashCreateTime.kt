package io.github.freya022.wiki.commands.slash

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.wiki.switches.WikiCommandProfile
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.Instant

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:create_time-kotlin]
@Command
class SlashCreateTimeKotlin : ApplicationCommand() {
    override fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?,
        commandPath: CommandPath,
        optionName: String,
        parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        if (commandPath.name == "create_time") {
            if (optionName == "timestamp") {
                // Create a snapshot of the instant the command was created
                val now = Instant.now()
                // Give back the instant snapshot, as this will be called every time the command runs
                return ApplicationGeneratedValueSupplier { now }
            }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType)
    }

    @JDASlashCommand(name = "create_time", description = "Shows the creation time of this command")
    fun onSlashCreateTime(
        event: GuildSlashEvent,
        @GeneratedOption timestamp: Instant
    ) {
        event.reply("I was created on ${TimeFormat.DATE_TIME_SHORT.format(timestamp)}").queue()
    }
}
// --8<-- [end:create_time-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:create_time-kotlin_dsl]
@Command
class SlashCreateTimeKotlinDsl {
    fun onSlashCreateTime(event: GuildSlashEvent, timestamp: Instant) {
        event.reply("I was created on ${TimeFormat.DATE_TIME_SHORT.format(timestamp)}").queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("create_time", function = ::onSlashCreateTime) {
            description = "Shows the creation time of this command"

            // Create a snapshot of the instant the command was created
            val now = Instant.now()
            generatedOption("timestamp") {
                // Give back the instant snapshot, as this will be called every time the command is run
                return@generatedOption now
            }
        }
    }
}
// --8<-- [end:create_time-kotlin_dsl]