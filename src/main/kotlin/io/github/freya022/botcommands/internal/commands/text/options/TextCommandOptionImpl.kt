package io.github.freya022.botcommands.internal.commands.text.options

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.api.parameters.resolvers.QuotableTextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.options.CommandOptionImpl
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextCommandOptionBuilderImpl

internal class TextCommandOptionImpl internal constructor(
    override val parent: TextCommandParameter,
    optionBuilder: TextCommandOptionBuilderImpl,
    internal val resolver: TextParameterResolver<*, *>
) : CommandOptionImpl(optionBuilder),
    TextCommandOption {

    override val executable get() = parent.executable

    override val helpName: String = optionBuilder.optionName
    override val helpExample: String? = optionBuilder.helpExample
    override val isId = optionBuilder.isId

    override val isQuotable: Boolean
        get() = resolver is QuotableTextParameterResolver

    val groupCount = resolver.preferredPattern.matcher("").groupCount()

    override fun getResolverHelpExample(event: BaseCommandEvent) =
        resolver.getHelpExample(this, event)
}