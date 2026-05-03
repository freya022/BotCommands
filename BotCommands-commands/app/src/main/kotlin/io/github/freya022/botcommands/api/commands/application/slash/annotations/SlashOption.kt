package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.SlashOptionChoiceProvider
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashOptionRegistry
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Sets a parameter as a slash command option from Discord.
 *
 * The supported data types can be seen in [SlashParameterResolver], more types can be supported by implementing it.
 *
 * ### Display name
 * Option names can be inferred from the parameter's name,
 * see [the wiki](https://bc.freya02.dev/3.X/setup/parameter-names/)
 * for more details.
 *
 * ### Order
 * Options have the same order on Discord and the method,
 * however, required options must be placed first (i.e., not null, and without a default value).
 *
 * If the options are unordered, they will be automatically ordered and logged.
 *
 * ### Modifiers
 * - [@LongRange][LongRange]/[@DoubleRange][DoubleRange]: Changes the allowed value range
 * - [@Length][Length]: Changes the allowed string length
 * - [@ChannelTypes][ChannelTypes]: Changes the allowed channel types, see the `GuildChannel` support on [SlashParameterResolver]
 * - Any `@Nullable` annotation: Makes the parameter optional
 *
 * ### Choices
 * Choices can be added by either [their parameter resolver][SlashParameterResolver.getPredefinedChoices],
 * or by the application command itself from implementing [SlashOptionChoiceProvider].
 *
 * ### Varargs
 * You can set this parameter to be a vararg using [@VarArgs][VarArgs], multiple varargs are allowed.
 *
 * @see Aggregate @Aggregate
 *
 * @see AutocompleteHandler @AutocompleteHandler
 *
 * @see SlashOptionRegistry.option DSL equivalent
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SlashOption(
    /**
     * Name of the option.<br>
     * Must follow the Discord specifications,
     * see the [OptionData] constructor for details.
     *
     * This can be localized, see [LocalizationFunction] on how options are mapped.
     *
     * This is optional if the parameter name is found,
     * see [the wiki](https://bc.freya02.dev/3.X/setup/parameter-names/) for more details.
     */
    val name: String = "",

    /**
     * Description of the option.<br>
     * Must follow the Discord specifications,
     * see the [OptionData] constructor for details.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.<br>
     * If none is found then it is defaulted to `No Description`.
     *
     * This can be localized, see [LocalizationFunction] on how options are mapped, example: `ban.options.user.description`.
     * This is optional if the parameter is not a slash command parameter.
     *
     * @see SlashCommandOptionBuilder.description DSL equivalent
     */
    val description: String = "",

    /**
     * Enables using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * **Note:** Predefined choices can still be overridden by the [SlashOptionChoiceProvider].
     *
     * @return `true` to enable using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @see SlashCommandOptionBuilder.usePredefinedChoices DSL equivalent
     */
    val usePredefinedChoices: Boolean = false,

    /**
     * Name of the autocomplete handler.
     *
     * Must match an autocomplete handler created from [@AutocompleteHandler][AutocompleteHandler] or [AutocompleteHandlerProvider].
     *
     * @see AutocompleteTransformer
     * @see SlashCommandOptionBuilder.autocompleteByName DSL equivalent
     * @see AutocompleteHandlerProvider Declaring an autocomplete handler using the DSL
     */
    val autocomplete: String = ""
)
