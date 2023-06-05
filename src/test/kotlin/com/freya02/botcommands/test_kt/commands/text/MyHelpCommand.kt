package com.freya02.botcommands.test_kt.commands.text

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.core.service.ConditionalServiceChecker
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo

object HelpCondition : ConditionalServiceChecker {
    override fun checkServiceAvailability(context: BContext): String? {
        return "No help handler for you"
    }
}

@BService
@ServiceType(IHelpCommand::class)
@ConditionalService(HelpCondition::class)
class MyHelpCommand : IHelpCommand {
    //Only triggered when an existing command is misused
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}

@BService
@ServiceType(IHelpCommand::class)
@ConditionalService(HelpCondition::class)
class MyHelpCommand2 : IHelpCommand {
    //Only triggered when an existing command is misused
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}