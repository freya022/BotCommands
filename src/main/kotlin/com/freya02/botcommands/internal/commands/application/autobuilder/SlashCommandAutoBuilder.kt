package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.*
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.annotations.CommandId
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.*
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autobuilder.metadata.SlashFunctionMetadata
import com.freya02.botcommands.internal.commands.autobuilder.*
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import kotlin.reflect.full.findAnnotation

@BService
internal class SlashCommandAutoBuilder(private val context: BContextImpl, classPathContainer: ClassPathContainer) {
    private val functions: List<SlashFunctionMetadata>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDASlashCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalSlashEvent::class))
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
        manager: IApplicationCommandManager,
        metadata: SlashFunctionMetadata,
        subcommands: Map<String, List<SlashFunctionMetadata>>,
        subcommandGroups: Map<String, SlashSubcommandGroupMetadata>
    ) {
        val annotation = metadata.annotation
        val instance = metadata.instance
        val path = metadata.path
        val commandId = metadata.commandId

        manager.slashCommand(path.name, annotation.scope) {
            defaultLocked = annotation.defaultLocked
            description = annotation.description

            val subcommandsMetadata = subcommands[name]
            subcommandsMetadata?.let { metadataList ->
                metadataList.forEach { subMetadata ->
                    subcommand(subMetadata.path.subname!!) {
                        //TODO replace with #subcommandDescription in annotation
                        this@subcommand.description = subMetadata.annotation.description
                        this@subcommand.configureBuilder(subMetadata)
                        this@subcommand.addFunction(subMetadata.func)
                        this@subcommand.processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata, instance, commandId)
                    }
                }
            }

            val subcommandGroupsMetadata = subcommandGroups[name]
            subcommandGroupsMetadata?.let { groupMetadata ->
                subcommandGroup(groupMetadata.name) {
                    this@subcommandGroup.description = groupMetadata.description

                    groupMetadata.subcommands.forEach { (subname, metadataList) ->
                        metadataList.forEach { subMetadata ->
                            subcommand(subname) {
                                this@subcommand.configureBuilder(subMetadata)
                                this@subcommand.addFunction(subMetadata.func)
                                this@subcommand.processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata, instance, commandId)
                            }
                        }
                    }
                }
            }

            configureBuilder(metadata)

            val isTopLevel = subcommandsMetadata == null && subcommandGroupsMetadata == null
            if (isTopLevel) {
                addFunction(metadata.func)
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

        var optionIndex = 0
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            when (val optionAnnotation = kParameter.findAnnotation<AppOption>()) {
                null -> when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> customOption(kParameter.findDeclarationName())
                    else -> generatedOption(
                        kParameter.findDeclarationName(), instance.getGeneratedValueSupplier(
                            guild,
                            commandId,
                            path,
                            kParameter.findOptionName().asDiscordString(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
                else -> option(
                    kParameter.findDeclarationName(),
                    optionAnnotation.name.nullIfEmpty() ?: kParameter.findDeclarationName().asDiscordString()
                ) {
                    description = optionAnnotation.description.nullIfEmpty() ?: "No description"

                    kParameter.findAnnotation<LongRange>()?.let { range -> valueRange = ValueRange.ofLong(range.from, range.to) }
                    kParameter.findAnnotation<DoubleRange>()?.let { range -> valueRange = ValueRange.ofDouble(range.from, range.to) }
                    kParameter.findAnnotation<Length>()?.let { length -> lengthRange = LengthRange.of(length.min, length.max) }

                    kParameter.findAnnotation<ChannelTypes>()?.let { channelTypesAnnotation ->
                        channelTypes = enumSetOf<ChannelType>().also { types ->
                            types += channelTypesAnnotation.value
                        }
                    }

                    processAutocomplete(optionAnnotation)

                    choices = instance.getOptionChoices(guild, path, optionName)

                    optionIndex++
                }
            }
        }
    }

    private fun SlashCommandOptionBuilder.processAutocomplete(optionAnnotation: AppOption) {
        if (optionAnnotation.autocomplete.isNotEmpty()) {
            autocompleteReference(optionAnnotation.autocomplete)
        }
    }

    private class SlashSubcommandGroupMetadata(val name: String, val description: String) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
    }
}