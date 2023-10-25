package io.github.freya022.botcommands.test.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandRejectionHandler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
class MyCommandFilters : TextCommandFilter, ApplicationCommandFilter {
    override suspend fun checkSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Any? = check(event.guildChannel)

    override suspend fun checkSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Any? = check(event.guildChannel)

    private fun check(channel: Channel): String? {
        if (channel.idLong != 722891685755093076) {
            return "Can only run commands in <#722891685755093076>"
        }
        return null
    }
}

@BService
class MyCommandRejectionHandler : TextCommandRejectionHandler, ApplicationCommandRejectionHandler {
    override suspend fun handleSuspend(
        event: MessageReceivedEvent,
        variation: TextCommandVariation,
        args: String,
        userData: Any
    ) {
        event.message.reply(userData as String).await()
    }

    override suspend fun handleSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo,
        userData: Any
    ) {
        event.reply_(userData as String, ephemeral = true).await()
    }
}