package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder.Companion.findOption
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.suppliers.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

internal class AutocompleteHandler(
    private val slashCommandInfo: SlashCommandInfo, //Beware of this-leaks, the object is not completely initialized
    slashCmdOptionBuilders: Map<String, OptionBuilder>,
    internal val autocompleteInfo: AutocompleteInfo
) {
    private val instance = slashCommandInfo.context.serviceContainer.getFunctionService(autocompleteInfo.method)
    internal val methodParameters: MethodParameters

    private val maxChoices = OptionData.MAX_CHOICES - if (autocompleteInfo.showUserInput) 1 else 0
    private val choiceSupplier: ChoiceSupplier

    init {
        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        methodParameters = MethodParameters.transform<SlashParameterResolver<*, *>>( //Same transform method as in SlashCommandInfo, but option transformer is different
            slashCommandInfo.context,
            autocompleteInfo.method,
            slashCmdOptionBuilders
        ) {
            optionPredicate = { slashCmdOptionBuilders[it.findDeclarationName()] is SlashCommandOptionBuilder }
            optionTransformer = { kParameter, paramName, resolver ->
                val optionBuilder = slashCmdOptionBuilders.findOption<SlashCommandOptionBuilder>(paramName, "an autocomplete option")
                AutocompleteCommandParameter(kParameter, optionBuilder, resolver)
            }
        }

        //accommodate for user input
        val collectionReturnType =
            autocompleteInfo.method.returnType.arguments.firstOrNull()?.type?.jvmErasure //TODO appropriate type detection in subtypes
                ?: throwUser("Unable to determine return type, it should inherit Collection")

        choiceSupplier = when {
            collectionReturnType.isSubclassOfAny(String::class, Long::class, Double::class) ->
                generateSupplierFromStrings(autocompleteInfo.mode)
            Command.Choice::class.isSuperclassOf(collectionReturnType) -> ChoiceSupplierChoices(maxChoices)
            else -> {
                @Suppress("UNCHECKED_CAST")
                val transformer =
                    slashCommandInfo.context.config.applicationConfig.autocompleteTransformers[collectionReturnType.starProjectedType] as? AutocompleteTransformer<Any>
                        ?: throwUser("No autocomplete transformer has been register for objects of type '${collectionReturnType.simpleName}', you may also check the docs for ${AutocompleteHandler::class.java.simpleName}")
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
        objects[autocompleteInfo.method.instanceParameter!!] = instance
        objects[autocompleteInfo.method.valueParameters.first()] = event

        slashCommandInfo.putSlashOptions(event, objects, methodParameters)

        if (objects.size - 2 < methodParameters.size) {
            return emptyList() //Autocomplete was triggered without all the required parameters being present
        }

        val actualChoices: MutableList<Command.Choice> = arrayOfSize(25)
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