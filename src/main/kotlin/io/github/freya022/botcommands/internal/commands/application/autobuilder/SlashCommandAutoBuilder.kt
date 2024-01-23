package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.*
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.*
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.SlashFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.MetadataFunctionHolder
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }
private val defaultTopLevelMetadata = TopLevelSlashCommandData()

@BService
internal class SlashCommandAutoBuilder(
    private val context: BContextImpl,
    private val resolverContainer: ResolverContainer
) {
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

    private val forceGuildCommands = context.applicationConfig.forceGuildCommands

    private val topLevelMetadata: MutableMap<String, TopLevelSlashCommandMetadata> = hashMapOf()

    init {
        val functions: List<SlashFunctionMetadata> =
            context.instantiableServiceAnnotationsMap
                .getInstantiableFunctionsWithAnnotation<Command, JDASlashCommand>()
                .requiredFilter(FunctionFilter.nonStatic())
                .requiredFilter(FunctionFilter.firstArg(GlobalSlashEvent::class))
                .map {
                    val func = it.function
                    val annotation = func.findAnnotation<JDASlashCommand>() ?: throwInternal("${annotationRef<JDASlashCommand>()} should be present")
                    val path = CommandPath.of(annotation.name, annotation.group.nullIfBlank(), annotation.subcommand.nullIfBlank()).also { path ->
                        if (path.group != null && path.nameCount == 2) {
                            throwUser(func, "Slash commands with groups need to have their subcommand name set")
                        }
                    }
                    val commandId = func.findAnnotation<CommandId>()?.value

                    SlashFunctionMetadata(it, annotation, path, commandId)
                }

        // Create all top level metadata
        val missingTopLevels = functions.groupByTo(hashMapOf()) { it.path.name }
        functions.forEach { slashFunctionMetadata ->
            slashFunctionMetadata.func.findAnnotation<TopLevelSlashCommandData>()?.let { annotation ->
                // Remove all slash commands with the top level name
                val name = slashFunctionMetadata.path.name
                check(name in missingTopLevels) {
                    val refs = functions
                        .filter { it.path.name == name && it.func.hasAnnotation<TopLevelSlashCommandData>() }
                        .joinAsList { it.func.shortSignature }
                    "Cannot have multiple ${annotationRef<TopLevelSlashCommandData>()} on a same top-level command '$name':\n$refs"
                }

                missingTopLevels.remove(name)
                topLevelMetadata[name] = TopLevelSlashCommandMetadata(name, annotation, slashFunctionMetadata)
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
                topLevelMetadata.putIfAbsentOrThrow(name, TopLevelSlashCommandMetadata(name, defaultTopLevelMetadata, slashFunctionMetadata))
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
                    .mapNotNull { metadata -> metadata.func.findAnnotation<SlashCommandGroupData>() }
                    .also { annotations ->
                        check(annotations.size <= 1) {
                            val refs = groupSubcommands
                                .filter { it.func.hasAnnotation<SlashCommandGroupData>() }
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

    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        topLevelMetadata
            .values
            .forEachWithDelayedExceptions { topLevelMetadata ->
                val topLevelAnnotation = topLevelMetadata.annotation
                if (forceGuildCommands)
                    return@forEachWithDelayedExceptions logger.debug { "Skipping command '${topLevelMetadata.name}' as ${BApplicationConfig::forceGuildCommands.reference} is enabled" }

                if (!manager.isValidScope(topLevelAnnotation.scope)) return@forEachWithDelayedExceptions

                val metadata = topLevelMetadata.metadata
                if (checkTestCommand(manager, metadata.func, topLevelAnnotation.scope, context) != TestState.NO_ANNOTATION) {
                    throwInternal("Test commands on a global scope should have thrown in ${::checkTestCommand.shortSignatureNoSrc}")
                }

                processCommand(manager, topLevelMetadata)
            }
    }

    fun declareGuild(manager: GuildApplicationCommandManager) {
        topLevelMetadata
            .values
            .forEachWithDelayedExceptions { topLevelMetadata ->
                val topLevelAnnotation = topLevelMetadata.annotation

                //Declare as a guild command: remove invalid scopes when commands aren't forced as guild scoped
                if (!forceGuildCommands && !manager.isValidScope(topLevelAnnotation.scope)) return@forEachWithDelayedExceptions

                val metadata = topLevelMetadata.metadata
                val instance = metadata.instance
                val path = metadata.path

                //TODO test
                metadata.commandId?.also { id ->
                    if (!checkCommandId(manager, instance, id, path)) {
                        logger.trace { "Skipping command '$path' as its command ID was rejected on ${manager.guild}" }
                        return@forEachWithDelayedExceptions
                    }
                }

                if (checkTestCommand(manager, metadata.func, topLevelAnnotation.scope, context) == TestState.EXCLUDE) {
                    logger.trace { "Skipping command '$path' as it is a test command on ${manager.guild}" }
                    return@forEachWithDelayedExceptions
                }

                processCommand(manager, topLevelMetadata)
            }
    }

    private fun processCommand(manager: AbstractApplicationCommandManager, topLevelMetadata: TopLevelSlashCommandMetadata) {
        val metadata = topLevelMetadata.metadata
        val annotation = metadata.annotation
        val path = metadata.path

        val name = path.name
        val subcommandsMetadata = topLevelMetadata.subcommands
        val subcommandGroupsMetadata = topLevelMetadata.subcommandGroups
        val isTopLevelOnly = subcommandsMetadata.isEmpty() && subcommandGroupsMetadata.isEmpty()
        val actualScope = if (forceGuildCommands) CommandScope.GUILD else topLevelMetadata.annotation.scope
        manager.slashCommand(name, actualScope, if (isTopLevelOnly) metadata.func.castFunction() else null) {
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

            subcommandsMetadata.forEach { subMetadata ->
                subcommand(subMetadata.path.subname!!, subMetadata.func.castFunction()) {
                    configureSubcommand(manager, subMetadata)
                }
            }

            subcommandGroupsMetadata.values.forEach { groupMetadata ->
                subcommandGroup(groupMetadata.name) {
                    this@subcommandGroup.description = groupMetadata.properties.description.nullIfBlank()

                    groupMetadata.subcommands.forEach { (subname, metadataList) ->
                        metadataList.forEach { subMetadata ->
                            subcommand(subname, subMetadata.func.castFunction()) {
                                configureSubcommand(manager, subMetadata)
                            }
                        }
                    }
                }
            }

            configureBuilder(metadata)

            if (isTopLevelOnly) {
                processOptions((manager as? GuildApplicationCommandManager)?.guild, metadata)
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

        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            val declaredName = kParameter.findDeclarationName()
            when (val optionAnnotation = kParameter.findAnnotation<SlashOption>()) {
                null -> when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> {
                        resolverContainer.requireCustomOption(func, kParameter, SlashOption::class)
                        customOption(declaredName)
                    }
                    else -> generatedOption(
                        declaredName, instance.getGeneratedValueSupplier(
                            guild,
                            metadata.commandId,
                            path,
                            kParameter.findOptionName(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
                else -> {
                    val optionName = optionAnnotation.name.nullIfBlank() ?: declaredName.toDiscordString()
                    if (kParameter.type.jvmErasure.isValue) {
                        val inlineClassType = kParameter.type.jvmErasure
                        when (val varArgs = kParameter.findAnnotation<VarArgs>()) {
                            null -> inlineClassOption(declaredName, optionName, inlineClassType) {
                                configureOption(guild, instance, kParameter, optionAnnotation)
                            }
                            else -> inlineClassOptionVararg(declaredName, inlineClassType, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                                configureOption(guild, instance, kParameter, optionAnnotation)
                            }
                        }
                    } else {
                        when (val varArgs = kParameter.findAnnotation<VarArgs>()) {
                            null -> option(declaredName, optionName) {
                                configureOption(guild, instance, kParameter, optionAnnotation)
                            }
                            else -> optionVararg(declaredName, varArgs.value, varArgs.numRequired, { i -> "${optionName}_$i" }) {
                                configureOption(guild, instance, kParameter, optionAnnotation)
                            }
                        }
                    }
                }
            }
        }
    }

    context(SlashCommandBuilder)
    private fun SlashCommandOptionBuilder.configureOption(guild: Guild?, instance: ApplicationCommand, kParameter: KParameter, optionAnnotation: SlashOption) {
        description = optionAnnotation.description.nullIfBlank()

        kParameter.findAnnotation<LongRange>()?.let { range -> valueRange = ValueRange.ofLong(range.from, range.to) }
        kParameter.findAnnotation<DoubleRange>()?.let { range -> valueRange = ValueRange.ofDouble(range.from, range.to) }
        kParameter.findAnnotation<Length>()?.let { length -> lengthRange = LengthRange.of(length.min, length.max) }

        kParameter.findAnnotation<ChannelTypes>()?.let { channelTypesAnnotation ->
            channelTypes = enumSetOf<ChannelType>().also { types ->
                types += channelTypesAnnotation.value
            }
        }

        processAutocomplete(optionAnnotation)

        usePredefinedChoices = optionAnnotation.usePredefinedChoices
        choices = instance.getOptionChoices(guild, this@SlashCommandBuilder.path, optionName)
    }

    private fun SlashCommandOptionBuilder.processAutocomplete(optionAnnotation: SlashOption) {
        if (optionAnnotation.autocomplete.isNotEmpty()) {
            autocompleteReference(optionAnnotation.autocomplete)
        }
    }
}
