package io.github.freya022.botcommands.internal.commands.application.slash.options.builder

import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoContainer
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.interactions.commands.Command
import kotlin.reflect.KFunction

internal class SlashCommandOptionBuilderImpl internal constructor(
    private val context: BContext,
    internal val commandBuilder: SlashCommandBuilderImpl,
    optionParameter: OptionParameter,
    override val optionName: String
): ApplicationCommandOptionBuilderImpl(optionParameter),
   SlashCommandOptionBuilder {

    override var description: String? = null
        set(value) {
            require(value == null || value.isNotBlank()) { "Description cannot be blank" }
            field = value
        }

    override var usePredefinedChoices: Boolean = false
        set(enable) {
            check(choices == null) {
                "Cannot use predefined choices when choices are set"
            }
            field = enable
        }

    override var choices: List<Command.Choice>? = null
        set(choices) {
            check(!usePredefinedChoices) {
                "Cannot set choices when predefined choices are enabled"
            }
            require(choices == null || choices.isNotEmpty()) {
                "List cannot be empty"
            }
            field = choices
        }

    override var valueRange: ValueRange? = null

    override var lengthRange: LengthRange? = null

    internal var autocompleteInfo: AutocompleteInfoImpl? = null
        private set

    override fun autocompleteByName(name: String) {
        autocompleteInfo = context.getService<AutocompleteInfoContainer>()[name] ?: throwArgument("Unknown autocomplete handler: $name")
    }

    override fun autocompleteByFunction(function: KFunction<*>) {
        autocompleteInfo = context.getService<AutocompleteInfoContainer>()[function]
            ?: throwArgument("No autocomplete handler declared from: ${function.shortSignatureNoSrc}")
    }
}