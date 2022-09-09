package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.IApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppOption
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
import com.freya02.botcommands.internal.commands.application.autobuilder.metadata.MessageContextFunctionMetadata
import com.freya02.botcommands.internal.commands.application.autobuilder.metadata.UserContextFunctionMetadata
import com.freya02.botcommands.internal.commands.autobuilder.*
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
internal class ContextCommandAutoBuilder(classPathContainer: ClassPathContainer) {
    private val messageFunctions: List<MessageContextFunctionMetadata>
    private val userFunctions: List<UserContextFunctionMetadata>

    init {
        messageFunctions = classPathContainer.functionsWithAnnotation<JDAMessageCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalMessageEvent::class)
            .map {
                val instance = it.instance as? ApplicationCommand
                    ?: throwUser(it.function, "Declaring class must extend ${ApplicationCommand::class.simpleName}")
                val func = it.function
                val annotation = func.findAnnotation<JDAMessageCommand>() ?: throwInternal("@JDAMessageCommand should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                MessageContextFunctionMetadata(instance, func, annotation, path, commandId)
            }

        userFunctions = classPathContainer.functionsWithAnnotation<JDAUserCommand>()
            .requireNonStatic()
            .requireFirstArg(GlobalUserEvent::class)
            .map {
                val instance = it.instance as? ApplicationCommand
                    ?: throwUser(it.function, "Declaring class must extend ${ApplicationCommand::class.simpleName}")
                val func = it.function
                val annotation = func.findAnnotation<JDAUserCommand>() ?: throwInternal("@JDAMessageCommand should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                UserContextFunctionMetadata(instance, func, annotation, path, commandId)
            }
    }

    //Separated functions so message errors don't prevent user commands from being registered
    fun declareGlobalMessage(manager: GlobalApplicationCommandManager) = declareMessage(manager)

    fun declareGlobalUser(manager: GlobalApplicationCommandManager) = declareUser(manager)

    fun declareGuildMessage(manager: GuildApplicationCommandManager) = declareMessage(manager)

    fun declareGuildUser(manager: GuildApplicationCommandManager) = declareUser(manager)

    private fun declareMessage(manager: IApplicationCommandManager) {
        messageFunctions.forEachWithDelayedExceptions {
            val annotation = it.annotation

            if (!manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

            processMessageCommand(manager, it)
        }
    }

    private fun declareUser(manager: IApplicationCommandManager) {
        userFunctions.forEachWithDelayedExceptions {
            val annotation = it.annotation

            if (!manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

            processUserCommand(manager, it)
        }
    }

    private fun processMessageCommand(manager: IApplicationCommandManager, metadata: MessageContextFunctionMetadata) {
        val func = metadata.func
        val instance = metadata.instance
        val path = metadata.path

        //TODO test
        val commandId = metadata.commandId?.also {
            if (!checkCommandId(manager, instance, it, path)) {
                return
            }
        }

        val annotation = metadata.annotation
        manager.messageCommand(path.name, annotation.scope) {
            fillCommandBuilder(func)
            addFunction(metadata.func)
            fillApplicationCommandBuilder(func)

            defaultLocked = annotation.defaultLocked

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }

    private fun processUserCommand(manager: IApplicationCommandManager, metadata: UserContextFunctionMetadata) {
        val func = metadata.func
        val instance = metadata.instance
        val path = metadata.path

        //TODO test
        val commandId = metadata.commandId?.also {
            if (!checkCommandId(manager, instance, it, path)) {
                return
            }
        }

        val annotation = metadata.annotation
        manager.userCommand(path.name, annotation.scope) {
            fillCommandBuilder(func)
            addFunction(metadata.func)
            fillApplicationCommandBuilder(func)

            defaultLocked = annotation.defaultLocked

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }

    private fun checkCommandId(manager: IApplicationCommandManager, instance: ApplicationCommand, it: String, path: CommandPath): Boolean {
        if (manager is GuildApplicationCommandManager) {
            val guildIds = instance.getGuildsForCommandId(it, path) ?: return true

            if (manager.guild.idLong !in guildIds) {
                return false //Don't push command if it isn't allowed
            }
        }

        return true
    }

    private fun ApplicationCommandBuilder.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        commandId: String?
    ) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            when (val optionAnnotation = kParameter.findAnnotation<AppOption>()) {
                null -> when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> customOption(kParameter.findDeclarationName())
                    else -> generatedOption(
                        kParameter.findDeclarationName(), instance.getGeneratedValueSupplier(
                            guild,
                            commandId,
                            CommandPath.ofName(name),
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