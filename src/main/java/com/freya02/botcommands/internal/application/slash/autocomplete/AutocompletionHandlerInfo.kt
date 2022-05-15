package com.freya02.botcommands.internal.application.slash.autocomplete

import com.freya02.botcommands.annotations.api.application.slash.annotations.VarArgs
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompletionHandler
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CacheAutocompletion
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashCommandParameter
import com.freya02.botcommands.internal.application.slash.SlashUtils
import com.freya02.botcommands.internal.application.slash.SlashUtils2.checkDefaultValue
import com.freya02.botcommands.internal.application.slash.autocomplete.caches.AbstractAutocompletionCache
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierChoices
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierStringContinuity
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierStringFuzzy
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierTransformer
import com.freya02.botcommands.internal.runner.MethodRunner
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.function.Consumer
import kotlin.math.max
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

// The annotated method returns a list of things
// These things can be, and are mapped as follows:
//      String -> Choice(String, String)
//      Choice -> keep the same choice
//      Object -> Transformer -> Choice
class AutocompletionHandlerInfo(
    private val context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<Collection<*>>
) : ExecutableInteractionInfo {
    override val methodRunner: MethodRunner
    override val parameters: MethodParameters<AutocompleteCommandParameter>

    private val showUserInput: Boolean
    private val choiceSupplier: ChoiceSupplier
    private val cache: AbstractAutocompletionCache

    val handlerName: String
    val maxChoices: Int

    init {
        methodRunner = object : MethodRunner {
            //TODO replace
            @Suppress("UNCHECKED_CAST")
            override fun <R> invoke(
                args: Array<Any>,
                throwableConsumer: Consumer<Throwable>,
                successCallback: ConsumerEx<R>
            ) {
                try {
                    val call = method.call(*args)
                    successCallback.accept(call as R)
                } catch (e: Throwable) {
                    throwableConsumer.accept(e)
                }
            }
        }

        val annotation = method.findAnnotation<AutocompletionHandler>()!! //TODO change for normal code
        val autocompletionMode: AutocompletionMode = annotation.mode
        val cacheAutocompletion = method.findAnnotation<CacheAutocompletion>()!!

        cache = AbstractAutocompletionCache.fromMode(this, cacheAutocompletion)
        handlerName = annotation.name
        showUserInput = annotation.showUserInput
        maxChoices = OptionData.MAX_CHOICES - if (showUserInput) 1 else 0 //accommodate for user input
        val collectionReturnType =
            method.returnType.arguments.firstOrNull()?.type?.jvmErasure //TODO appropriate type detection in subtypes
                ?: throwUser("Unable to determine return type, it should inherit Collection")

        choiceSupplier = when {
            collectionReturnType.isSubclassOfAny(String::class, Long::class, Double::class) ->
                generateSupplierFromStrings(autocompletionMode)
            Command.Choice::class.isSuperclassOf(collectionReturnType) -> ChoiceSupplierChoices(this)
            else -> {
                @Suppress("UNCHECKED_CAST")
                val transformer =
                    context.getAutocompletionTransformer(collectionReturnType.starProjectedType) as? AutocompletionTransformer<Any>
                        ?: throwUser("No autocompletion transformer has been register for objects of type '${collectionReturnType.simpleName}', you may also check the docs for ${AutocompletionHandler::class.java.simpleName}")
                ChoiceSupplierTransformer(this, transformer)
            }
        }

        parameters = MethodParameters.of(method) { index, parameter -> AutocompleteCommandParameter(parameter, index) }
    }

    @Throws(Exception::class)
    private fun invokeAutocompletionHandler(
        slashCommand: SlashCommandInfo,
        event: CommandAutoCompleteInteractionEvent,
        throwableConsumer: Consumer<Throwable>,
        collectionCallback: ConsumerEx<Collection<*>>
    ) {
        val objects: MutableList<Any?> = ArrayList(parameters.size + 1)
        objects.add(event)
        for (parameter in parameters) {
            val guild = event.guild
            if (guild != null) {
                //Resolve the target slash command parameter, so we can retrieve its default value
                val slashParameter = slashCommand.parameters
                    .stream()
                    .filter { p: SlashCommandParameter -> p.applicationOptionData.effectiveName == parameter.applicationOptionData.effectiveName }
                    .findAny()
                    .orElseThrow { IllegalArgumentException("Could not find corresponding slash command parameter '" + parameter.applicationOptionData.effectiveName + "' when using autocomplete") }
                val supplier = slashParameter.defaultOptionSupplierMap[guild.idLong]
                if (supplier != null) {
                    val defaultVal = supplier.getDefaultValue(event)
                    this.checkDefaultValue(parameter, defaultVal)
                    objects.add(defaultVal)
                    continue
                }
            }
            val arguments = max(1, parameter.varArgs)
            val objectList: MutableList<Any?> = ArrayList(arguments)
            val applicationOptionData = parameter.applicationOptionData
            for (varArgNum in 0 until arguments) {
                if (parameter.isOption) {
                    val optionName = applicationOptionData.effectiveName
                    val varArgName = SlashUtils.getVarArgName(optionName, varArgNum)
                    val optionMapping = event.getOption(varArgName)

                    // Discord sends empty strings if you don't type anything, apparently is intended behavior
                    // Discord also sends invalid number strings, intended behavior too...
                    if (optionMapping == null || optionMapping.asString.isEmpty() || parameter.isPrimitive && !optionMapping.asString.chars()
                            .allMatch { i: Int -> Character.isDigit(i) || i == '.'.code }
                    ) {
                        if (parameter.isPrimitive) {
                            objectList.add(0)
                        } else {
                            objectList.add(null)
                        }
                        continue

                        //Don't throw if option mapping is not found, this is normal under autocompletion, only some options are sent
                    }
                    val resolved = parameter.resolver.resolve(context, slashCommand, event, optionMapping)

                    //If this is an additional vararg then it's OK for it to be null
                    if (resolved == null) {
                        //Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
                        LOGGER.trace(
                            "The parameter '{}' of value '{}' could not be resolved into a {}",
                            applicationOptionData.effectiveName,
                            optionMapping.asString,
                            parameter.boxedType.simpleName
                        )
                        return
                    }
                    if (!parameter.boxedType.jvmErasure.isSuperclassOf(resolved::class)) {
                        LOGGER.error(
                            "The parameter '{}' of value '{}' is not a valid type (expected a {})",
                            applicationOptionData.effectiveName,
                            optionMapping.asString,
                            parameter.boxedType.simpleName
                        )
                        return
                    }
                    objectList.add(resolved)
                } else {
                    objectList.add(parameter.customResolver.resolve(context, this, event))
                }
            }

            //For some reason using an array list instead of a regular array
            // magically unboxes primitives when passed to Method#invoke
            objects.add(if (parameter.isVarArg) objectList else objectList[0])
        }

        try {
            collectionCallback.accept(method.call(*objects.toTypedArray()))
        } catch (e: Throwable) {
            throwableConsumer.accept(e)
        }
    }

    private fun generateSupplierFromStrings(autocompletionMode: AutocompletionMode): ChoiceSupplier {
        return if (autocompletionMode == AutocompletionMode.FUZZY) {
            ChoiceSupplierStringFuzzy(this)
        } else {
            ChoiceSupplierStringContinuity(this)
        }
    }

    @Throws(Exception::class)
    fun retrieveChoices(
        slashCommand: SlashCommandInfo,
        event: CommandAutoCompleteInteractionEvent,
        throwableConsumer: Consumer<Throwable>,
        choiceCallback: Consumer<List<Command.Choice?>?>
    ) {
        cache.retrieveAndCall(event, choiceCallback) { key: CompositeAutocompletionKey? ->
            generateChoices(slashCommand, event, throwableConsumer) { choices: List<Command.Choice?>? ->
                cache.put(key, choices)
                choiceCallback.accept(choices)
            }
        }
    }

    @Throws(Exception::class)
    private fun generateChoices(
        slashCommand: SlashCommandInfo,
        event: CommandAutoCompleteInteractionEvent,
        throwableConsumer: Consumer<Throwable>,
        choiceCallback: ConsumerEx<List<Command.Choice?>>
    ) {
        invokeAutocompletionHandler(slashCommand, event, throwableConsumer) { collection: Collection<*>? ->
            val actualChoices: MutableList<Command.Choice?> = ArrayList(25)
            val suppliedChoices = choiceSupplier.apply(event, collection)
            val autoCompleteQuery = event.focusedOption

            //If something is typed but there are no choices, don't display user input
            if (showUserInput && autoCompleteQuery.value.isNotBlank() && suppliedChoices.isNotEmpty()) {
                val choice = getChoice(autoCompleteQuery.type, autoCompleteQuery.value)

                //Could be null if option mapping is malformed
                if (choice != null) {
                    actualChoices.add(choice)
                }
            }
            var i = 0
            while (i < maxChoices && i < suppliedChoices.size) {
                actualChoices.add(suppliedChoices[i])
                i++
            }
            choiceCallback.accept(actualChoices)
        }
    }

    fun checkParameters(info: SlashCommandInfo) {
        val slashOptions = info.optionParameters
        autocompleteParameterLoop@ for (autocompleteParameter in parameters) {
            if (!autocompleteParameter.isOption) continue

            for (slashCommandParameter in slashOptions) {
                if (slashCommandParameter.applicationOptionData.effectiveName == autocompleteParameter.applicationOptionData.effectiveName) {
                    checkParameter(slashCommandParameter, autocompleteParameter)
                    continue@autocompleteParameterLoop
                }
            }

            throwUser("Couldn't find parameter named '${autocompleteParameter.applicationOptionData.effectiveName}'")
        }
    }

    private fun checkParameter(
        slashCommandParameter: SlashCommandParameter,
        autocompleteParameter: AutocompleteCommandParameter
    ) {
        if (!slashCommandParameter.isOption) return
        val slashParameterType = slashCommandParameter.boxedType
        val autocompleteParameterType = autocompleteParameter.boxedType
        requireUser(slashParameterType == autocompleteParameterType) {
            "Autocompletion handler parameter #%d does not have the same type as slash command parameter: Provided: %s, correct: %s".format(
                autocompleteParameter.index,
                autocompleteParameterType,
                slashParameterType
            )
        }

        //If one is var arg but not the other
        requireUser(!(slashCommandParameter.isVarArg xor autocompleteParameter.isVarArg)) {
            "Autocompletion handler parameter #${autocompleteParameter.index} must be annotated with @${VarArgs::class.java.simpleName} if the slash command option is too"
        }

        requireUser(slashCommandParameter.varArgs == autocompleteParameter.varArgs) {
            "Autocompletion handler parameter #${autocompleteParameter.index} must have the same vararg number"
        }
    }

    fun invalidate() {
        cache.invalidate()
    }

    companion object {
        @JvmStatic
        fun getChoice(type: OptionType, string: String): Command.Choice? {
            return when (type) {
                OptionType.STRING -> Command.Choice(string, string)
                OptionType.INTEGER -> {
                    try {
                        Command.Choice(string, string.toLong())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                OptionType.NUMBER -> {
                    try {
                        Command.Choice(string, string.toDouble())
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                else -> throw IllegalArgumentException("Invalid autocompletion option type: $type")
            }
        }
    }
}