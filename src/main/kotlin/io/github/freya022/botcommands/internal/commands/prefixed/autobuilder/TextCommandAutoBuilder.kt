package io.github.freya022.botcommands.internal.commands.prefixed.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.RequireOwner
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.annotations.*
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.commands.autobuilder.castFunction
import io.github.freya022.botcommands.internal.commands.autobuilder.fillCommandBuilder
import io.github.freya022.botcommands.internal.commands.autobuilder.forEachWithDelayedExceptions
import io.github.freya022.botcommands.internal.commands.autobuilder.requireCustomOption
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandComparator
import io.github.freya022.botcommands.internal.commands.prefixed.TextUtils.components
import io.github.freya022.botcommands.internal.commands.prefixed.autobuilder.metadata.TextFunctionMetadata
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

@BService
internal class TextCommandAutoBuilder(
    context: BContextImpl,
    private val resolverContainer: ResolverContainer
) {
    private class TextCommandContainer(val name: String) {
        val subcommands: MutableMap<String, TextCommandContainer> = hashMapOf()
        val variations: MutableList<TextFunctionMetadata> = arrayListOf()

        val metadata: TextFunctionMetadata? get() = variations.firstOrNull()
    }

    // The dominating metadata will set attributes on the top level command, such as the general description
    private object MetadataDominatorComparator : Comparator<TextFunctionMetadata> {
        private var warned = false

        override fun compare(a: TextFunctionMetadata, b: TextFunctionMetadata): Int {
            if (a.annotation.path.contentEquals(b.annotation.path)) {
                val firstHasGD = a.annotation.generalDescription.isNotBlank()
                val secondHasGD = b.annotation.generalDescription.isNotBlank()
                if (firstHasGD && secondHasGD) {
                    if (!warned) {
                        warned = true
                        logger.warn {
                            """
                            Annotated text command ${a.path} has multiple general descriptions, only one declaration can exist.
                            See ${a.func.shortSignature}
                            See ${b.func.shortSignature}
                        """.trimIndent()
                        }
                    }
                    return 0
                } else if (secondHasGD) {
                    return 1 //B is superior
                } else {
                    return -1
                }
            }

            // Metadata containers need to be constructed first
            return a.path.nameCount.compareTo(b.path.nameCount)
        }
    }

    private val functions: List<TextFunctionMetadata>

    init {
        functions = context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, JDATextCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(BaseCommandEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDATextCommand>() ?: throwInternal("@JDATextCommand should be present")
                val path = CommandPath.of(annotation.path.asList())

                TextFunctionMetadata(it, annotation, path)
            }
            .sortedWith(MetadataDominatorComparator)
    }

    fun declare(manager: TextCommandManager) {
        val containers: MutableMap<String, TextCommandContainer> = hashMapOf()

        functions.forEachWithDelayedExceptions { metadata ->
            // Checking if a (sub)command exists is not possible,
            // as it would require checking if two functions are the same commands
            val firstContainer = containers.computeIfAbsent(metadata.path.name) { TextCommandContainer(it) }
            val container = when (metadata.path.nameCount) {
                1 -> firstContainer
                // Navigate to the subcommand that's going to hold the command
                else -> metadata.path.components.drop(1).fold(firstContainer) { acc, subName ->
                    acc.subcommands.computeIfAbsent(subName) { TextCommandContainer(it) }
                }
            }

            container.variations.add(metadata)
        }

        containers.values.forEach { container ->
            try {
                processCommand(manager, container)
            } catch (e: Exception) {
                logger.error(e) { "An exception occurred while registering annotated text command '${container.name}'" }
            }
        }
    }

    private fun processCommand(manager: TextCommandManager, container: TextCommandContainer) {
        manager.textCommand(container.name) {
            container.metadata?.let { metadata ->
                try {
                    processBuilder(metadata)
                } catch (e: Exception) {
                    rethrowUser(metadata.func, "Unable to construct a text command", e)
                }
            }

            processVariations(container)

            container.subcommands.values.forEach { subContainer ->
                processSubcontainer(subContainer)
            }
        }
    }

    private fun TextCommandBuilder.processSubcontainer(subContainer: TextCommandContainer) {
        subcommand(subContainer.name) {
            subContainer.metadata?.let { metadata ->
                try {
                    processBuilder(metadata)
                } catch (e: Exception) {
                    rethrowUser(metadata.func, "Unable to construct a text subcommand", e)
                }
            }

            processVariations(subContainer)

            subContainer.subcommands.values.forEach {
                processSubcontainer(it)
            }
        }
    }

    private fun TextCommandBuilder.processVariations(container: TextCommandContainer) {
        container
            .variations
            .sortedWith(TextCommandComparator(context)) //Sort variations as to put most complex variations first, and fallback last
            .forEach {
                variation(it.func.castFunction()) {
                    try {
                        processVariation(it)
                    } catch (e: Exception) {
                        rethrowUser(it.func, "Unable to construct a text command variation", e)
                    }
                }
            }
    }

    private fun TextCommandVariationBuilder.processVariation(metadata: TextFunctionMetadata) {
        processOptions(metadata.func, metadata.instance, metadata.path)

        filters += AnnotationUtils.getFilters(context, metadata.func, TextCommandFilter::class)

        description = metadata.annotation.description.nullIfBlank()
        usage = metadata.annotation.usage.nullIfBlank()
        example = metadata.annotation.example.nullIfBlank()
    }

    private fun TextCommandBuilder.processBuilder(metadata: TextFunctionMetadata) {
        val func = metadata.func
        val annotation = metadata.annotation
        val instance = metadata.instance

        //Only put the command function if the path specified on the function is the same as the one computed in pathComponents

        fillCommandBuilder(func)

        if (this is TopLevelTextCommandBuilder) {
            func.findAnnotation<Category>()?.let { category = it.value }
        }

        aliases = annotation.aliases.toMutableList()
        description = annotation.generalDescription.nullIfBlank()

        hidden = func.hasAnnotation<Hidden>()
        ownerRequired = func.hasAnnotation<RequireOwner>()

        func.findAnnotation<NSFW>()?.let { nsfwAnnotation ->
            nsfw {
                allowInDMs = nsfwAnnotation.dm
                allowInGuild = nsfwAnnotation.guild
            }
        }

        detailedDescription = instance.detailedDescription
    }

    private fun TextCommandVariationBuilder.processOptions(func: KFunction<*>, instance: TextCommand, path: CommandPath) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            val declaredName = kParameter.findDeclarationName()
            when (val optionAnnotation = kParameter.findAnnotation<TextOption>()) {
                null -> when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> {
                        resolverContainer.requireCustomOption(func, kParameter, TextOption::class)
                        customOption(declaredName)
                    }
                    else -> generatedOption(
                        declaredName, instance.getGeneratedValueSupplier(
                            path,
                            kParameter.findOptionName(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
                else -> {
                    val optionName = optionAnnotation.name.nullIfBlank() ?: declaredName
                    if (kParameter.type.jvmErasure.isValue) {
                        val inlineClassType = kParameter.type.jvmErasure
                        when (val varArgs = kParameter.findAnnotation<VarArgs>()) {
                            null -> inlineClassOption(declaredName, optionName, inlineClassType) {
                                configureOption(kParameter, optionAnnotation)
                            }
                            else -> inlineClassOptionVararg(declaredName, inlineClassType, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                                configureOption(kParameter, optionAnnotation)
                            }
                        }
                    } else {
                        when (val varArgs = kParameter.findAnnotation<VarArgs>()) {
                            null -> option(declaredName, optionName) {
                                configureOption(kParameter, optionAnnotation)
                            }
                            else -> optionVararg(declaredName, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                                configureOption(kParameter, optionAnnotation)
                            }
                        }
                    }

                }
            }
        }
    }

    private fun TextCommandOptionBuilder.configureOption(kParameter: KParameter, optionAnnotation: TextOption) {
        helpExample = optionAnnotation.example.nullIfBlank()
        isId = kParameter.hasAnnotation<ID>()
    }
}