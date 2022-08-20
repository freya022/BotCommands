package com.freya02.botcommands.test.commands_kt.message

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.parameters.ParameterType
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.MarkdownSanitizer

@CommandMarker
class MessageContextRaw : ApplicationCommand() {
    override fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?,
        commandPath: CommandPath,
        optionName: String,
        parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        if (optionName == "raw_content") {
            return ApplicationGeneratedValueSupplier {
                it as MessageContextInteractionEvent

                MarkdownSanitizer.escape(it.target.contentRaw)
            }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType)
    }

    @JDAMessageCommand(scope = CommandScope.GUILD, name = "Raw content (annotated)")
    fun onMessageContextRaw(
        event: GuildMessageEvent,
        @AppOption message: Message,
        @GeneratedOption rawContent: String
    ) {
        event.reply_("Raw for message ID ${message.id}: $rawContent", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.messageCommand("Raw content", CommandScope.GUILD) {
            option("message")

            generatedOption("rawContent") {
                it as MessageContextInteractionEvent

                MarkdownSanitizer.escape(it.target.contentRaw)
            }

            function = ::onMessageContextRaw
        }
    }
}