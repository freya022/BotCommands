package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.annotations.*
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.autocomplete.AutocompleteInfoContainer
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*
import kotlin.reflect.KFunction

class SlashCommandOptionBuilder internal constructor(
    private val context: BContext,
    internal val commandBuilder: SlashCommandBuilder,
    optionParameter: OptionParameter,
    val optionName: String
): ApplicationCommandOptionBuilder(optionParameter) {
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
    var description: String? = null
        set(value) {
            require(value == null || value.isNotBlank()) { "Description cannot be blank" }
            field = value
        }

    /**
     * Enables using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * **Note:** Predefined choices can still be overridden by [ApplicationCommand.getOptionChoices].
     *
     * @return `true` to enable using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @see SlashOption.usePredefinedChoices
     */
    var usePredefinedChoices: Boolean = false

    /**
     * The option's choices.
     *
     * The choices returned by this method will have their name localized
     * if they are present in the [localization bundles][BApplicationConfigBuilder.addLocalizations].
     *
     * @see SlashParameterResolver.getPredefinedChoices
     *
     * @see ApplicationCommand.getOptionChoices
     */
    var choices: List<Choice>? = null

    /**
     * Sets the minimum and maximum values on the specified option.
     *
     * **Note:** This is only for floating point number types!
     *
     * @see DoubleRange
     * @see LongRange
     */
    var valueRange: ValueRange? = null

    /**
     * Sets the minimum and maximum string length on the specified option.
     *
     * **Note:** This is only for string types!
     *
     * @see Length
     */
    var lengthRange: LengthRange? = null

    /**
     * Sets the desired channel types for this option.
     *
     * You can alternatively use a specific channel type,
     * such as [TextChannel] to automatically restrict the channel type.
     *
     * @see ChannelTypes
     */
    var channelTypes: EnumSet<ChannelType>? = null

    internal var autocompleteInfo: AutocompleteInfo? = null
        private set

    /**
     * Declares an autocomplete function for slash command options.
     *
     * The name of the handler must be unique,
     * I recommend naming them like: `YourClassSimpleName: AutocompletedField` such as `SlashTag: tagName`.
     *
     * Requirements:
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
     * **Requirement:** The declaring class must be annotated with [@Handler][Handler] or be in an existing [@Command][Command] class.
     *
     * @see SlashOption.autocomplete
     */
    fun autocomplete(name: String, function: KFunction<Collection<Any>>, block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfo = AutocompleteInfoBuilder(context, name, function).apply(block).build()
    }

    /**
     * Uses an existing autocomplete handler with the specified [name][AutocompleteHandler.name].
     *
     * Must match a method annotated with [AutocompleteHandler] with the same name in it.
     *
     * @see AutocompleteHandler @AutocompleteHandler
     */
    fun autocompleteReference(name: String) {
        autocompleteInfo = context.getService<AutocompleteInfoContainer>()[name] ?: throwUser("Unknown autocomplete handler: $name")
    }
}
