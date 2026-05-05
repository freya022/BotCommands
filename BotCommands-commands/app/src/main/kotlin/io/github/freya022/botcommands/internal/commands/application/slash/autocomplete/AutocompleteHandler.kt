package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.arrayOfSize
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.options.AutocompleteCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.slash.getSlashOptions
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType as JDAOptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * Autocomplete handlers are per-option,
 * they can share the same [AutocompleteInfo] but act on different commands with different sets of options,
 * as long as both the command function and the autocomplete function share parameters of the same name and type.
 *
 * Due to the Many-to-One associations, the handler cannot store any state, and must be stored in [AutocompleteInfo].
 */
internal class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfoImpl,
    private val autocompleteInfo: AutocompleteInfoImpl
) : ExecutableMixin {

    override val context: BContext get() = slashCommandInfo.context

    override val eventFunction = autocompleteInfo.eventFunction
    override val parameters: List<AutocompleteCommandParameterImpl>

    init {
        val autocompleteParameters = function.nonEventParameters
        val unmappedParameters = autocompleteParameters.map { it.findDeclarationName() } - slashCommandInfo.parameters.mapTo(hashSetOf()) { it.name }
        require(unmappedParameters.isEmpty()) {
            val autocompleteSignature = function.getSignature(parameterNames = unmappedParameters)
            """
                Could not find parameters declared as $unmappedParameters
                Required by autocomplete function $autocompleteSignature
                From slash command ${slashCommandInfo.function.shortSignature}
            """.trimIndent()
        }

        this.parameters = autocompleteParameters.map { autocompleteParameter ->
            val rootSlashParameter = slashCommandInfo.parameters.single { it.name == autocompleteParameter.name }
            AutocompleteCommandParameterImpl(rootSlashParameter, function)
        }
    }

    internal fun invalidate() {
        autocompleteInfo.invalidate()
    }

    internal suspend fun handle(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        return autocompleteInfo.cache.retrieveAndCall(this, event, this::generateChoices)
    }

    private suspend fun generateChoices(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val objects = getSlashOptions(event, parameters)
            ?: return emptyList() //Autocomplete was triggered without all the required parameters being present

        val actualChoices: MutableList<Command.Choice> = arrayOfSize(25)
        val suppliedChoices = autocompleteInfo.choiceSupplier.apply(event, autocompleteInfo.methodAccessor.callSuspend(objects))
        val autoCompleteQuery = event.focusedOption

        //If something is typed but there are no choices, don't display user input
        if (autocompleteInfo.showUserInput && autoCompleteQuery.value.isNotBlank() && suppliedChoices.isNotEmpty()) {
            autoCompleteQuery.value
                .asChoice(autoCompleteQuery.type)
                ?.let { //Could be null if option mapping is malformed
                    actualChoices.add(it)
                }
        }

        //Fill with choices until max
        actualChoices.addAll(suppliedChoices.take(OptionData.MAX_CHOICES - actualChoices.size))

        return actualChoices
    }

    internal fun validateParameters() {
        val compositeKeys = autocompleteInfo.cache.compositeKeys
        if (compositeKeys.isNotEmpty()) {
            val optionDiscordNames = slashCommandInfo.parameters
                .flatMap { it.allOptions }
                .filterIsInstance<SlashCommandOption>()
                .map { it.discordName }
            for (compositeKey in compositeKeys) {
                require(compositeKey in optionDiscordNames) {
                    """
                        Could not find composite key named '$compositeKey', available options: $optionDiscordNames
                        On autocomplete function ${autocompleteInfo.function.shortSignature}
                        Available options from ${slashCommandInfo.function.shortSignature}
                    """.trimIndent()
                }
            }
        }
    }

    internal companion object {
        internal fun String.asChoice(type: JDAOptionType): Command.Choice? {
            return when (type) {
                JDAOptionType.STRING -> Command.Choice(this, this)
                JDAOptionType.INTEGER -> {
                    try {
                        Command.Choice(this, toLong())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                JDAOptionType.NUMBER -> {
                    try {
                        Command.Choice(this, toDouble())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                else -> throwArgument("Invalid autocomplete option type: $type")
            }
        }
    }
}
