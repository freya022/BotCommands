package io.github.freya022.botcommands.internal.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandParameter
import io.github.freya022.botcommands.internal.commands.application.options.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform

internal class SlashCommandParameterImpl internal constructor(
    override val executable: SlashCommandInfoImpl,
    builder: SlashCommandBuilderImpl,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl
) : ApplicationCommandParameterImpl(executable.context, optionAggregateBuilder, GlobalSlashEvent::class),
    SlashCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        SlashCommandParameterImpl(executable, builder, it as SlashCommandOptionAggregateBuilderImpl)
    }

    override val options = CommandOptions.transform(
        this,
        ApplicationCommandResolverData(builder),
        optionAggregateBuilder,
        ::SlashCommandOptionImpl
    )
}
