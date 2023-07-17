package com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.annotations.Handler
import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

/**
 * Uses the method as an autocomplete function for [slash command options][SlashOption].
 *
 * Requirements:
 *  - The method must be public
 *  - The method must be non-static
 *  - The first parameter must be [CommandAutoCompleteInteractionEvent]
 *
 * The annotated method returns a [List] of the following types:
 *  - String, Long, Double -> Choice(String, String), uses fuzzy matching to give the best choices first
 *  - Choice -> keep the same choice, same order as provided
 *  - T (the type of your choice) -> Transformer -> Choice, same order as provided<br>
 *    i.e. this means that an [AutocompleteTransformer] will be used to transform the items of your list, while preserving the order.
 *
 * You can add more List element types with [BApplicationConfigBuilder.registerAutocompleteTransformer]
 *
 * ## State aware autocomplete
 * You can also use state-aware autocomplete,
 * as you can retrieve parameters the user has **already** entered.
 *
 * The requirements are as follows:
 *  - The parameters must be named the same as in the original slash command
 *  - The parameters of the same name must have the same type as the original slash command
 *
 * You are free to add custom options, or omit parameters.
 *
 * **Requirement:** The declaring class must be annotated with [@Handler][Handler] or be in an existing [@Command][Command] class.
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
    val name: String,

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
     * **This being `false` does not mean that the bot-provided choices are forced upon the user, autocomplete is never forced, unlike choices**
     *
     * @return `true` if the user's input should be shown, `false` if not
     *
     * @see AutocompleteInfoBuilder.showUserInput DSL equivalent
     */
    val showUserInput: Boolean = true
)
