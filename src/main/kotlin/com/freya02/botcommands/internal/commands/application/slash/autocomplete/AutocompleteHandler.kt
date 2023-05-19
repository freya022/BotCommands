package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.suppliers.*
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.full.*
import net.dv8tion.jda.api.interactions.commands.OptionType as JDAOptionType

internal class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfo, //Beware of this-leaks, the object is not completely initialized
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    internal val autocompleteInfo: AutocompleteInfo
) : IExecutableInteractionInfo {
    override val method = autocompleteInfo.function
    override val instance = slashCommandInfo.context.serviceContainer.getFunctionService(autocompleteInfo.function)
    override val parameters: List<MethodParameter>
        get() = methodParameters

    internal val methodParameters: List<AutocompleteCommandParameter>
    internal val compositeParameters: List<AutocompleteCommandOption>

    //accommodate for user input
    private val maxChoices = OptionData.MAX_CHOICES - if (autocompleteInfo.showUserInput) 1 else 0
    private val choiceSupplier: ChoiceSupplier

    init {
        methodParameters = slashCmdOptionAggregateBuilders.filterKeys { method.findParameterByName(it) != null }.transform<SlashCommandOptionAggregateBuilder, _> {
            AutocompleteCommandParameter(slashCommandInfo, slashCmdOptionAggregateBuilders, it, method)
        }

        compositeParameters = methodParameters
            .flatMap { it.commandOptions }
            .filter { it.optionType == OptionType.OPTION }
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
        val objects = slashCommandInfo.getSlashOptions(event, methodParameters)
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