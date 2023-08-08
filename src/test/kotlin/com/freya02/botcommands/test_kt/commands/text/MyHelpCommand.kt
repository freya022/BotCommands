package com.freya02.botcommands.test_kt.commands.text

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.core.service.ConditionalServiceChecker
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.core.service.annotations.ServicePriority
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo

object HelpCondition : ConditionalServiceChecker {
    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? {
        return "No help handler for you"
    }
}

@BService(priority = -1)
@ConditionalService(HelpCondition::class)
class MyHelpCommand : IHelpCommand {
    //Only triggered when an existing command is misused
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}

@BService
@ServicePriority(Int.MAX_VALUE)
@ConditionalService(HelpCondition::class)
class MyHelpCommand2 : IHelpCommand {
    //Only triggered when an existing command is misused
    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}