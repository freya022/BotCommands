package io.github.freya022.botcommands.internal.commands.application.context

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandOptionAggregateBuilderImpl
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import kotlin.reflect.KClass

internal abstract class ContextCommandParameterImpl(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilderImpl<*>,
    eventType: KClass<out GenericContextInteractionEvent<*>>
) : ApplicationCommandParameterImpl(context, optionAggregateBuilder, eventType),
    ContextCommandParameter