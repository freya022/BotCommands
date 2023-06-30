package io.github.freya022.bot.commands.text

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo

@BService
@ServiceType(IHelpCommand::class)
class HelpCommand : IHelpCommand {
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("Invalid command syntax !").queue()
    }
}