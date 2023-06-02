package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.*
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.annotations.CommandId
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.*
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autobuilder.metadata.SlashFunctionMetadata
import com.freya02.botcommands.internal.commands.autobuilder.*
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.LocalizationUtils
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.requiredFilter
import com.freya02.botcommands.internal.utils.withFilter
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure


@BService
internal class SlashCommandAutoBuilder(private val context: BContextImpl, classPathContainer: ClassPathContainer) {
    private val functions: List<SlashFunctionMetadata>

    init {
        functions = context.serviceAnnotationsMap.getClassesWithAnnotation<Command>()
            .flatMap { clazz ->
                clazz.declaredMemberFunctions
                    .withFilter(FunctionFilter.annotation<JDASlashCommand>())
                    .requiredFilter(FunctionFilter.nonStatic())
                    .requiredFilter(FunctionFilter.firstArg(GlobalSlashEvent::class))
                    .map { ClassPathFunction(context, clazz, it) }
            }
            .map {
                val instanceSupplier: () -> ApplicationCommand = { it.asCommandInstance() }
                val func = it.function
                val annotation = func.findAnnotation<JDASlashCommand>() ?: throwInternal("@JDASlashCommand should be present")
                val path = CommandPath.of(annotation.name, annotation.group.nullIfEmpty(), annotation.subcommand.nullIfEmpty()).also { path ->
                    if (path.group != null && path.nameCount == 2) {
                        throwUser(func, "Slash commands with groups need to have their subcommand name set")
                    }
                }
                val commandId = func.findAnnotation<CommandId>()?.value

                SlashFunctionMetadata(instanceSupplier, func, annotation, path, commandId)
            }
    }

    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
        val subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata> = hashMapOf()
        fillSubcommandsAndGroups(subcommands, subcommandGroups)

        functions
            .distinctBy { it.path.name } //Subcommands are handled by processCommand, only retain one metadata per top-level name
            .forEachWithDelayedExceptions {
                val annotation = it.annotation
                if (!manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

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
                if (!manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

                val instance = metadata.instance
                val path = metadata.path

                //TODO test
                metadata.commandId?.also { id ->
                    if (!checkCommandId(manager, instance, id, path)) {
                        return
                    }
                }

                if (!checkTestCommand(manager, metadata.func, annotation.scope, context)) {
                    return
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
            defaultLocked = annotation.defaultLocked
            description = getEffectiveDescription(annotation)

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
            when (val optionAnnotation = kParameter.findAnnotation<AppOption>()) {
                null -> when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> customOption(declaredName)
                    else -> generatedOption(
                        declaredName, instance.getGeneratedValueSupplier(
                            guild,
                            commandId,
                            path,
                            kParameter.findOptionName().asDiscordString(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
                else -> {
                    val optionName = optionAnnotation.name.nullIfEmpty() ?: declaredName.asDiscordString()
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
    private fun SlashCommandOptionBuilder.configureOption(guild: Guild?, instance: ApplicationCommand, kParameter: KParameter, optionAnnotation: AppOption) {
        description = getEffectiveDescription(optionAnnotation)

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
        choices = instance.getOptionChoices(guild, path, optionName)
    }

    private fun SlashCommandOptionBuilder.processAutocomplete(optionAnnotation: AppOption) {
        if (optionAnnotation.autocomplete.isNotEmpty()) {
            autocompleteReference(optionAnnotation.autocomplete)
        }
    }

    context(TopLevelSlashCommandBuilder)
    private fun getEffectiveDescription(annotation: JDASlashCommand): String {
        val joinedPath = path.getFullPath('.')
        val rootLocalization = LocalizationUtils.getCommandRootLocalization(context, "$joinedPath.description")
        if (rootLocalization != null) return rootLocalization

        return annotation.description
    }

    context(SlashCommandBuilder, SlashCommandOptionBuilder)
    private fun getEffectiveDescription(optionAnnotation: AppOption): String {
        val joinedPath = path.getFullPath('.')
        val rootLocalization = LocalizationUtils.getCommandRootLocalization(context, "$joinedPath.options.$optionName.description")
        if (rootLocalization != null) return rootLocalization

        return optionAnnotation.description.nullIfEmpty() ?: "No description"
    }

    private class SlashSubcommandGroupMetadata(val name: String, val description: String) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
    }
}
