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
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
internal class SlashCommandAutoBuilder(classPathContainer: ClassPathContainer) {
    private val functions: List<ClassPathFunction>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDASlashCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalSlashEvent::class)
    }

    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        functions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDASlashCommand>() ?: throwInternal("@JDASlashCommand should be present")

            if (!annotation.scope.isGlobal) return@forEach

            processCommand(manager, annotation, func, it)
        }
    }

    fun declareGuild(manager: GuildApplicationCommandManager) {
        functions.forEachWithDelayedExceptions {
            val func = it.function
            val annotation = func.findAnnotation<JDASlashCommand>() ?: throwInternal("@JDASlashCommand should be present")

            if (annotation.scope.isGlobal) return@forEachWithDelayedExceptions

            processCommand(manager, annotation, func, it)
        }
    }

    private fun processCommand(
        manager: IApplicationCommandManager,
        annotation: JDASlashCommand,
        func: KFunction<*>,
        classPathFunction: ClassPathFunction
    ) {
        val instance = classPathFunction.instance as? ApplicationCommand ?: throwUser(
            classPathFunction.function,
            "Declaring class must extend ${ApplicationCommand::class.simpleName}"
        )

        val path = CommandPath.of(annotation.name, annotation.group.nullIfEmpty(), annotation.subcommand.nullIfEmpty())

        //TODO test
        val commandId = func.findAnnotation<CommandId>()?.value?.also {
            if (manager is GuildApplicationCommandManager) {
                val guildIds = instance.getGuildsForCommandId(it, path) ?: return@also

                if (manager.guild.idLong !in guildIds) {
                    return //Don't push command if it isn't allowed
                }
            }
        }

        manager.slashCommand(path, annotation.scope) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func)

            defaultLocked = annotation.defaultLocked
            description = annotation.description

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }

    private fun String.nullIfEmpty(): String? = when {
        isEmpty() -> null
        else -> this
    }

    private fun SlashCommandBuilder.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        commandId: String?
    ) {
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
                else -> option(kParameter.findDeclarationName(), optionAnnotation.name.nullIfEmpty() ?: kParameter.findDeclarationName().asDiscordString()) {
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
}