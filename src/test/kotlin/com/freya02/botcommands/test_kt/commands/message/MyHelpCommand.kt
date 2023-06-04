package com.freya02.botcommands.test_kt.commands.message

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo

@BService
@ServiceType(IHelpCommand::class)
class MyHelpCommand : IHelpCommand {
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}