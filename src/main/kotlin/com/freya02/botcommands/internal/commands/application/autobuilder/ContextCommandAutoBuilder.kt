package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.IApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.CommandId
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.user.GlobalUserEvent
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

    //Separated functions so message errors don't prevent user commands from being registered
    @AppDeclaration
    fun declareGlobalMessage(manager: GlobalApplicationCommandManager) = declareMessage(manager)

    @AppDeclaration
    fun declareGlobalUser(manager: GlobalApplicationCommandManager) = declareUser(manager)

    @AppDeclaration
    fun declareGuildMessage(manager: GuildApplicationCommandManager) = declareMessage(manager)

    @AppDeclaration
    fun declareGuildUser(manager: GuildApplicationCommandManager) = declareUser(manager)

    private fun declareMessage(manager: IApplicationCommandManager) {
        messageFunctions.forEachWithDelayedExceptions {
            val func = it.function
            val annotation = func.findAnnotation<JDAMessageCommand>() ?: throwInternal("@JDAMessageCommand should be present")

            if (manager is GuildApplicationCommandManager && annotation.scope.isGlobal) return@forEachWithDelayedExceptions
            if (manager is GlobalApplicationCommandManager && !annotation.scope.isGlobal) return@forEachWithDelayedExceptions

            processMessageCommand(manager, annotation, func, it)
        }
    }

    private fun declareUser(manager: IApplicationCommandManager) {
        userFunctions.forEachWithDelayedExceptions {
            val func = it.function
            val annotation = func.findAnnotation<JDAUserCommand>() ?: throwInternal("@JDAUserCommand should be present")

            if (manager is GuildApplicationCommandManager && annotation.scope.isGlobal) return@forEachWithDelayedExceptions
            if (manager is GlobalApplicationCommandManager && !annotation.scope.isGlobal) return@forEachWithDelayedExceptions

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
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func)

            defaultLocked = annotation.defaultLocked

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
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
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func)

            defaultLocked = annotation.defaultLocked

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }

    private fun String.nullIfEmpty(): String? = when {
        isEmpty() -> null
        else -> this
    }

    private fun ApplicationCommandBuilder.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        commandId: String?
    ) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            when (val optionAnnotation = kParameter.findAnnotation<com.freya02.botcommands.api.commands.application.annotations.AppOption>()) {
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