package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandParameter
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.CommandParameterImpl
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.reflect.KClass

internal abstract class ApplicationCommandParameterImpl internal constructor(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>,
    eventType: KClass<out GenericCommandInteractionEvent>
) : CommandParameterImpl(context, optionAggregateBuilder, eventType),
    ApplicationCommandParameter {

    abstract override val nestedAggregatedParameters: List<ApplicationCommandParameterImpl>
}