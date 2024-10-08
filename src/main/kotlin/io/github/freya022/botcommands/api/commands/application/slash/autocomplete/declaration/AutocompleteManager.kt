package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction

/**
 * Allows programmatic declaration of autocomplete handlers using [AutocompleteHandlerProvider] in Kotlin source code.
 *
 * @see AutocompleteHandlerProvider
 */
interface AutocompleteManager {
    val context: BContext

    /**
     * Declares an autocomplete function for slash command options.
     *
     * The name must be unique; I recommend naming them similarly to:
     * `YourClassSimpleName: AutocompletedField` (for example, `SlashTag: tagName`).
     *
     * Requirements:
     *  - The method must be public
     *  - The method must be non-static
     *  - The first parameter must be [CommandAutoCompleteInteractionEvent]
     *
     * The given function must return a [Collection], which gets processed differently based on its element type:
     *  - `String`, `Long`, `Double`: Makes a choice where `name` == `value`,
     *  uses fuzzy matching to give the best choices first
     *  - `Choice`: Keeps the same choices in the same order, does not do any matching.
     *  - Any type supported by an [AutocompleteTransformer]: Keeps the same order after transformation.
     *
     * You can add support for more element types with [autocomplete transformers][AutocompleteTransformer].
     *
     * ## State-aware autocomplete
     * You can also use state-aware autocomplete,
     * meaning you can get what the user has inserted in other options.
     *
     * The requirements are as follows:
     *  - The parameters can only be the event + parameters present on the slash command
     *  - They must be of the same name, type and nullability as the original slash command
     *  - They can only include parameters from the slash command itself, not from nested parameters (aka aggregates)
     *
     * **Note:** Parameters refers to method parameters, not Discord options.
     *
     * @see AutocompleteTransformer
     *
     * @see AutocompleteHandler @AutocompleteHandler
     */
    fun autocomplete(function: KFunction<Collection<Any>>, name: String? = null, block: AutocompleteInfoBuilder.() -> Unit = {})
}