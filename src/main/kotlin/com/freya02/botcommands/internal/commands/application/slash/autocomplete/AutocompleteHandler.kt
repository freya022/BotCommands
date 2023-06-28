package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.api.core.service.getInterfacedServices
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.core.utils.arrayOfSize
import com.freya02.botcommands.api.core.utils.isSubclassOfAny
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandOption
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.suppliers.*
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.transform
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure
import net.dv8tion.jda.api.interactions.commands.OptionType as JDAOptionType

internal class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfo,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    internal val autocompleteInfo: AutocompleteInfo
) : IExecutableInteractionInfo {
    override val eventFunction = autocompleteInfo.eventFunction
    override val parameters: List<AutocompleteCommandParameter>

    //accommodate for user input
    private val maxChoices = OptionData.MAX_CHOICES - if (autocompleteInfo.showUserInput) 1 else 0
    private val choiceSupplier: ChoiceSupplier

    init {
        this.parameters = slashCmdOptionAggregateBuilders.filterKeys { function.findParameterByName(it) != null }.transform {
            AutocompleteCommandParameter(slashCommandInfo, slashCmdOptionAggregateBuilders, it, function)
        }

        val collectionElementType = autocompleteInfo.function.returnType.collectionElementType?.jvmErasure
            ?: throwUser("Unable to determine return type, it should inherit Collection")

        choiceSupplier = when {
            collectionElementType.isSubclassOfAny(String::class, Long::class, Double::class) ->
                generateSupplierFromStrings(autocompleteInfo.mode)
            Command.Choice::class.isSuperclassOf(collectionElementType) -> ChoiceSupplierChoices(maxChoices)
            else -> {
                val transformer = slashCommandInfo.context.serviceContainer
                    .getInterfacedServices<AutocompleteTransformer<Any>>()
                    .firstOrNull { it.elementType == collectionElementType.java }
                    ?: throwUser("No autocomplete transformer has been register for objects of type '${collectionElementType.simpleName}', " +
                            "you may also check the docs for ${AutocompleteHandler::class.simpleName} and ${AutocompleteTransformer::class.simpleName}")
                ChoiceSupplierTransformer(transformer, maxChoices)
            }
        }

        //Register this handler
        slashCommandInfo.context.getService<AutocompleteHandlerContainer>() += this
    }

    internal fun invalidate() {
        autocompleteInfo.cache.invalidate()
    }

    suspend fun handle(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
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
            val optionNames = slashCommandInfo.parameters
                .flatMap { it.allOptions }
                .filterIsInstance<SlashCommandOption>()
                .map { it.discordName }
            for (compositeKey in compositeKeys) {
                if (compositeKey !in optionNames) {
                    throwUser(autocompleteInfo.function, "Could not find composite key named '$compositeKey', available options: $optionNames\n" +
                            "See ${slashCommandInfo.function.shortSignature}")
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
                else -> throw IllegalArgumentException("Invalid autocomplete option type: $type")
            }
        }
    }
}