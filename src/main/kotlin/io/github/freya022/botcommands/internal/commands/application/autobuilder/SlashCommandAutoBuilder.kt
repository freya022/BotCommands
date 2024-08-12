package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.provider.*
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.*
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashOptionRegistry
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.SlashFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.MetadataFunctionHolder
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure
import net.dv8tion.jda.api.interactions.commands.Command as JDACommand

private val logger = KotlinLogging.logger { }
private val defaultTopLevelMetadata = TopLevelSlashCommandData()

@BService
@RequiresApplicationCommands
internal class SlashCommandAutoBuilder(
    override val serviceContainer: ServiceContainer,
    applicationConfig: BApplicationConfig,
    private val resolverContainer: ResolverContainer,
    functionAnnotationsMap: FunctionAnnotationsMap
) : CommandAutoBuilder, GlobalApplicationCommandProvider, GuildApplicationCommandProvider {
    private class TopLevelSlashCommandMetadata(
        val name: String,
        val annotation: TopLevelSlashCommandData,
        val metadata: SlashFunctionMetadata
    ) : MetadataFunctionHolder {
        override val func: KFunction<*> get() = metadata.func

        val subcommands: MutableList<SlashFunctionMetadata> = arrayListOf()
        val subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata> = hashMapOf()
    }

    private class SlashSubcommandGroupMetadata(val name: String) {
        class Properties(val description: String)

        lateinit var properties: Properties

        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
    }

    override val optionAnnotation: KClass<out Annotation> = SlashOption::class

    private val forceGuildCommands = applicationConfig.forceGuildCommands

    private val topLevelMetadata: MutableMap<String, TopLevelSlashCommandMetadata> = hashMapOf()

    init {
        val functions: List<SlashFunctionMetadata> =
            functionAnnotationsMap
                .getWithClassAnnotation<Command, JDASlashCommand>()
                .requiredFilter(FunctionFilter.nonStatic())
                .requiredFilter(FunctionFilter.firstArg(GlobalSlashEvent::class))
                .map {
                    val func = it.function
                    val annotation = func.findAnnotationRecursive<JDASlashCommand>() ?: throwInternal("${annotationRef<JDASlashCommand>()} should be present")
                    if (annotation.group.isNotBlank()) {
                        requireAt(annotation.subcommand.isNotBlank(), func) {
                            "Slash commands with groups need to have their subcommand name set"
                        }
                    }

                    val path = CommandPath.of(annotation.name, annotation.group.nullIfBlank(), annotation.subcommand.nullIfBlank())
                    val commandId = func.findAnnotationRecursive<CommandId>()?.value

                    SlashFunctionMetadata(it, annotation, path, commandId)
                }

        val duplicatePaths = functions.groupBy { it.path.fullPath }.filterValues { it.size >= 2 }
        check(duplicatePaths.isEmpty()) {
            val sharedPaths = duplicatePaths.entries.joinAsList { (path, metadataList) ->
                val signatures = metadataList.joinAsList { it.func.shortSignature }
                "$path:\n${signatures.prependIndent()}"
            }
            "Multiple annotated commands share the same path:\n$sharedPaths"
        }

        val missingTopLevels = functions.groupByTo(hashMapOf()) { it.path.name }
        // Check that top level names don't appear more than once
        missingTopLevels.values.forEach { metadataList ->
            val hasTopLevel = metadataList.any { it.path.nameCount == 1 }
            val hasSubcommands = metadataList.any { it.path.nameCount > 1 }
            check(!hasTopLevel || !hasSubcommands) {
                buildString {
                    appendLine("Cannot have both top level commands with subcommands:")
                    appendLine("Top level:")
                    appendLine(metadataList.filter { it.path.nameCount == 1 }.joinAsList { it.func.shortSignature })
                    appendLine("Subcommands:")
                    appendLine(metadataList.filter { it.path.nameCount > 1 }.joinAsList { it.func.shortSignature })
                }
            }
        }

        // Create all top level metadata
        functions.forEach { slashFunctionMetadata ->
            slashFunctionMetadata.func.findAnnotationRecursive<TopLevelSlashCommandData>()?.let { annotation ->
                // Remove all slash commands with the top level name
                val name = slashFunctionMetadata.path.name
                check(name in missingTopLevels) {
                    val refs = functions
                        .filter { it.path.name == name && it.func.hasAnnotationRecursive<TopLevelSlashCommandData>() }
                        .joinAsList { it.func.shortSignature }
                    "Cannot have multiple ${annotationRef<TopLevelSlashCommandData>()} on a same top-level command '$name':\n$refs"
                }

                missingTopLevels.remove(name)
                topLevelMetadata.putIfAbsentOrThrowInternal(name, TopLevelSlashCommandMetadata(name, annotation, slashFunctionMetadata))
            }
        }

        // Create default metadata for top level commands with no subcommands or groups
        // This can only be applied to single top level commands
        // as the function metadata needs to be taken from the function that has the top level annotation.
        // This is especially important for annotations such as @Test,
        // which are read on the function with the top-level annotation.
        // Picking a random function is not suited in this case.
        missingTopLevels.values
            .mapNotNull { it.singleOrNull() }
            .forEach { slashFunctionMetadata ->
                val name = slashFunctionMetadata.path.name
                missingTopLevels.remove(name)
                topLevelMetadata.putIfAbsentOrThrowInternal(name, TopLevelSlashCommandMetadata(name, defaultTopLevelMetadata, slashFunctionMetadata))
            }

        // Check if all commands have their metadata
        check(missingTopLevels.isEmpty()) {
            val missingTopLevelRefs = missingTopLevels.entries.joinAsList { (name, metadataList) ->
                if (metadataList.size == 1) throwInternal("Single top level commands should have been assigned the metadata")
                "$name:\n${metadataList.joinAsList("\t -") { it.func.shortSignature }}"
            }

            "At least one top-level slash command must be annotated with ${annotationRef<TopLevelSlashCommandData>()}:\n$missingTopLevelRefs"
        }

        // Assign subcommands and groups
        functions.forEachWithDelayedExceptions { metadata ->
            if (metadata.path.nameCount < 2) return@forEachWithDelayedExceptions

            val topLevelMetadata = topLevelMetadata[metadata.path.name]
                ?: throwInternal("Missing top level metadata '${metadata.path.name}' when assigning subcommands")
            if (metadata.path.nameCount == 2) {
                topLevelMetadata.subcommands.add(metadata)
            } else if (metadata.path.nameCount == 3) {
                topLevelMetadata
                    .subcommandGroups
                    .getOrPut(metadata.path.group!!) { metadata.toSubcommandGroupMetadata() }
                    .subcommands
                    .getOrPut(metadata.path.subname!!) { arrayListOf() }
                    .add(metadata)
            }
        }

        // For each subcommand group, find the SlashCommandGroupData from its subcommands
        topLevelMetadata.values.forEach { topLevelSlashCommandMetadata ->
            topLevelSlashCommandMetadata.subcommandGroups.values.forEach { slashSubcommandGroupMetadata ->
                val groupSubcommands = slashSubcommandGroupMetadata.subcommands.values.flatten()
                val annotation = groupSubcommands
                    .mapNotNull { metadata -> metadata.func.findAnnotationRecursive<SlashCommandGroupData>() }
                    .also { annotations ->
                        check(annotations.size <= 1) {
                            val refs = groupSubcommands
                                .filter { it.func.hasAnnotationRecursive<SlashCommandGroupData>() }
                                .joinAsList { it.func.shortSignature }
                            "Cannot have multiple ${annotationRef<SlashCommandGroupData>()} on a same subcommand group '${topLevelSlashCommandMetadata.name} ${slashSubcommandGroupMetadata.name}':\n$refs"
                        }
                    }
                    .firstOrNull() ?: SlashCommandGroupData()

                slashSubcommandGroupMetadata.properties = SlashSubcommandGroupMetadata.Properties(annotation.description)
            }
        }
    }

    private fun SlashFunctionMetadata.toSubcommandGroupMetadata() = SlashSubcommandGroupMetadata(path.group!!)

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) = declare(manager)

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) = declare(manager)

    private fun declare(manager: AbstractApplicationCommandManager) {
        with(SkipLogger(logger)) {
            topLevelMetadata
                .values
                .forEachWithDelayedExceptions loop@{ topLevelMetadata ->
                    val metadata = topLevelMetadata.metadata
                    runFiltered(
                        manager,
                        forceGuildCommands,
                        metadata,
                        topLevelMetadata.annotation.scope
                    ) {
                        processCommand(manager, topLevelMetadata)
                    }
                }
            log((manager as? GuildApplicationCommandManager)?.guild, JDACommand.Type.SLASH)
        }
    }

    context(SkipLogger)
    private fun processCommand(manager: AbstractApplicationCommandManager, topLevelMetadata: TopLevelSlashCommandMetadata) {
        val metadata = topLevelMetadata.metadata
        val annotation = metadata.annotation
        val path = metadata.path

        val name = path.name
        val subcommandsMetadata = topLevelMetadata.subcommands
        val subcommandGroupsMetadata = topLevelMetadata.subcommandGroups
        val isTopLevelOnly = subcommandsMetadata.isEmpty() && subcommandGroupsMetadata.isEmpty()
        manager.slashCommand(name, if (isTopLevelOnly) metadata.func.castFunction() else null) {
            contexts = topLevelMetadata.annotation.contexts.toEnumSetOr(manager.defaultContexts)
            integrationTypes = topLevelMetadata.annotation.integrationTypes.toEnumSetOr(manager.defaultIntegrationTypes)
            isDefaultLocked = topLevelMetadata.annotation.defaultLocked
            nsfw = topLevelMetadata.annotation.nsfw

            // On top-level only commands, the description can be set on either of the annotations, but not both
            if (isTopLevelOnly) {
                // One of them needs to not be set
                require(topLevelMetadata.annotation.description.isBlank() || annotation.description.isBlank()) {
                    "Slash command annotated with ${annotationRef<TopLevelSlashCommandData>()} must only have a description set once"
                }
            }
            description = annotation.description.nullIfBlank() ?: topLevelMetadata.annotation.description.nullIfBlank()

            addSubcommands(manager, subcommandsMetadata, metadata.commandId)

            addSubcommandGroups(manager, subcommandGroupsMetadata, metadata.commandId)

            configureBuilder(metadata)

            if (isTopLevelOnly) {
                processOptions((manager as? GuildApplicationCommandManager)?.guild, metadata)
            }
        }
    }

    context(SkipLogger)
    private fun TopLevelSlashCommandBuilder.addSubcommandGroups(
        manager: AbstractApplicationCommandManager,
        subcommandGroupsMetadata: MutableMap<String, SlashSubcommandGroupMetadata>,
        commandId: String?,
    ) {
        subcommandGroupsMetadata.values.forEach { groupMetadata ->
            subcommandGroup(groupMetadata.name) {
                description = groupMetadata.properties.description.nullIfBlank()

                groupMetadata.subcommands.forEach { (subname, metadataList) ->
                    metadataList.forEach subcommandLoop@{ subMetadata ->
                        if (!checkDeclarationFilter(manager, subMetadata.func, subMetadata.path, commandId))
                            return@subcommandLoop // Already logged

                        subcommand(subname, subMetadata.func.castFunction()) {
                            configureSubcommand(manager, subMetadata)
                        }
                    }
                }
            }
        }
    }

    context(SkipLogger)
    private fun TopLevelSlashCommandBuilder.addSubcommands(
        manager: AbstractApplicationCommandManager,
        subcommandsMetadata: MutableList<SlashFunctionMetadata>,
        commandId: String?,
    ) {
        subcommandsMetadata.forEach { subMetadata ->
            if (!checkDeclarationFilter(manager, subMetadata.func, subMetadata.path, commandId))
                return@forEach // Already logged

            subcommand(subMetadata.path.subname!!, subMetadata.func.castFunction()) {
                configureSubcommand(manager, subMetadata)
            }
        }
    }

    private fun SlashSubcommandBuilder.configureSubcommand(manager: AbstractApplicationCommandManager, subMetadata: SlashFunctionMetadata) {
        this.description = subMetadata.annotation.description.nullIfBlank()
        this.configureBuilder(subMetadata)
        this.processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata)
    }

    private fun SlashCommandBuilder.configureBuilder(metadata: SlashFunctionMetadata) {
        fillCommandBuilder(metadata.func)
        fillApplicationCommandBuilder(metadata.func)
    }

    private fun SlashCommandBuilder.processOptions(guild: Guild?, metadata: SlashFunctionMetadata) {
        val instance = metadata.instance
        val func = metadata.func
        val path = metadata.path

        fun slashOption(kParameter: KParameter, declaredName: String, optionAnnotation: SlashOption) {
            fun SlashOptionRegistry.addOption(valueName: String) {
                val optionName = optionAnnotation.name.ifBlank { declaredName.toDiscordString() }
                val varArgs = kParameter.findAnnotationRecursive<VarArgs>()
                if (varArgs != null) {
                    optionVararg(valueName, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                        configureOption(guild, instance, kParameter, optionAnnotation)
                    }
                } else {
                    option(valueName, optionName) {
                        configureOption(guild, instance, kParameter, optionAnnotation)
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
            val optionAnnotation = kParameter.findAnnotationRecursive<SlashOption>()
            if (optionAnnotation != null) {
                slashOption(kParameter, declaredName, optionAnnotation)
            } else if (kParameter.hasAnnotationRecursive<GeneratedOption>()) {
                generatedOption(
                    declaredName, instance.getGeneratedValueSupplier(
                        guild,
                        metadata.commandId,
                        path,
                        kParameter.findOptionName(),
                        ParameterType.ofType(kParameter.type)
                    )
                )
            } else if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(kParameter.wrap())) {
                customOption(declaredName)
            } else {
                requireServiceOptionOrOptional(func, kParameter, JDASlashCommand::class)
                serviceOption(declaredName)
            }
        }
    }

    context(SlashCommandBuilder)
    private fun SlashCommandOptionBuilder.configureOption(guild: Guild?, instance: ApplicationCommand, kParameter: KParameter, optionAnnotation: SlashOption) {
        description = optionAnnotation.description.nullIfBlank()

        kParameter.findAnnotationRecursive<LongRange>()?.let { range -> valueRange = ValueRange.ofLong(range.from, range.to) }
        kParameter.findAnnotationRecursive<DoubleRange>()?.let { range -> valueRange = ValueRange.ofDouble(range.from, range.to) }
        kParameter.findAnnotationRecursive<Length>()?.let { length -> lengthRange = LengthRange.of(length.min, length.max) }

        processAutocomplete(optionAnnotation)

        usePredefinedChoices = optionAnnotation.usePredefinedChoices
        val optionChoices = instance.getOptionChoices(guild, this@SlashCommandBuilder.path, optionName)
        if (optionChoices.isNotEmpty())
            choices = optionChoices
    }

    private fun SlashCommandOptionBuilder.processAutocomplete(optionAnnotation: SlashOption) {
        if (optionAnnotation.autocomplete.isNotEmpty()) {
            autocompleteByName(optionAnnotation.autocomplete)
        }
    }
}
