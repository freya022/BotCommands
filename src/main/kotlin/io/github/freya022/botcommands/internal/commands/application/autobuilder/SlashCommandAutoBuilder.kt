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
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.SlashFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

@BService
internal class SlashCommandAutoBuilder(
    private val context: BContextImpl,
    private val resolverContainer: ResolverContainer
) {
    private val logger = KotlinLogging.logger { }

    private val forceGuildCommands = context.applicationConfig.forceGuildCommands

    private val functions: List<SlashFunctionMetadata> =
        context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, JDASlashCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalSlashEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDASlashCommand>() ?: throwInternal("@JDASlashCommand should be present")
                val path = CommandPath.of(annotation.name, annotation.group.nullIfBlank(), annotation.subcommand.nullIfBlank()).also { path ->
                    if (path.group != null && path.nameCount == 2) {
                        throwUser(func, "Slash commands with groups need to have their subcommand name set")
                    }
                }
                val commandId = func.findAnnotation<CommandId>()?.value

                SlashFunctionMetadata(it, annotation, path, commandId)
            }

    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
        val subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata> = hashMapOf()
        fillSubcommandsAndGroups(subcommands, subcommandGroups)

        functions
            .distinctBy { it.path.name } //Subcommands are handled by processCommand, only retain one metadata per top-level name
            .forEachWithDelayedExceptions {
                val annotation = it.annotation
                if (forceGuildCommands || !manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

                if (checkTestCommand(manager, it.func, annotation.scope, context) == TestState.EXCLUDE) {
                    return@forEachWithDelayedExceptions
                }

                processCommand(manager, it, subcommands, subcommandGroups)
            }
    }

    fun declareGuild(manager: GuildApplicationCommandManager) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
        val subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata> = hashMapOf()
        fillSubcommandsAndGroups(subcommands, subcommandGroups)

        functions
            .distinctBy { it.path.name } //Subcommands are handled by processCommand, only retain one metadata per top-level name
            .forEachWithDelayedExceptions { metadata ->
                val annotation = metadata.annotation
                //Declare as a guild command: remove invalid scopes when commands aren't forced as guild scoped
                if (!forceGuildCommands && !manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

                val instance = metadata.instance
                val path = metadata.path

                //TODO test
                metadata.commandId?.also { id ->
                    if (!checkCommandId(manager, instance, id, path)) {
                        logger.trace { "Skipping command '$path' as its command ID was rejected on ${manager.guild}" }
                        return@forEachWithDelayedExceptions
                    }
                }

                if (checkTestCommand(manager, metadata.func, annotation.scope, context) == TestState.EXCLUDE) {
                    logger.trace { "Skipping command '$path' as it is a test command on ${manager.guild}" }
                    return@forEachWithDelayedExceptions
                }

                processCommand(manager, metadata, subcommands, subcommandGroups)
            }
    }

    private fun fillSubcommandsAndGroups(
        subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>>,
        subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata>
    ) {
        functions.forEachWithDelayedExceptions { metadata ->
            when (metadata.path.nameCount) {
                2 -> subcommands.computeIfAbsent(metadata.path.name) { arrayListOf() }.add(metadata)
                3 -> subcommandGroups
                    .computeIfAbsent(metadata.path.name) { SlashSubcommandGroupMetadata(metadata.path.group!!, metadata.annotation.description) }
                    .subcommands
                    .computeIfAbsent(metadata.path.subname!!) { arrayListOf() }
                    .add(metadata)
            }
        }
    }

    private fun processCommand(
        manager: AbstractApplicationCommandManager,
        metadata: SlashFunctionMetadata,
        subcommands: Map<String, List<SlashFunctionMetadata>>,
        subcommandGroups: Map<String, SlashSubcommandGroupMetadata>
    ) {
        val annotation = metadata.annotation
        val instance = metadata.instance
        val path = metadata.path
        val commandId = metadata.commandId

        val name = path.name
        val subcommandsMetadata = subcommands[name]
        val subcommandGroupsMetadata = subcommandGroups[name]
        val isTopLevel = subcommandsMetadata == null && subcommandGroupsMetadata == null
        manager.slashCommand(name, annotation.scope, if (isTopLevel) metadata.func.castFunction() else null) {
            isDefaultLocked = annotation.defaultLocked
            description = annotation.description

            subcommandsMetadata?.let { metadataList ->
                metadataList.forEach { subMetadata ->
                    subcommand(subMetadata.path.subname!!, subMetadata.func.castFunction()) {
                        //TODO replace with #subcommandDescription in annotation
                        this@subcommand.description = subMetadata.annotation.description
                        this@subcommand.configureBuilder(subMetadata)
                        this@subcommand.processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata, instance, commandId)
                    }
                }
            }

            subcommandGroupsMetadata?.let { groupMetadata ->
                subcommandGroup(groupMetadata.name) {
                    this@subcommandGroup.description = groupMetadata.description

                    groupMetadata.subcommands.forEach { (subname, metadataList) ->
                        metadataList.forEach { subMetadata ->
                            subcommand(subname, subMetadata.func.castFunction()) {
                                this@subcommand.configureBuilder(subMetadata)
                                this@subcommand.processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata, instance, commandId)
                            }
                        }
                    }
                }
            }

            configureBuilder(metadata)

            if (isTopLevel) {
                processOptions((manager as? GuildApplicationCommandManager)?.guild, metadata, instance, commandId)
            }
        }
    }

    private fun SlashCommandBuilder.configureBuilder(metadata: SlashFunctionMetadata) {
        fillCommandBuilder(metadata.func)
        fillApplicationCommandBuilder(metadata.func, metadata.annotation)
    }

    private fun SlashCommandBuilder.processOptions(
        guild: Guild?,
        metadata: SlashFunctionMetadata,
        instance: ApplicationCommand,
        commandId: String?
    ) {
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
                            commandId,
                            path,
                            kParameter.findOptionName(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
                else -> {
                    val optionName = optionAnnotation.name.nullIfBlank() ?: declaredName.toDiscordString()
                    if (kParameter.type.jvmErasure.isValue) {
                        val inlineClassType = kParameter.type.jvmErasure.java
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
        description = optionAnnotation.description

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

    private class SlashSubcommandGroupMetadata(val name: String, val description: String) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
    }
}
