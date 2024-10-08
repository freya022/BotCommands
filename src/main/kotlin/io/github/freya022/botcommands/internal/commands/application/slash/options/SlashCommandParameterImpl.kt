package io.github.freya022.botcommands.internal.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.options.transform

internal class SlashCommandParameterImpl internal constructor(
    context: BContext,
    command: SlashCommandInfoImpl,
    builder: SlashCommandBuilderImpl,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl
) : AbstractSlashCommandParameter(context, command, optionAggregateBuilder),
    SlashCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        SlashCommandParameterImpl(context, command, builder, it as SlashCommandOptionAggregateBuilderImpl)
    }

    override val options = transformOptions(this, builder, optionAggregateBuilder, ::SlashCommandOptionImpl)
}
