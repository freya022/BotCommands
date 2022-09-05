package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.*
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.annotations.CommandId
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.api.commands.application.slash.annotations.DoubleRange
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.autobuilder.fillApplicationCommandBuilder
import com.freya02.botcommands.internal.commands.autobuilder.fillCommandBuilder
import com.freya02.botcommands.internal.commands.autobuilder.forEachWithDelayedExceptions
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
internal class SlashCommandAutoBuilder(classPathContainer: ClassPathContainer) {
    private val functions: List<SlashFunctionMetadata>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDASlashCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalSlashEvent::class)
            .map {
                val instance = it.instance as? ApplicationCommand
                    ?: throwUser(it.function, "Declaring class must extend ${ApplicationCommand::class.simpleName}")
                val func = it.function
                val annotation = func.findAnnotation<JDASlashCommand>() ?: throwInternal("@JDASlashCommand should be present")
                val path = CommandPath.of(annotation.name, annotation.group, annotation.subcommand).also { path ->
                    if (path.group != null && path.nameCount == 2) {
                        throwUser(func, "Slash commands with groups need to have their subcommand name set")
                    }
                }
                val commandId = func.findAnnotation<CommandId>()?.value

                SlashFunctionMetadata(instance, func, annotation, path, commandId)
            }
    }

    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
        val subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata> = hashMapOf()
        fillSubcommandsAndGroups(subcommands, subcommandGroups)

        functions.forEachWithDelayedExceptions {
            val annotation = it.annotation
            if (!annotation.scope.isGlobal) return@forEachWithDelayedExceptions

            processCommand(manager, it, subcommands, subcommandGroups)
        }
    }

    fun declareGuild(manager: GuildApplicationCommandManager) {
        val subcommands: MutableMap<String, MutableList<SlashFunctionMetadata>> = hashMapOf()
        val subcommandGroups: MutableMap<String, SlashSubcommandGroupMetadata> = hashMapOf()
        fillSubcommandsAndGroups(subcommands, subcommandGroups)

        functions.forEachWithDelayedExceptions { metadata ->
            val instance = metadata.instance
            val annotation = metadata.annotation
            val path = metadata.path

            if (annotation.scope.isGlobal) return@forEachWithDelayedExceptions

            //TODO test
            metadata.commandId?.also { id ->
                val guildIds = instance.getGuildsForCommandId(id, path) ?: return@also

                if (manager.guild.idLong !in guildIds) {
                    return@forEachWithDelayedExceptions //Don't push command if it isn't allowed
                }
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
                    .computeIfAbsent(metadata.path.group!!) { arrayListOf() }
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
            configureBuilder(metadata)

            defaultLocked = annotation.defaultLocked
            description = annotation.description

            subcommands[name]?.let { metadataList ->
                metadataList.forEach { subMetadata ->
                    subcommand(subMetadata.path.subname!!) {
                        configureBuilder(subMetadata)
                        processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata, instance, commandId)
                    }
                }
            }

            subcommandGroups[name]?.let { groupMetadata ->
                subcommandGroup(groupMetadata.name) {
                    description = groupMetadata.description

                    groupMetadata.subcommands.forEach { (subname, metadataList) ->
                        metadataList.forEach { subMetadata ->
                            subcommand(subname) {
                                configureBuilder(subMetadata)
                                processOptions((manager as? GuildApplicationCommandManager)?.guild, subMetadata, instance, commandId)
                            }
                        }
                    }
                }
            }

            processOptions((manager as? GuildApplicationCommandManager)?.guild, metadata, instance, commandId)
        }
    }

    private fun SlashCommandBuilder.configureBuilder(metadata: SlashFunctionMetadata) {
        fillCommandBuilder(metadata.func)
        fillApplicationCommandBuilder(metadata.func)
    }

    private fun String.nullIfEmpty(): String? = when {
        isEmpty() -> null
        else -> this
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

                    kParameter.findAnnotation<LongRange>()?.let { range -> valueRange = ValueRange(range.from, range.to) }
                    kParameter.findAnnotation<DoubleRange>()?.let { range -> valueRange = ValueRange(range.from, range.to) }

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

    private class SlashFunctionMetadata(
        val instance: ApplicationCommand,
        val func: KFunction<*>,
        val annotation: JDASlashCommand,
        val path: CommandPath,
        val commandId: String?
    )
}