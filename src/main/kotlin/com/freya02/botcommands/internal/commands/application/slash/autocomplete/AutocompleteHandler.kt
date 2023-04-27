package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.suppliers.*
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

internal class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfo, //Beware of this-leaks, the object is not completely initialized
    slashCmdOptionAggregateBuilders: Map<String, OptionAggregateBuilder>,
    internal val autocompleteInfo: AutocompleteInfo
) {
    private val instance = slashCommandInfo.context.serviceContainer.getFunctionService(autocompleteInfo.function)
    internal val methodParameters: MethodParameters
    internal val compositeParameters: List<AutocompleteCommandOption>

    //accommodate for user input
    private val maxChoices = OptionData.MAX_CHOICES - if (autocompleteInfo.showUserInput) 1 else 0
    private val choiceSupplier: ChoiceSupplier

    init {
        methodParameters = MethodParameters.transform<SlashCommandOptionAggregateBuilder>(slashCmdOptionAggregateBuilders) {
            AutocompleteCommandParameter(slashCommandInfo, slashCmdOptionAggregateBuilders, it)
        }

        compositeParameters = methodParameters
            .map { it as AutocompleteCommandParameter }
            .flatMap { it.commandOptions }
            .filter { it.methodParameterType == MethodParameterType.OPTION }
            .map { it as AutocompleteCommandOption }
            .filter { it.isCompositeKey }

        val collectionElementType = autocompleteInfo.function.collectionElementType
            ?: throwUser("Unable to determine return type, it should inherit Collection")

        choiceSupplier = when {
            collectionElementType.isSubclassOfAny(String::class, Long::class, Double::class) ->
                generateSupplierFromStrings(autocompleteInfo.mode)
            Command.Choice::class.isSuperclassOf(collectionElementType) -> ChoiceSupplierChoices(maxChoices)
            else -> {
                @Suppress("UNCHECKED_CAST")
                val transformer =
                    slashCommandInfo.context.applicationConfig.autocompleteTransformers[collectionElementType.starProjectedType] as? AutocompleteTransformer<Any>
                        ?: throwUser("No autocomplete transformer has been register for objects of type '${collectionElementType.simpleName}', you may also check the docs for ${AutocompleteHandler::class.java.simpleName}")
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
        val objects: MutableMap<KParameter, Any?> = mutableMapOf()
        objects[autocompleteInfo.function.instanceParameter!!] = instance
        objects[autocompleteInfo.function.valueParameters.first()] = event

        slashCommandInfo.putSlashOptions(event, objects, methodParameters)

        if (objects.size - 2 < methodParameters.size) {
            return emptyList() //Autocomplete was triggered without all the required parameters being present
        }

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

    internal companion object {
        internal fun String.asChoice(type: OptionType): Command.Choice? {
            return when (type) {
                OptionType.STRING -> Command.Choice(this, this)
                OptionType.INTEGER -> {
                    try {
                        Command.Choice(this, toLong())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                OptionType.NUMBER -> {
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