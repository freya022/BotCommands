package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.arrayOfSize
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.suppliers.*
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.throwUser
import io.github.freya022.botcommands.internal.transform
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.collectionElementType
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.jvm.jvmErasure
import net.dv8tion.jda.api.interactions.commands.OptionType as JDAOptionType

/**
 * Autocomplete handlers are per-option,
 * they can share the same [AutocompleteInfo] but act on different commands with different sets of options,
 * as long as both the command function and the autocomplete function share parameters of the same name and type.
 *
 * Due to the Many-to-One associations, the handler cannot store any state, and must be stored in [AutocompleteInfo].
 */
internal class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfoImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    private val autocompleteInfo: AutocompleteInfoImpl,
    builder: SlashCommandBuilderImpl
) : ExecutableMixin {
    override val eventFunction = autocompleteInfo.eventFunction
    override val parameters: List<AutocompleteCommandParameterImpl>

    //accommodate for user input
    private val maxChoices = OptionData.MAX_CHOICES - if (autocompleteInfo.showUserInput) 1 else 0
    private val choiceSupplier: ChoiceSupplier

    init {
        this.parameters = slashCmdOptionAggregateBuilders.filterKeys { function.findParameterByName(it) != null }.transform {
            AutocompleteCommandParameterImpl(slashCommandInfo.context, slashCommandInfo, builder, slashCmdOptionAggregateBuilders, it, function)
        }

        val unmappedParameters = function.nonEventParameters.map { it.findDeclarationName() } - parameters.mapTo(hashSetOf()) { it.name }
        require(unmappedParameters.isEmpty()) {
            val autocompleteSignature = function.getSignature(parameterNames = unmappedParameters)
            """
                Could not find options declared as $unmappedParameters
                Required by autocomplete function $autocompleteSignature
                From slash command ${slashCommandInfo.function.shortSignature}
            """.trimIndent()
        }

        val collectionElementType = autocompleteInfo.function.returnType.collectionElementType?.jvmErasure
            ?: throwUser("Unable to determine return type, it should inherit Collection")

        choiceSupplier = when {
            collectionElementType in listOf(String::class, Long::class, Double::class) ->
                generateSupplierFromStrings(autocompleteInfo.mode)
            collectionElementType.isSubclassOf<Command.Choice>() -> ChoiceSupplierChoices(maxChoices)
            else -> {
                val transformer = slashCommandInfo.context.serviceContainer
                    .getInterfacedServices<AutocompleteTransformer<Any>>()
                    .firstOrNull { it.elementType == collectionElementType.java }
                    ?: throwUser("No autocomplete transformer has been register for objects of type '${collectionElementType.simpleName}', " +
                            "you may also check the docs for ${classRef<AutocompleteHandler>()} and ${classRef<AutocompleteTransformer<*>>()}")
                ChoiceSupplierTransformer(transformer, maxChoices)
            }
        }
    }

    internal fun invalidate() {
        autocompleteInfo.invalidate()
    }

    internal suspend fun handle(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        return autocompleteInfo.cache.retrieveAndCall(this, event, this::generateChoices)
    }

    private suspend fun generateChoices(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val objects = slashCommandInfo.getSlashOptions(event, parameters)
            ?: return emptyList() //Autocomplete was triggered without all the required parameters being present

        val actualChoices: MutableList<Command.Choice> = arrayOfSize(25)
        val suppliedChoices = choiceSupplier.apply(event, autocompleteInfo.function.callSuspendBy(objects))
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

    private fun generateSupplierFromStrings(autocompleteMode: AutocompleteMode): ChoiceSupplier {
        return if (autocompleteMode == AutocompleteMode.FUZZY) {
            ChoiceSupplierStringFuzzy(maxChoices)
        } else {
            ChoiceSupplierStringContinuity(maxChoices)
        }
    }

    internal fun validateParameters() {
        autocompleteInfo.autocompleteCache?.compositeKeys?.let { compositeKeys ->
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