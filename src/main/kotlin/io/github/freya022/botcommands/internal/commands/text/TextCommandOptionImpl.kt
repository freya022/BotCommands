package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.QuotableTextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.CommandOptionImpl
import io.github.freya022.botcommands.internal.commands.text.builder.TextCommandOptionBuilderImpl

internal class TextCommandOptionImpl internal constructor(
    override val context: BContext,
    override val command: TextCommandVariation,
    optionBuilder: TextCommandOptionBuilderImpl,
    internal val resolver: TextParameterResolver<*, *>
) : CommandOptionImpl(optionBuilder),
    TextCommandOption {

    override val helpName: String = optionBuilder.optionName
    override val helpExample: String? = optionBuilder.helpExample
    override val isId = optionBuilder.isId

    override val isQuotable: Boolean
        get() = resolver is QuotableTextParameterResolver

    val groupCount = resolver.preferredPattern.matcher("").groupCount()

    override fun getResolverHelpExample(event: BaseCommandEvent) =
        resolver.getHelpExample(this, event)
}