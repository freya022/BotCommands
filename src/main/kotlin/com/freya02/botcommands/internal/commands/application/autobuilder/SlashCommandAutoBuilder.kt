package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.*
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.annotations.CommandId
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.autobuilder.fillApplicationCommandBuilder
import com.freya02.botcommands.internal.commands.autobuilder.fillCommandBuilder
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
internal class SlashCommandAutoBuilder(private val autocompleteHandlerContainer: AutocompleteHandlerContainer, classPathContainer: ClassPathContainer) {
    private val functions: List<ClassPathFunction>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDASlashCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalSlashEvent::class)
    }

    @AppDeclaration
    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        functions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDASlashCommand>()!!

            if (!annotation.scope.isGlobal) return@forEach

            processCommand(manager, annotation, func, it)
        }
    }

    @AppDeclaration
    fun declareGuild(manager: GuildApplicationCommandManager) {
        functions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDASlashCommand>()!!

            if (annotation.scope.isGlobal) return@forEach

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

                    kParameter.findAnnotation<com.freya02.botcommands.api.commands.application.slash.annotations.LongRange>()?.let { range -> valueRange = ValueRange(range.from, range.to) }
                    kParameter.findAnnotation<com.freya02.botcommands.api.commands.application.slash.annotations.DoubleRange>()?.let { range -> valueRange = ValueRange(range.from, range.to) }

                    kParameter.findAnnotation<com.freya02.botcommands.api.commands.application.slash.annotations.ChannelTypes>()?.let { channelTypesAnnotation ->
                        channelTypes = enumSetOf<ChannelType>().also { types ->
                            types += channelTypesAnnotation.value
                        }
                    }

                    processAutocomplete(optionAnnotation, func)

                    choices = instance.getOptionChoices(null, path, optionIndex)

                    optionIndex++
                }
            }
        }
    }

    private fun SlashCommandOptionBuilder.processAutocomplete(
        optionAnnotation: AppOption,
        func: KFunction<*>
    ) {
        if (optionAnnotation.autocomplete.isNotEmpty()) {
            autocomplete {
                val autocompleteFunction = autocompleteHandlerContainer[optionAnnotation.autocomplete]?.function ?: throwUser(
                    func,
                    "Autocomplete handler '${optionAnnotation.autocomplete}' was not found"
                )

                @Suppress("UNCHECKED_CAST")
                this@autocomplete.function = autocompleteFunction as KFunction<Collection<*>>

                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotation<AutocompleteHandler>()!!

                mode = autocompleteHandlerAnnotation.mode
                showUserInput = autocompleteHandlerAnnotation.showUserInput

                autocompleteFunction.findAnnotation<CacheAutocomplete>()?.let { autocompleteCacheAnnotation ->
                    cache {
                        cacheMode = autocompleteCacheAnnotation.cacheMode
                        cacheSize = autocompleteCacheAnnotation.cacheSize

                        userLocal = autocompleteCacheAnnotation.userLocal
                        channelLocal = autocompleteCacheAnnotation.channelLocal
                        guildLocal = autocompleteCacheAnnotation.guildLocal
                    }
                }
            }
        }
    }
}