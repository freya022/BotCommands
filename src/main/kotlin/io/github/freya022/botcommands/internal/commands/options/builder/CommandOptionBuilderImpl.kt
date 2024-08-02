package io.github.freya022.botcommands.internal.commands.options.builder

import io.github.freya022.botcommands.api.commands.options.builder.CommandOptionBuilder
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class CommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter
) : OptionBuilderImpl(optionParameter),
    CommandOptionBuilder {

    /**
     * Switch managed by the vararg aggregates
     *
     * @see OptionImpl.isOptionalOrNullable
     */
    internal var isOptional: Boolean? = null
}