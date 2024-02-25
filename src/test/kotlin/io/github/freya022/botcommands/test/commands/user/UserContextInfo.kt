package io.github.freya022.botcommands.test.commands.user

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@Command
class UserContextInfo : ApplicationCommand(), GlobalApplicationCommandProvider {
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

    @JDAUserCommand(scope = CommandScope.GLOBAL, name = "User info (annotated)")
    fun onUserContextInfo(
        event: GlobalUserEvent,
        @ContextOption user: InputUser,
        @GeneratedOption userTag: String
    ) {
        event.reply_("Tag of user ID ${user.id}: $userTag", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.userCommand("User info", CommandScope.GLOBAL, ::onUserContextInfo) {
            option("user")

            generatedOption("userTag") {
                it as UserContextInteractionEvent

                it.target.asTag
            }
        }
    }
}