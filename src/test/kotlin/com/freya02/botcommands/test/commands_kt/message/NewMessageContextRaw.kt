package com.freya02.botcommands.test.commands_kt.message

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.annotations.api.application.annotations.GeneratedOption
import com.freya02.botcommands.annotations.api.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier
import com.freya02.botcommands.api.parameters.ParameterType
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.MarkdownSanitizer

@CommandMarker
class NewMessageContextRaw : ApplicationCommand() {
    override fun getGeneratedValueSupplier(
        guild: Guild,
        commandId: String?,
        commandPath: CommandPath,
        optionName: String,
        parameterType: ParameterType
    ): GeneratedValueSupplier? {
        if (optionName == "raw_content") {
            return GeneratedValueSupplier {
                it as MessageContextInteractionEvent

                MarkdownSanitizer.escape(it.target.contentRaw)
            }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType)
    }

    @JDAMessageCommand(scope = CommandScope.GUILD, name = "Raw content (new)")
    fun onMessageContextRaw(
        event: GuildMessageEvent,
        @AppOption message: Message,
        @GeneratedOption rawContent: String
    ) {
        event.reply_("Raw for message ID ${message.id}: $rawContent", ephemeral = true).queue()
    }
}