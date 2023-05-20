package com.freya02.botcommands.test_kt.commands.user

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.context.user.GuildUserEvent
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.parameters.ParameterType
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@CommandMarker
class UserContextInfo : ApplicationCommand() {
    override fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?,
        commandPath: CommandPath,
        optionName: String,
        parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        if (optionName == "user_tag") {
            return ApplicationGeneratedValueSupplier {
                it as UserContextInteractionEvent

                it.target.asTag
            }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType)
    }

    @JDAUserCommand(scope = CommandScope.GUILD, name = "User info (annotated)")
    fun onUserContextInfo(
        event: GuildUserEvent,
        @AppOption user: User,
        @GeneratedOption userTag: String
    ) {
        event.reply_("Tag of user ID ${user.id}: $userTag", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.userCommand("User info", CommandScope.GUILD, ::onUserContextInfo) {
            option("user")

            generatedOption("userTag") {
                it as UserContextInteractionEvent

                it.target.asTag
            }
        }
    }
}