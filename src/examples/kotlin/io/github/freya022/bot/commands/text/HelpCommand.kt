package io.github.freya022.bot.commands.text

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfo

@BService
class HelpCommand : IHelpCommand {
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("Invalid command syntax !").queue()
    }
}