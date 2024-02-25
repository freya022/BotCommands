package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

/**
 * Uses the method as an autocomplete function for [slash command options][SlashOption].
 *
 * The name must be unique; I recommend naming them similarly to:
 * `YourClassSimpleName: AutocompletedField` (for example, `SlashTag: tagName`).
 *
 * Requirements:
 *  - The declaring class must be annotated with [@Handler][Handler] or be an existing [@Command][Command] class.
 *  - The method must be public
 *  - The method must be non-static
 *  - The first parameter must be [CommandAutoCompleteInteractionEvent]
 *
 * The annotated method must return a [Collection], which gets processed differently based on its element type:
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
 *  - The parameters must be named the same as in the original slash command.
 *  - The parameters of the same name must have the same type as the original slash command.
 *  - The parameters can be in any order, include custom parameters or omit options.
 *
 * **Note:** Parameters refers to method parameters, not Discord options.
 *
 * ## Registering more option types
 * Supported types are in [ParameterResolver],
 * additional types can be added by implementing [SlashParameterResolver].
 *
 * @see SlashOption @SlashOption
 * @see JDASlashCommand @JDASlashCommand
 * @see CacheAutocomplete @CacheAutocomplete
 *
 * @see AutocompleteHandlerProvider DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutocompleteHandler(
    /**
     * Sets the name of the autocomplete handler, **it must be the same as what you set in [SlashOption.autocomplete]**.
     *
     * The name must be unique, another handler cannot share it.
     */
    @get:JvmName("value") val name: String,

    /**
     * Sets the [autocomplete mode][AutocompleteMode].
     *
     * **This is only usable on collection return types of String, Double and Long**
     *
     * @see AutocompleteMode
     * @see AutocompleteInfoBuilder.mode DSL equivalent
     */
    val mode: AutocompleteMode = AutocompleteMode.FUZZY,

    /**
     * Whether the user input is shown as the first suggestion.
     *
     * **Note:** `false` does not mean that the bot-provided choices are forced upon the user,
     * autocomplete is never forced, unlike choices.
     *
     * **Default:** `false`
     *
     * @see AutocompleteInfoBuilder.showUserInput DSL equivalent
     */
    val showUserInput: Boolean = false
)
