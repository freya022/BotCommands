package io.github.freya022.botcommands.internal.commands.prefixed.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.RequireOwner
import io.github.freya022.botcommands.api.commands.application.slash.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand
import io.github.freya022.botcommands.api.commands.prefixed.TextCommandManager
import io.github.freya022.botcommands.api.commands.prefixed.annotations.*
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.computeIfAbsentOrNull
import io.github.freya022.botcommands.api.core.utils.nullIfEmpty
import io.github.freya022.botcommands.api.parameters.ParameterType
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

@BService
internal class TextCommandAutoBuilder(
    private val context: BContextImpl,
    private val resolverContainer: ResolverContainer
) {
    private val logger = KotlinLogging.logger { }

    private val functions: List<TextFunctionMetadata>

    init {
        functions = context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, JDATextCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(BaseCommandEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDATextCommand>() ?: throwInternal("@JDATextCommand should be present")
                val path = CommandPath.of(annotation.name, annotation.group.nullIfEmpty(), annotation.subcommand.nullIfEmpty()).also { path ->
                    if (path.group != null && path.nameCount == 2) {
                        throwUser(func, "Slash commands with groups need to have their subcommand name set")
                    }
                }

                TextFunctionMetadata(it, annotation, path)
            }
    }

    fun declare(manager: TextCommandManager) {
        val containers: MutableMap<String, TextCommandContainer> = hashMapOf()

        functions.forEachWithDelayedExceptions { metadata ->
            val firstContainer = containers.computeIfAbsent(metadata.path.name) { TextCommandContainer(it, metadata) }
            val container = when (metadata.path.nameCount) {
                1 -> firstContainer
                else -> {
                    val split = metadata.path.components
                    // Navigate to the subcommand that's going to hold the command
                    split
                        .drop(1) //Skip first component as it is the initial step
                        .dropLast(1) //Navigate text command containers until n-1 path component
                        .fold(firstContainer) { acc, s ->
                            acc.subcommands.computeIfAbsent(s) { TextCommandContainer(s, null) }
                        }
                        .subcommands //Only put metadata on the last path component as this is what the annotation applies on
                        .computeIfAbsentOrNull(split.last()) { TextCommandContainer(it, metadata) }
                        ?: throwUser(metadata.func, "Text subcommand with path '${metadata.path}' already exists")
                }
            }

            container.variations.add(metadata)
        }

        containers.values.forEach { container ->
            try {
                processCommand(manager, container)
            } catch (e: Exception) {
                logger.error("An exception occurred while registering annotated text command '${container.name}'", e)
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
        description = annotation.description

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
                    val optionName = optionAnnotation.name.nullIfEmpty() ?: declaredName
                    if (kParameter.type.jvmErasure.isValue) {
                        val inlineClassType = kParameter.type.jvmErasure.java
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
        helpExample = optionAnnotation.example.nullIfEmpty()
        isId = kParameter.hasAnnotation<ID>()
    }

    /**
     * @param metadata This is only the metadata of the first method encountered with the annotation
     */
    private class TextCommandContainer(val name: String, val metadata: TextFunctionMetadata?) {
        val subcommands: MutableMap<String, TextCommandContainer> = hashMapOf()
        val variations: MutableList<TextFunctionMetadata> = arrayListOf()
    }
}