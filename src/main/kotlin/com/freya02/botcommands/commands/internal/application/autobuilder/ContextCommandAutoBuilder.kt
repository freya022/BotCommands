package com.freya02.botcommands.commands.internal.application.autobuilder

import com.freya02.botcommands.annotations.api.annotations.CommandId
import com.freya02.botcommands.annotations.api.annotations.Cooldown
import com.freya02.botcommands.annotations.api.annotations.NSFW
import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.annotations.api.application.annotations.GeneratedOption
import com.freya02.botcommands.annotations.api.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.annotations.api.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.*
import com.freya02.botcommands.api.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.application.builder.MessageCommandBuilder
import com.freya02.botcommands.api.application.builder.UserCommandBuilder
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ClassPathFunction
import com.freya02.botcommands.core.internal.requireFirstArg
import com.freya02.botcommands.core.internal.requireNonStatic
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.findOptionName
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.AnnotationUtils
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
internal class ContextCommandAutoBuilder(classPathContainer: ClassPathContainer) {
    private val messageFunctions: List<ClassPathFunction>
    private val userFunctions: List<ClassPathFunction>

    init {
        messageFunctions = classPathContainer.functionsWithAnnotation<JDAMessageCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalMessageEvent::class)

        userFunctions = classPathContainer.functionsWithAnnotation<JDAUserCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalUserEvent::class)
    }

    @Declaration
    fun declareGlobal(manager: GlobalApplicationCommandManager) = declare(manager)

    @Declaration
    fun declareGuild(manager: GuildApplicationCommandManager) = declare(manager)

    private fun declare(manager: IApplicationCommandManager) {
        messageFunctions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDAMessageCommand>()!!

            if (manager is GuildApplicationCommandManager && annotation.scope.isGlobal) return@forEach
            if (manager is GlobalApplicationCommandManager && !annotation.scope.isGlobal) return@forEach

            processMessageCommand(manager, annotation, func, it)
        }

        userFunctions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDAUserCommand>()!!

            if (manager is GuildApplicationCommandManager && annotation.scope.isGlobal) return@forEach
            if (manager is GlobalApplicationCommandManager && !annotation.scope.isGlobal) return@forEach

            processUserCommand(manager, annotation, func, it)
        }
    }

    private fun processMessageCommand(
        manager: IApplicationCommandManager,
        annotation: JDAMessageCommand,
        func: KFunction<*>,
        classPathFunction: ClassPathFunction
    ) {
        val instance = classPathFunction.instance as? ApplicationCommand ?: throwUser(
            classPathFunction.function,
            "Declaring class must extend ${ApplicationCommand::class.simpleName}"
        )

        val path = CommandPath.ofName(annotation.name)

        //TODO test
        val commandId = func.findAnnotation<CommandId>()?.value?.also {
            if (manager is GuildApplicationCommandManager) {
                val guildIds = instance.getGuildsForCommandId(it, path) ?: return@also

                if (manager.guild.idLong !in guildIds) {
                    return //Don't push command if it isn't allowed
                }
            }
        }

        manager.messageCommand(path.name, annotation.scope) {
            defaultLocked = annotation.defaultLocked

            processApplicationCommandBuilder(func, commandId, manager, instance)
        }
    }

    private fun processUserCommand(
        manager: IApplicationCommandManager,
        annotation: JDAUserCommand,
        func: KFunction<*>,
        classPathFunction: ClassPathFunction
    ) {
        val instance = classPathFunction.instance as? ApplicationCommand ?: throwUser(
            classPathFunction.function,
            "Declaring class must extend ${ApplicationCommand::class.simpleName}"
        )

        val path = CommandPath.ofName(annotation.name)

        //TODO test
        val commandId = func.findAnnotation<CommandId>()?.value?.also {
            if (manager is GuildApplicationCommandManager) {
                val guildIds = instance.getGuildsForCommandId(it, path) ?: return@also

                if (manager.guild.idLong !in guildIds) {
                    return //Don't push command if it isn't allowed
                }
            }
        }

        manager.userCommand(path.name, annotation.scope) {
            defaultLocked = annotation.defaultLocked

            processApplicationCommandBuilder(func, commandId, manager, instance)
        }
    }

    private fun ApplicationCommandBuilder.processApplicationCommandBuilder(
        func: KFunction<*>,
        commandId: String?,
        manager: IApplicationCommandManager,
        instance: ApplicationCommand
    ) {
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

        processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance)

        @Suppress("UNCHECKED_CAST")
        function = func as KFunction<Any>
    }

    private fun String.nullIfEmpty(): String? = when {
        isEmpty() -> null
        else -> this
    }

    private fun ApplicationCommandBuilder.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand
    ) {
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

                else -> when (this) {
                    is UserCommandBuilder -> option(optionAnnotation.name.nullIfEmpty() ?: kParameter.findDeclarationName())
                    is MessageCommandBuilder -> option(optionAnnotation.name.nullIfEmpty() ?: kParameter.findDeclarationName())
                }
            }
        }
    }
}