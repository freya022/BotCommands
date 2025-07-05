package io.github.freya022.botcommands.internal.commands.text.options.builder

import io.github.freya022.botcommands.api.commands.text.options.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.options.builder.CommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class TextCommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
    internal val optionName: String,
) : CommandOptionBuilderImpl(optionParameter),
    TextCommandOptionBuilder {

    override var helpExample: String? = null
    override var isId: Boolean = false
}