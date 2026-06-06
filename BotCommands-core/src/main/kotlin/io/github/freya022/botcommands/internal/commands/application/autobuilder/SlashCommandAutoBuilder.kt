package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplierProvider
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.SlashOptionChoiceProvider
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
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
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.SlashCommandMetadata
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.SlashFunctionMetadata
import io.github.freya022.botcommands.internal.commands.application.autobuilder.utils.ParameterAdapter
import io.github.freya022.botcommands.internal.commands.autobuilder.castFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command as JDACommand
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

private val defaultTopLevelMetadata = TopLevelSlashCommandData()

@BService
@RequiresApplicationCommands
internal class SlashCommandAutoBuilder(
    override val serviceContainer: ServiceContainer,
    applicationConfig: BApplicationConfig,
    private val resolverContainer: ResolverContainer,
    functionAnnotationsMap: FunctionAnnotationsMap
) : ApplicationCommandAutoBuilder<SlashCommandMetadata>(applicationConfig) {

    override val optionAnnotation: KClass<out Annotation> = SlashOption::class
    override val commandType: JDACommand.Type get() = JDACommand.Type.SLASH

    private val metadata: Map<String, SlashCommandMetadata>
    override val rootAnnotatedCommands: Collection<SlashCommandMetadata>
        get() = metadata.values

    init {
        val topLevelMetadata: MutableMap<String, SlashCommandMetadata.TopLevel> = hashMapOf()
        val groupedBuilders: MutableMap<String, SlashCommandMetadata.Grouped.Builder> = hashMapOf()

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

        // Check that top level names don't appear more than once
        functions.groupBy { it.path.name }.values.forEach { metadataList ->
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

        // Find subcommands that don't have a @TopLevelSlashCommandData
        run {
            val subcommandsByName = functions
                .filter { it.path.nameCount > 1 }
                .groupBy { it.path.name }

            val subcommandListsWithoutTopAnnotation = subcommandsByName.filterValues { metadataList ->
                val hasTopLevelAnnotation = metadataList.any { it.func.hasAnnotationRecursive<TopLevelSlashCommandData>() }
                !hasTopLevelAnnotation
            }

            require(subcommandListsWithoutTopAnnotation.isEmpty()) {
                val topNamesWithoutAnnotation = subcommandListsWithoutTopAnnotation.keys.joinAsList()
                "Subcommands must have at least one function be annotated with ${annotationRef<TopLevelSlashCommandData>()}:\n$topNamesWithoutAnnotation"
            }
        }

        val functionsByName = functions.groupBy { it.path.name }
        // At this point we have made sure that subcommands have an @TopLevelSlashCommandData at least once
        functionsByName.forEach { (name, metadataList) ->
            fun findTopLevelMetadata(): SlashFunctionMetadata? {
                return metadataList.firstOrNull { it.func.hasAnnotationRecursive<TopLevelSlashCommandData>() }
            }

            fun findTopLevelAnnotation(): TopLevelSlashCommandData? {
                return metadataList.firstNotNullOfOrNull { it.func.findAnnotationRecursive<TopLevelSlashCommandData>() }
            }

            fun throwMissingTopLevelAnnotation(): Nothing {
                throwInternal("${annotationRef<TopLevelSlashCommandData>()} should have been checked present for command '$name'")
            }

            if (metadataList.size == 1) {
                val metadata = metadataList.single()
                if (metadata.path.nameCount == 1) {
                    topLevelMetadata.putIfAbsentOrThrowInternal(name, SlashCommandMetadata.TopLevel(findTopLevelAnnotation() ?: defaultTopLevelMetadata, metadata))
                } else {
                    val topLevelAnnotation = findTopLevelAnnotation() ?: throwMissingTopLevelAnnotation()
                    groupedBuilders.putIfAbsentOrThrowInternal(name, SlashCommandMetadata.Grouped.Builder(name, topLevelAnnotation, metadata))
                }
            } else if (metadataList.size >= 2) {
                val topLevelAnnotation = findTopLevelAnnotation() ?: throwMissingTopLevelAnnotation()
                val topLevelMetadata = findTopLevelMetadata() ?: throwMissingTopLevelAnnotation()
                groupedBuilders.putIfAbsentOrThrowInternal(name, SlashCommandMetadata.Grouped.Builder(name, topLevelAnnotation, topLevelMetadata))
            } else {
                throwInternal("No functions for '$name'")
            }
        }

        // Assign subcommands and groups
        functions.forEach { metadata ->
            if (metadata.path.nameCount < 2) return@forEach

            val builder = groupedBuilders[metadata.path.name]
                ?: throwInternal("Missing top level metadata '${metadata.path.name}' when assigning subcommands")
            if (metadata.path.nameCount == 2) {
                builder.subcommands.add(metadata)
            } else if (metadata.path.nameCount == 3) {
                builder
                    .subcommandGroups
                    .getOrPut(metadata.path.group!!) { SlashCommandMetadata.Grouped.SubcommandGroup.Builder(metadata.path.group!!) }
                    .subcommands
                    .add(metadata)
            }
        }

        // For each subcommand group, find the SlashCommandGroupData from its subcommands
        groupedBuilders.values.forEach { topLevelSlashCommandMetadata ->
            topLevelSlashCommandMetadata.subcommandGroups.values.forEach { slashSubcommandGroupMetadata ->
                val groupSubcommands = slashSubcommandGroupMetadata.subcommands
                val annotation = run {
                    val annotations = groupSubcommands.mapNotNull { metadata -> metadata.func.findAnnotationRecursive<SlashCommandGroupData>() }

                    check(annotations.size <= 1) {
                        val refs = groupSubcommands
                            .filter { it.func.hasAnnotationRecursive<SlashCommandGroupData>() }
                            .joinAsList { it.func.shortSignature }
                        "Cannot have multiple ${annotationRef<SlashCommandGroupData>()} on a same subcommand group '${topLevelSlashCommandMetadata.name} ${slashSubcommandGroupMetadata.name}':\n$refs"
                    }

                    annotations.firstOrNull() ?: SlashCommandGroupData()
                }

                slashSubcommandGroupMetadata.properties = SlashCommandMetadata.Grouped.SubcommandGroup.Properties(annotation.description)
            }
        }

        this.metadata = topLevelMetadata + groupedBuilders.mapValues { (_, builder) -> builder.build() }
    }

    context(logger: SkipLogger)
    override fun declareTopLevel(manager: AbstractApplicationCommandManager, rootCommand: SlashCommandMetadata) {
        val metadata = rootCommand.metadata
        val annotation = metadata.annotation
        val path = metadata.path

        val name = path.name

        fun TopLevelSlashCommandBuilder.configureTopLevelCommons() {
            contexts = if (forceGuildCommands) {
                setOf(InteractionContextType.GUILD)
            } else {
                rootCommand.annotation.contexts.toEnumSetOr(manager.defaultContexts)
            }
            integrationTypes = rootCommand.annotation.integrationTypes.toEnumSetOr(manager.defaultIntegrationTypes)
            isDefaultLocked = rootCommand.annotation.defaultLocked
            nsfw = rootCommand.annotation.nsfw

            // Prioritize [[TopLevelSlashCommandData]] as this is top level
            description = rootCommand.annotation.description.nullIfBlank() ?: annotation.description.nullIfBlank()
        }

        if (rootCommand is SlashCommandMetadata.TopLevel) {
            if (!checkDeclarationFilter(manager, metadata))
                return

            manager.slashCommand(name, metadata.func.castFunction()) {
                configureTopLevelCommons()

                configureBuilder(metadata)

                processOptions((manager as? GuildApplicationCommandManager)?.guild, metadata)
            }
        } else if (rootCommand is SlashCommandMetadata.Grouped) {
            val filteredSubcommands = rootCommand.subcommands.filter { subMetadata ->
                checkDeclarationFilter(manager, subMetadata)
            }
            // Filter subcommands from groups and remove groups with no subcommands
            val filteredSubcommandGroups = rootCommand.subcommandGroups.values
                // Make a copy of subcommand groups but with subcommands filtered
                .map { subGroupMetadata ->
                    subGroupMetadata.filterSubcommands { subMetadata ->
                        checkDeclarationFilter(manager, subMetadata)
                    }
                }
                // Remove groups without subcommands
                .filter { it.subcommands.isNotEmpty() }

            if (filteredSubcommands.isEmpty() && filteredSubcommandGroups.isEmpty())
                return

            manager.slashCommand(name, function = null) {
                configureTopLevelCommons()

                addSubcommands(manager, filteredSubcommands)

                addSubcommandGroups(manager, filteredSubcommandGroups)

                configureBuilder(metadata)
            }
        }
    }

    context(_: SkipLogger)
    private fun TopLevelSlashCommandBuilder.addSubcommandGroups(
        manager: AbstractApplicationCommandManager,
        subcommandGroupsMetadata: Collection<SlashCommandMetadata.Grouped.SubcommandGroup>,
    ) {
        subcommandGroupsMetadata.forEach { groupMetadata ->
            subcommandGroup(groupMetadata.name) {
                description = groupMetadata.properties.description.nullIfBlank()

                groupMetadata.subcommands.forEach { subMetadata ->
                    subcommand(subMetadata.path.subname!!, subMetadata.func.castFunction()) {
                        configureSubcommand(manager, subMetadata)
                    }
                }
            }
        }
    }

    context(_: SkipLogger)
    private fun TopLevelSlashCommandBuilder.addSubcommands(
        manager: AbstractApplicationCommandManager,
        subcommandsMetadata: List<SlashFunctionMetadata>,
    ) {
        subcommandsMetadata.forEach { subMetadata ->
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
        fillCommandBuilder(ApplicationCommandUnit(serviceContainer, metadata))
        fillApplicationCommandBuilder(metadata.func)
    }

    private fun SlashCommandBuilder.processOptions(guild: Guild?, metadata: SlashFunctionMetadata) {
        metadata.func.nonInstanceParameters.drop(1).forEach { kParameter ->
            val paramType = kParameter.type.jvmErasure
            if (paramType.isValue) {
                inlineClassAggregate(kParameter.findDeclarationName(), paramType) { valueParameter, _ ->
                    addOption(this@inlineClassAggregate, metadata, guild, ParameterAdapter(kParameter, valueParameter))
                }
            } else {
                addOption(this@processOptions, metadata, guild, ParameterAdapter(kParameter, kParameter))
            }
        }
    }

    private fun addOption(registry: SlashOptionRegistry, metadata: SlashFunctionMetadata, guild: Guild?, parameter: ParameterAdapter) {
        val instance = metadata.instance
        val path = metadata.path
        val func = metadata.func
        val commandId = metadata.commandId

        val optionAnnotation = parameter.findAnnotation<SlashOption>()
        if (optionAnnotation != null) {
            val optionName = optionAnnotation.name.ifBlank { parameter.discordName }
            val varArgs = parameter.findAnnotation<VarArgs>()
            if (varArgs != null) {
                registry.optionVararg(parameter.declaredName, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                    configureOption(metadata, guild, parameter, optionAnnotation)
                }
            } else {
                registry.option(parameter.declaredName, optionName) {
                    configureOption(metadata, guild, parameter, optionAnnotation)
                }
            }
        } else if (parameter.hasAnnotation<GeneratedOption>()) {
            checkAt(instance is ApplicationGeneratedValueSupplierProvider, func) {
                "Declaring class must extend ${classRef<ApplicationGeneratedValueSupplierProvider>()}"
            }

            val valueSupplier = instance.getGeneratedValueSupplier(guild, commandId, path, parameter.discordName, parameter.actualType)
            registry.generatedOption(parameter.declaredName, valueSupplier)
        } else if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(parameter.valueParameter.wrap())) {
            registry.customOption(parameter.declaredName)
        } else {
            requireServiceOptionOrOptional(func, parameter, JDASlashCommand::class)
            registry.serviceOption(parameter.declaredName)
        }
    }

    private fun SlashCommandOptionBuilder.configureOption(metadata: SlashFunctionMetadata, guild: Guild?, parameter: ParameterAdapter, optionAnnotation: SlashOption) {
        val instance = metadata.instance

        description = optionAnnotation.description.nullIfBlank()

        parameter.findAnnotation<LongRange>()?.let { range -> valueRange = ValueRange.ofLong(range.from, range.to) }
        parameter.findAnnotation<DoubleRange>()?.let { range -> valueRange = ValueRange.ofDouble(range.from, range.to) }
        parameter.findAnnotation<Length>()?.let { length -> lengthRange = LengthRange.of(length.min, length.max) }

        processAutocomplete(optionAnnotation)

        usePredefinedChoices = optionAnnotation.usePredefinedChoices
        if (instance is SlashOptionChoiceProvider) {
            val optionChoices = instance.getOptionChoices(guild, metadata.path, optionName)
            if (optionChoices.isNotEmpty())
                choices = optionChoices
        }
    }

    private fun SlashCommandOptionBuilder.processAutocomplete(optionAnnotation: SlashOption) {
        if (optionAnnotation.autocomplete.isNotEmpty()) {
            autocompleteByName(optionAnnotation.autocomplete)
        }
    }
}
