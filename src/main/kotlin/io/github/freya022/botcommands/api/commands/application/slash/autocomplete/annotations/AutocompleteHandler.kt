package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.SlashParameterResolver
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

/**
 * Uses the method as an autocomplete function for [slash command options][SlashOption].
 *
 * The name of the handler must be unique,
 * I recommend naming them like: `YourClassSimpleName: AutocompletedField` such as `SlashTag: tagName`.
 *
 * Requirements:
 *  - The declaring class must be annotated with [@Handler][Handler] or be an existing [@Command][Command] class.
 *  - The method must be public
 *  - The method must be non-static
 *  - The first parameter must be [CommandAutoCompleteInteractionEvent]
 *
 * The annotated method must returns a [List], which gets processed differently based on its element type:
 *  - `String`, `Long`, `Double`: Makes a choice where `name` == `value`,
 *  uses fuzzy matching to give the best choices first
 *  - `Choice`: Keeps the same choices in the same order, does not do any matching.
 *  - Any type supported by an [AutocompleteTransformer]: Keeps the same order after transformation.
 *
 * You can add more List element types by having a service implementing [AutocompleteTransformer],
 * or with [BApplicationConfigBuilder.registerAutocompleteTransformer].
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
 * Supported parameters are in [ParameterResolver],
 * additional resolvers can be implemented with [SlashParameterResolver].
 *
 * @see SlashOption @SlashOption
 * @see JDASlashCommand @JDASlashCommand
 * @see CacheAutocomplete @CacheAutocomplete
 *
 * @see SlashCommandOptionBuilder.autocomplete DSL equivalent
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
     * Determines if the user input is shown as the first suggestion
     *
     * This allows the user to force his own input more easily
     *
     * **This being `false` does not mean that the bot-provided choices are forced upon the user,
     * autocomplete is never forced, unlike choices**
     *
     * @return `true` if the user's input should be shown, `false` if not
     *
     * @see AutocompleteInfoBuilder.showUserInput DSL equivalent
     */
    val showUserInput: Boolean = true
)
