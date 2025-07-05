package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.interactions.commands.Command

/**
 * Transforms autocomplete results into choices.
 *
 * **Usage**: Register your instance as a service with [BService].
 *
 * @param E Type of the List's elements
 *
 * @see SlashOption.autocomplete
 * @see AutocompleteHandler @AutocompleteHandler
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface AutocompleteTransformer<E> {
    val elementType: Class<E>

    fun apply(e: E): Command.Choice
}
