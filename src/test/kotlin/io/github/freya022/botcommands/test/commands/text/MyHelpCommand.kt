package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.service.annotations.ServicePriority
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

object HelpCondition : ConditionalServiceChecker, Condition {
    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
        return "No help handler for you"
    }

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return false
    }
}

@ServiceName("helpCommand") // Just to make sure there is no name collision with the built-in help
@BService(priority = -1)
@ConditionalService(HelpCondition::class)
@Conditional(HelpCondition::class)
class MyHelpCommand : IHelpCommand {
    //Only triggered when an existing command is misused
    override suspend fun onInvalidCommandSuspend(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}

@BService
@ServicePriority(Int.MAX_VALUE)
@ConditionalService(HelpCondition::class)
@Conditional(HelpCondition::class)
class MyHelpCommand2 : IHelpCommand {
    //Only triggered when an existing command is misused
    override suspend fun onInvalidCommandSuspend(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        event.respond("My help content").queue()
    }
}