package io.github.freya022.botcommands.api.commands.application.slash.options.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.annotations.DoubleRange
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import kotlin.reflect.KFunction

interface SlashCommandOptionBuilder : ApplicationCommandOptionBuilder {
    //TODO docs
    val optionName: String

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
     * @see SlashOption.usePredefinedChoices
     */
    var description: String?

    /**
     * Enables using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @return `true` to enable using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @throws IllegalStateException If [choices] are set.
     *
     * @see SlashOption.usePredefinedChoices
     */
    var usePredefinedChoices: Boolean

    /**
     * The option's choices.
     *
     * The choices returned by this method will have their name localized
     * if they are present in the [localization bundles][BApplicationConfigBuilder.addLocalizations].
     *
     * @see SlashParameterResolver.getPredefinedChoices
     *
     * @throws IllegalStateException If [usePredefinedChoices] is enabled.
     *
     * @see ApplicationCommand.getOptionChoices
     */
    var choices: List<Choice>?

    /**
     * Sets the minimum and maximum values on the specified option.
     *
     * **Note:** This is only for floating point number types!
     *
     * @see DoubleRange
     * @see LongRange
     */
    var valueRange: ValueRange?

    /**
     * Sets the minimum and maximum string length on the specified option.
     *
     * **Note:** This is only for string types!
     *
     * @see Length
     */
    var lengthRange: LengthRange?

    /**
     * Uses an existing autocomplete handler with the specified [name][AutocompleteHandler.name].
     *
     * Must match an autocomplete handler created from a named [@AutocompleteHandler][AutocompleteHandler]
     * or [AutocompleteManager.autocomplete].
     *
     * @see AutocompleteHandler @AutocompleteHandler
     */
    fun autocompleteByName(name: String)

    /**
     * Uses an existing autocomplete handler with the specified function.
     *
     * Must match an autocomplete handler created from [@AutocompleteHandler][AutocompleteHandler]
     * or [AutocompleteManager.autocomplete].
     */
    fun autocompleteByFunction(function: KFunction<Collection<Any>>)
}
