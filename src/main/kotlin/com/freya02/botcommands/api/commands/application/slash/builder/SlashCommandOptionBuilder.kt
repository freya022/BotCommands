package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GuildApplicationSettings
import com.freya02.botcommands.api.commands.application.LengthRange
import com.freya02.botcommands.api.commands.application.ValueRange
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.annotations.*
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteInfoContainer
import com.freya02.botcommands.internal.parameters.OptionParameter
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import java.util.*
import kotlin.reflect.KFunction

class SlashCommandOptionBuilder internal constructor(
    private val context: BContextImpl,
    optionParameter: OptionParameter,
    val optionName: String
): ApplicationCommandOptionBuilder(optionParameter) {
    /**
     * **Annotation equivalent:** [SlashOption.description]
     *
     * @see SlashOption.usePredefinedChoices
     */
    var description: String = "No description"

    /**
     * Required for [SlashParameterResolver.getPredefinedChoices] to be used.
     *
     * **Note:** Predefined choices can still be overridden by [GuildApplicationSettings.getOptionChoices].
     *
     * **Annotation equivalent:** [SlashOption.usePredefinedChoices]
     *
     * @see SlashOption.usePredefinedChoices
     */
    var usePredefinedChoices: Boolean = false

    /**
     * **Equivalent for annotated commands:** [ApplicationCommand.getOptionChoices]
     *
     * @see ApplicationCommand.getOptionChoices
     */
    var choices: List<Choice>? = null

    /**
     * **Annotation equivalents:**
     * - [DoubleRange]
     * - [LongRange]
     *
     * @see DoubleRange
     * @see LongRange
     */
    var valueRange: ValueRange? = null

    /**
     * **Annotation equivalent:** [Length]
     *
     * @see Length
     */
    var lengthRange: LengthRange? = null

    /**
     * **Annotation equivalent:** [ChannelTypes]
     *
     * @see ChannelTypes
     */
    var channelTypes: EnumSet<ChannelType>? = null

    internal var autocompleteInfo: AutocompleteInfo? = null
        private set

    /**
     * Creates an autocomplete handler
     *
     * The name of the handler must be unique,
     * I recommend naming them like: `YourClassSimpleName: AutocompletedField`<br>
     * Example: `SlashTag: tagName`
     *
     * **Annotation equivalent:** [SlashOption.autocomplete]
     *
     * @see SlashOption.autocomplete
     */
    fun autocomplete(name: String, function: KFunction<Collection<Any>>, block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfo = AutocompleteInfoBuilder(context, name, function).apply(block).build()
    }

    /**
     * Uses an existing autocomplete handler with the specified [name][AutocompleteHandler.name]
     *
     * @see AutocompleteHandler @AutocompleteHandler
     */
    fun autocompleteReference(name: String) {
        autocompleteInfo = context.getService<AutocompleteInfoContainer>()[name] ?: throwUser("Unknown autocomplete handler: $name")
    }
}
