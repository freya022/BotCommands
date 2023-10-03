package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.annotations.Optional
import com.freya02.botcommands.api.commands.application.GuildApplicationSettings
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import org.jetbrains.annotations.Nullable

//TODO keep an eye out for this wiki link
/**
 * Sets a parameter as a slash command option from Discord.
 *
 * Option names can be inferred from the parameter's name,
 * see the [Wiki about inferred option names](https://freya022.github.io/BotCommands-Wiki/3.X/using-commands/Inferred-option-names/)
 * for more details.
 *
 * Choices can be added by either [their parameter resolver][SlashParameterResolver.getPredefinedChoices],
 * or the [application command itself][GuildApplicationSettings.getOptionChoices].
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
     * see [the wiki](https://freya022.github.io/BotCommands-Wiki/3.X/using-commands/Inferred-option-names/) for more details.
     */
    //TODO keep an eye out for this wiki link
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
    val description: String = SlashCommandBuilder.DEFAULT_DESCRIPTION,

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
     * Name of the autocomplete handler.
     *
     * Must match a method annotated with [AutocompleteHandler] with the same name in it.
     *
     * @see SlashCommandOptionBuilder.autocompleteReference DSL equivalent
     * @see SlashCommandOptionBuilder.autocomplete Declaring an autocomplete handler using the DSL
     */
    val autocomplete: String = ""
)
