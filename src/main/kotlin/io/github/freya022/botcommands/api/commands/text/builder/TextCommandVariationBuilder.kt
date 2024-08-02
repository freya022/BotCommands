package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.options.builder.TextOptionRegistry
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.CommandDSL

@CommandDSL
interface TextCommandVariationBuilder : IDeclarationSiteHolderBuilder,
                                        TextOptionRegistry {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * Set of filters preventing this command from executing.
     *
     * @see TextCommandFilter
     * @see TextCommandRejectionHandler
     */
    val filters: MutableList<TextCommandFilter<*>>

    /**
     * Short description of the command displayed in the built-in help command,
     * below the command usage.
     *
     * @see JDATextCommandVariation.description
     */
    var description: String?

    /**
     * Usage string for this command variation,
     * the built-in help command already sets the prefix and command name, with a space at the end.
     *
     * If not set, the built-in help command will generate a string out of the options.
     *
     * @see JDATextCommandVariation.usage
     */
    var usage: String?

    /**
     * Example command for this command variation,
     * the built-in help command already sets the prefix and command name, with a space at the end.
     *
     * If not set, the built-in help command will generate a string out of the options.
     *
     * @see JDATextCommandVariation.example
     */
    var example: String?
}

/**
 * Convenience extension to load an [TextCommandFilter] service.
 *
 * Typically used as `filters += filter<MyApplicationCommandFilter>()`
 */
inline fun <reified T : TextCommandFilter<*>> TextCommandVariationBuilder.filter(): T {
    return context.getService<T>()
}
