package io.github.freya022.botcommands.internal.commands.text.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.annotations.*
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.commands.text.options.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.text.options.builder.TextOptionRegistry
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.commands.text.TextCommandComparator
import io.github.freya022.botcommands.internal.commands.text.TextUtils.components
import io.github.freya022.botcommands.internal.commands.text.autobuilder.metadata.TextFunctionMetadata
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }
private val defaultExtraData = TextCommandData()

@BService
@RequiresTextCommands
internal class TextCommandAutoBuilder(
    private val resolverContainer: ResolverContainer,
    functionAnnotationsMap: FunctionAnnotationsMap,
    override val serviceContainer: ServiceContainer
) : CommandAutoBuilder, TextCommandProvider {
    private class TextCommandContainer(val name: String) {
        var extraData: TextCommandData = defaultExtraData
        val hasExtraData get() = extraData !== defaultExtraData

        val subcommands: MutableMap<String, TextCommandContainer> = hashMapOf()
        // This may be empty in case this just holds subcommands
        val variations: MutableList<TextFunctionMetadata> = arrayListOf()

        val metadata: TextFunctionMetadata? get() = variations.firstOrNull()
    }

    override val optionAnnotation: KClass<out Annotation> = TextOption::class

    private val containers: MutableMap<String, TextCommandContainer> = hashMapOf()

    init {
        val functions = functionAnnotationsMap
            .getWithClassAnnotation<Command, JDATextCommandVariation>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(BaseCommandEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotationRecursive<JDATextCommandVariation>() ?: throwInternal("${annotationRef<JDATextCommandVariation>()} should be present")
                val path = CommandPath.of(annotation.path.asList())

                TextFunctionMetadata(it, annotation, path)
            }

        // Add variations to their (sub)commands
        functions.forEachWithDelayedExceptions { metadata ->
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

        // Assign user-generated extra data
        functions.forEach { textFunctionMetadata ->
            textFunctionMetadata.func.findAnnotationRecursive<TextCommandData>()?.let { annotation ->
                // If the path is not specified (empty array), use the path of the variation
                val path = when {
                    annotation.path.isNotEmpty() -> CommandPath.of(annotation.path.asList())
                    else -> textFunctionMetadata.path
                }
                // Find command
                val firstContainer = containers[path.name]
                    ?: throwInternal("Cannot find top level metadata for '${path.name}' when assigning extra data")
                val container = when (path.nameCount) {
                    1 -> firstContainer
                    // Navigate to the subcommand that's going to hold the command
                    else -> path.components.drop(1).fold(firstContainer) { acc, subName ->
                        acc.subcommands[subName] ?: throwArgument("Cannot find command variation '$path'")
                    }
                }

                // Cannot reassign extra data
                check(!container.hasExtraData) {
                    val refs = functions
                        .filter { it.func.findAnnotationRecursive<TextCommandData>()?.path contentEquals annotation.path }
                        .joinAsList { it.func.shortSignature }
                    "Cannot have multiple ${annotationRef<TextCommandData>()} assigned on '$path':\n$refs"
                }

                // Assign
                container.extraData = annotation
            }
        }
    }

    override fun declareTextCommands(manager: TextCommandManager) {
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
                    metadata.instance.javaClass.findAnnotationRecursive<Category>()?.let { category = it.value }

                    processBuilder(container, metadata)
                } catch (e: Exception) {
                    e.rethrowAt("Unable to construct a text command", metadata.func)
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
                    processBuilder(subContainer, metadata)
                } catch (e: Exception) {
                    e.rethrowAt("Unable to construct a text subcommand", metadata.func)
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
                        e.rethrowAt("Unable to construct a text command variation", it.func)
                    }
                }
            }
    }

    private fun TextCommandVariationBuilder.processVariation(metadata: TextFunctionMetadata) {
        declarationSite = DeclarationSite.fromFunctionSignature(metadata.func)
        processOptions(metadata.func, metadata.instance, metadata.path)

        filters += AnnotationUtils.getFilters(context, metadata.func, TextCommandFilter::class)

        description = metadata.annotation.description.nullIfBlank()
        usage = metadata.annotation.usage.nullIfBlank()
        example = metadata.annotation.example.nullIfBlank()
    }

    private fun TextCommandBuilder.processBuilder(container: TextCommandContainer, metadata: TextFunctionMetadata) {
        val instance = metadata.instance

        // Any variation could contain an annotation we're searching for.
        // But only one annotation may be taken for the given command
        val variationFunctions = container.variations.map { it.func }
        fillCommandBuilder(variationFunctions)

        description = container.extraData.description.nullIfBlank()
        aliases += container.extraData.aliases

        hidden = variationFunctions.singlePresentAnnotationOfVariants<Hidden>()
        ownerRequired = variationFunctions.singlePresentAnnotationOfVariants<RequireOwner>()

        nsfw = variationFunctions.singlePresentAnnotationOfVariants<NSFW>()

        detailedDescription = instance.detailedDescription
    }

    private fun TextCommandVariationBuilder.processOptions(func: KFunction<*>, instance: TextCommand, path: CommandPath) {
        fun textOption(kParameter: KParameter, declaredName: String, optionAnnotation: TextOption) {
            fun TextOptionRegistry.addOption(valueName: String) {
                val optionName = optionAnnotation.name.ifBlank { declaredName.toDiscordString() }
                val varArgs = kParameter.findAnnotationRecursive<VarArgs>()
                if (varArgs != null) {
                    optionVararg(valueName, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                        configureOption(kParameter, optionAnnotation)
                    }
                } else {
                    option(valueName, optionName) {
                        configureOption(kParameter, optionAnnotation)
                    }
                }
            }

            val paramType = kParameter.type.jvmErasure
            if (paramType.isValue) {
                inlineClassAggregate(declaredName, paramType) { valueName ->
                    addOption(valueName)
                }
            } else {
                addOption(declaredName)
            }
        }

        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            val declaredName = kParameter.findDeclarationName()
            val optionAnnotation = kParameter.findAnnotationRecursive<TextOption>()
            if (optionAnnotation != null) {
                textOption(kParameter, declaredName, optionAnnotation)
            } else if (kParameter.hasAnnotationRecursive<GeneratedOption>()) {
                generatedOption(
                    declaredName, instance.getGeneratedValueSupplier(
                        path,
                        kParameter.findOptionName(),
                        ParameterType.ofType(kParameter.type)
                    )
                )
            } else if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(kParameter.wrap())) {
                customOption(declaredName)
            } else {
                requireServiceOptionOrOptional(func, kParameter, JDATextCommandVariation::class)
                serviceOption(declaredName)
            }
        }
    }

    private fun TextCommandOptionBuilder.configureOption(kParameter: KParameter, optionAnnotation: TextOption) {
        helpExample = optionAnnotation.example.nullIfBlank()
        isId = kParameter.hasAnnotationRecursive<ID>()
    }
}