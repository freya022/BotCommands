package io.github.freya022.botcommands.internal.commands.application.context.options

import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.options.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionAggregateBuilderImpl
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import kotlin.reflect.KClass

internal abstract class ContextCommandParameterImpl(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilderImpl<*>,
    eventType: KClass<out GenericContextInteractionEvent<*>>
) : ApplicationCommandParameterImpl(context, optionAggregateBuilder, eventType),
    ContextCommandParameter