package com.freya02.botcommands.internal.application.slash.autocomplete

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompletionHandler
import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.application.builder.findOption
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.application.slash.autocomplete.caches.AbstractAutocompleteCache
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.*
import com.freya02.botcommands.internal.arrayOfSize
import com.freya02.botcommands.internal.isSubclassOfAny
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfo, //Beware of this-leaks, the object is not completely initialized
    internal val autocompleteInfo: AutocompleteInfo
) {
    private val instance = slashCommandInfo.context.serviceContainer.getFunctionService(autocompleteInfo.method)
    internal val methodParameters: MethodParameters

    private val maxChoices = OptionData.MAX_CHOICES - if (autocompleteInfo.showUserInput) 1 else 0
    private val choiceSupplier: ChoiceSupplier
    private val cache: AbstractAutocompleteCache

    init {
        methodParameters = MethodParameters.of<SlashParameterResolver>(slashCommandInfo.context, autocompleteInfo.method, slashCommandInfo.builder.optionBuilders) { parameter, name, resolver ->
            val optionBuilder = slashCommandInfo.builder.optionBuilders.findOption<SlashCommandOptionBuilder>(name)

            AutocompleteCommandParameter(parameter, optionBuilder, resolver)
        }

        //accommodate for user input
        val collectionReturnType =
            autocompleteInfo.method.returnType.arguments.firstOrNull()?.type?.jvmErasure //TODO appropriate type detection in subtypes
                ?: throwUser("Unable to determine return type, it should inherit Collection")

        choiceSupplier = when {
            collectionReturnType.isSubclassOfAny(String::class, Long::class, Double::class) ->
                generateSupplierFromStrings(autocompleteInfo.mode)
            Choice::class.isSuperclassOf(collectionReturnType) -> ChoiceSupplierChoices(maxChoices)
            else -> {
                @Suppress("UNCHECKED_CAST")
                val transformer =
                    slashCommandInfo.context.getAutocompletionTransformer(collectionReturnType.starProjectedType) as? AutocompletionTransformer<Any>
                        ?: throwUser("No autocompletion transformer has been register for objects of type '${collectionReturnType.simpleName}', you may also check the docs for ${AutocompletionHandler::class.java.simpleName}")
                ChoiceSupplierTransformer(transformer, maxChoices)
            }
        }

        cache = AbstractAutocompleteCache.fromMode(this)
    }

    suspend fun handle(event: CommandAutoCompleteInteractionEvent): Collection<Choice> {
        return cache.retrieveAndCall(event) { key: CompositeAutocompletionKey? ->
            val choices = generateChoices(event)

            if (key != null) {
                cache.put(key, choices)
            }

            return@retrieveAndCall choices
        }
    }

    private suspend fun generateChoices(event: CommandAutoCompleteInteractionEvent): List<Choice> {
        val objects: MutableMap<KParameter, Any?> = mutableMapOf()
        objects[autocompleteInfo.method.instanceParameter!!] = instance
        objects[autocompleteInfo.method.valueParameters.first()] = event

        slashCommandInfo.putSlashOptions(event, objects, methodParameters)

        val actualChoices: MutableList<Choice> = arrayOfSize(25)
        val suppliedChoices = choiceSupplier.apply(event, autocompleteInfo.method.callSuspendBy(objects))
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

    private fun generateSupplierFromStrings(autocompletionMode: AutocompletionMode): ChoiceSupplier {
        return if (autocompletionMode == AutocompletionMode.FUZZY) {
            ChoiceSupplierStringFuzzy(maxChoices)
        } else {
            ChoiceSupplierStringContinuity(maxChoices)
        }
    }

    internal companion object {
        internal fun String.asChoice(type: OptionType): Choice? {
            return when (type) {
                OptionType.STRING -> Choice(this, this)
                OptionType.INTEGER -> {
                    try {
                        Choice(this, toLong())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                OptionType.NUMBER -> {
                    try {
                        Choice(this, toDouble())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                else -> throw IllegalArgumentException("Invalid autocompletion option type: $type")
            }
        }
    }
}
