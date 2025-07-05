package io.github.freya022.botcommands.internal.commands.application.options

import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.options.CommandParameterImpl
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.reflect.KClass

internal abstract class ApplicationCommandParameterImpl internal constructor(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilderImpl<*>,
    eventType: KClass<out GenericCommandInteractionEvent>
) : CommandParameterImpl(context, optionAggregateBuilder, eventType),
    ApplicationCommandParameter {

    abstract override val nestedAggregatedParameters: List<ApplicationCommandParameterImpl>
}