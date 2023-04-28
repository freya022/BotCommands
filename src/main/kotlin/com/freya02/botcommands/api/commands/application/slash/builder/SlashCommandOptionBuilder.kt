package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.GuildApplicationSettings
import com.freya02.botcommands.api.commands.application.LengthRange
import com.freya02.botcommands.api.commands.application.ValueRange
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteInfoContainer
import com.freya02.botcommands.internal.parameters.MultiParameter
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import kotlin.reflect.KFunction

class SlashCommandOptionBuilder(
    private val context: BContextImpl,
    multiParameter: MultiParameter,
    optionName: String
): ApplicationCommandOptionBuilder(multiParameter, optionName) {
    var description: String = "No description"

    @Deprecated("Replaced with aggregates")
    var varArgs: Int = -1
    @Deprecated("Replaced with aggregates")
    var requiredVarArgs: Int = 0
        set(value) {
            Checks.check(value <= varArgs, "Cannot have more required varargs than there are varargs, required $value out of $varArgs")
            field = value
        }

    /**
     * Required for [SlashParameterResolver.getPredefinedChoices] to be used.
     *
     * **Note:** Predefined choices can still be overridden by [GuildApplicationSettings.getOptionChoices].
     */
    var usePredefinedChoices: Boolean = false
    var choices: List<Choice>? = null

    var valueRange: ValueRange? = null
    var lengthRange: LengthRange? = null
    var channelTypes: EnumSet<ChannelType>? = null

    var autocompleteInfo: AutocompleteInfo? = null
        private set

    /**
     * Name must be unique
     *
     * Recommended naming: `ClassSimpleName: AutocompletedField`
     *
     * Example: `SlashTag: tagName`
     */
    fun autocomplete(name: String, function: KFunction<Collection<*>>, block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfo = AutocompleteInfoBuilder(context, name, function).apply(block).build()
    }

    fun autocompleteReference(name: String) {
        autocompleteInfo = context.getService<AutocompleteInfoContainer>()[name] ?: throwUser("Unknown autocomplete handler: $name")
    }
}
