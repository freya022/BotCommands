package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * Sets a parameter as a slash command option from Discord.
 *
 * See the [Wiki about inferred option names](https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/) for more details.
 *
 * @see Optional @Optional
 * @see Nullable @Nullable
 *
 * @see LongRange @LongRange
 * @see DoubleRange @DoubleRange
 * @see Length @Length
 *
 * @see ChannelTypes @ChannelTypes
 *
 * @see AutocompleteHandler @AutocompleteHandler
 * @see VarArgs @VarArgs
 *
 * @see SlashCommandBuilder.option DSL equivalent
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SlashOption(
    /**
     * Name of the option.<br>
     * Must follow the Discord specifications,
     * see the [OptionData] constructor for details.
     *
     * This can be localized, see [LocalizationFunction] on how options are mapped.
     *
     * This is optional if the parameter name is found,
     * see [the wiki](https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/) for more details.
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
     * **Note:** Predefined choices can still be overridden by [GuildApplicationSettings.getOptionChoices].
     *
     * @return `true` to enable using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @see SlashCommandOptionBuilder.usePredefinedChoices DSL equivalent
     */
    val usePredefinedChoices: Boolean = false,

    /**
     * Name of the autocomplete handler.<br>
     * Must match a method annotated with [AutocompleteHandler] with the same name in it
     *
     * @see SlashCommandOptionBuilder.autocompleteReference DSL equivalent
     * @see SlashCommandOptionBuilder.autocomplete Declaring an autocomplete handler using the DSL
     */
    val autocomplete: String = ""
)
