package com.freya02.botcommands.commands.internal.application.autobuilder

import com.freya02.botcommands.annotations.api.annotations.CommandId
import com.freya02.botcommands.annotations.api.annotations.Cooldown
import com.freya02.botcommands.annotations.api.annotations.NSFW
import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.annotations.api.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.annotations.api.application.slash.annotations.DoubleRange
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.annotations.api.application.slash.annotations.LongRange
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompletionHandler
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CacheAutocompletion
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.*
import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ClassPathFunction
import com.freya02.botcommands.core.internal.requireFirstArg
import com.freya02.botcommands.core.internal.requireNonStatic
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.AnnotationUtils
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import com.freya02.botcommands.internal.utils.ReflectionUtils
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

@BService
internal class SlashCommandAutoBuilder(private val context: BContext, classPathContainer: ClassPathContainer) {
    private val functions: List<ClassPathFunction>
    private val autocompleteFunctions: Map<String, ClassPathFunction>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDASlashCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalSlashEvent::class)

        autocompleteFunctions = classPathContainer.functionsWithAnnotation<AutocompletionHandler>()
            .requireNonStatic()
            .requireFirstArg(CommandAutoCompleteInteractionEvent::class)
            .onEach {
                val returnType = it.function.returnType
                ReflectionUtils.getCollectionReturnType(returnType.jvmErasure.java, returnType.javaType) ?: throwUser(
                    it.function,
                    "Autocomplete handler needs to return a Collection"
                )
            }
            .associateBy { it.function.findAnnotation<AutocompletionHandler>()!!.name }
    }

    @Declaration
    fun declareGlobal(manager: GlobalApplicationCommandManager) {
        functions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDASlashCommand>()!!

            if (!annotation.scope.isGlobal) return@forEach

            processCommand(manager, annotation, func, it)
        }
    }

    @Declaration
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
                val guildIds = instance.getGuildsForCommandId(context, it, path) ?: return@also

                if (manager.guild.idLong !in guildIds) {
                    return //Don't push command if it isn't allowed
                }
            }
        }

        manager.slashCommand(path, annotation.scope) {
            defaultLocked = annotation.defaultLocked
            description = annotation.description

            func.findAnnotation<Cooldown>()?.let { cooldownAnnotation ->
                cooldown {
                    scope = cooldownAnnotation.cooldownScope
                    cooldown = cooldownAnnotation.cooldown
                    unit = cooldownAnnotation.unit
                }
            }

            func.findAnnotation<NSFW>()?.let { nsfwAnnotation ->
                nsfw {
                    allowInDMs = nsfwAnnotation.dm
                    allowInGuild = nsfwAnnotation.guild
                }
            }

            userPermissions = AnnotationUtils.getUserPermissions(func)
            botPermissions = AnnotationUtils.getBotPermissions(func)

            testOnly = AnnotationUtils.getEffectiveTestState(func)

            this.commandId = commandId

            processOptions(func, instance)

            @Suppress("UNCHECKED_CAST")
            function = func as KFunction<Any>
        }
    }

    private fun String.nullIfEmpty(): String? = when {
        isEmpty() -> null
        else -> this
    }

    private fun SlashCommandBuilder.processOptions(
        func: KFunction<*>,
        instance: ApplicationCommand
    ) {
        var optionIndex = 0
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            when (val optionAnnotation = kParameter.findAnnotation<AppOption>()) {
                null -> customOption(kParameter.findDeclarationName())
                else -> option(optionAnnotation.name) { //TODO default values
                    description = optionAnnotation.description
                    optional = kParameter.isNullable

                    kParameter.findAnnotation<LongRange>()?.let { range -> valueRange = ValueRange(range.from, range.to) }
                    kParameter.findAnnotation<DoubleRange>()?.let { range -> valueRange = ValueRange(range.from, range.to) }

                    kParameter.findAnnotation<ChannelTypes>()?.let { channelTypesAnnotation ->
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
                val autocompleteFunction = autocompleteFunctions[optionAnnotation.autocomplete]?.function ?: throwUser(
                    func,
                    "Autocomplete handler '${optionAnnotation.autocomplete}' was not found"
                )

                @Suppress("UNCHECKED_CAST")
                this@autocomplete.function = autocompleteFunction as KFunction<Collection<*>>

                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotation<AutocompletionHandler>()!!

                mode = autocompleteHandlerAnnotation.mode
                showUserInput = autocompleteHandlerAnnotation.showUserInput

                autocompleteFunction.findAnnotation<CacheAutocompletion>()?.let { autocompleteCacheAnnotation ->
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