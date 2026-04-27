package io.github.freya022.botcommands.internal.commands.options.builder

import io.github.freya022.botcommands.api.commands.options.builder.CommandOptionBuilder
import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.internal.core.options.builder.OptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

abstract class CommandOptionBuilderImpl(
    optionParameter: OptionParameter
) : OptionBuilderImpl(optionParameter),
    CommandOptionBuilder {

    /**
     * Switch managed by the vararg aggregates
     *
     * @see Option.isOptionalOrNullable
     */
    var isOptional: Boolean? = null
}
