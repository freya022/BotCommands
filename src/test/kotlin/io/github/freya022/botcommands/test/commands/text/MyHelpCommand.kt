package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.service.annotations.ServicePriority
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfo

object HelpCondition : ConditionalServiceChecker {
    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? {
        return "No help handler for you"
    }
}

@ServiceName("helpCommand") // Just to make sure there is no name collision with the built-in help
@BService(priority = -1)
@ConditionalService(HelpCondition::class)
class MyHelpCommand : IHelpCommand {
    //Only triggered when an existing command is misused
    override suspend fun onInvalidCommandSuspend(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}

@BService
@ServicePriority(Int.MAX_VALUE)
@ConditionalService(HelpCondition::class)
class MyHelpCommand2 : IHelpCommand {
    //Only triggered when an existing command is misused
    override suspend fun onInvalidCommandSuspend(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}