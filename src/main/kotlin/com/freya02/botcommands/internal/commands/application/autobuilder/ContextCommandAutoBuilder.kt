package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.annotations.CommandId
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.autobuilder.metadata.MessageContextFunctionMetadata
import com.freya02.botcommands.internal.commands.application.autobuilder.metadata.UserContextFunctionMetadata
import com.freya02.botcommands.internal.commands.autobuilder.*
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.requiredFilter
import com.freya02.botcommands.internal.utils.withFilter
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation

@BService
internal class ContextCommandAutoBuilder(private val context: BContextImpl, classPathContainer: ClassPathContainer) {
    private val messageFunctions: List<MessageContextFunctionMetadata>
    private val userFunctions: List<UserContextFunctionMetadata>

    init {
        messageFunctions = context.serviceAnnotationsMap.getClassesWithAnnotation<Command>()
            .flatMap { clazz ->
                clazz.declaredMemberFunctions
                    .withFilter(FunctionFilter.annotation<JDAMessageCommand>())
                    .requiredFilter(FunctionFilter.nonStatic())
                    .requiredFilter(FunctionFilter.firstArg(GlobalMessageEvent::class))
                    .map { ClassPathFunction(context, clazz, it) }
            }
            .map {
                val instanceSupplier: () -> ApplicationCommand = { it.asCommandInstance() }
                val func = it.function
                val annotation = func.findAnnotation<JDAMessageCommand>() ?: throwInternal("@JDAMessageCommand should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                MessageContextFunctionMetadata(instanceSupplier, func, annotation, path, commandId)
            }

        userFunctions = context.serviceAnnotationsMap.getClassesWithAnnotation<Command>()
            .flatMap { clazz ->
                clazz.declaredMemberFunctions
                    .withFilter(FunctionFilter.annotation<JDAUserCommand>())
                    .requiredFilter(FunctionFilter.nonStatic())
                    .requiredFilter(FunctionFilter.firstArg(GlobalUserEvent::class))
                    .map { ClassPathFunction(context, clazz, it) }
            }
            .map {
                val instanceSupplier: () -> ApplicationCommand = { it.asCommandInstance() }
                val func = it.function
                val annotation = func.findAnnotation<JDAUserCommand>() ?: throwInternal("@JDAMessageCommand should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                UserContextFunctionMetadata(instanceSupplier, func, annotation, path, commandId)
            }
    }

    //Separated functions so message errors don't prevent user commands from being registered
    fun declareGlobalMessage(manager: GlobalApplicationCommandManager) = declareMessage(manager)

    fun declareGlobalUser(manager: GlobalApplicationCommandManager) = declareUser(manager)

    fun declareGuildMessage(manager: GuildApplicationCommandManager) = declareMessage(manager)

    fun declareGuildUser(manager: GuildApplicationCommandManager) = declareUser(manager)

    private fun declareMessage(manager: AbstractApplicationCommandManager) {
        messageFunctions.forEachWithDelayedExceptions {
            val annotation = it.annotation

            if (!manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

            processMessageCommand(manager, it)
        }
    }

    private fun declareUser(manager: AbstractApplicationCommandManager) {
        userFunctions.forEachWithDelayedExceptions {
            val annotation = it.annotation

            if (!manager.isValidScope(annotation.scope)) return@forEachWithDelayedExceptions

            processUserCommand(manager, it)
        }
    }

    private fun processMessageCommand(manager: AbstractApplicationCommandManager, metadata: MessageContextFunctionMetadata) {
        val func = metadata.func
        val instance = metadata.instance
        val path = metadata.path

        //TODO test
        val commandId = metadata.commandId?.also {
            if (!checkCommandId(manager, instance, it, path)) {
                return
            }
        }

        if (!checkTestCommand(manager, func, metadata.annotation.scope, context)) {
            return
        }

        val annotation = metadata.annotation
        manager.messageCommand(path.name, annotation.scope, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func, annotation)

            defaultLocked = annotation.defaultLocked

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }

    private fun processUserCommand(manager: AbstractApplicationCommandManager, metadata: UserContextFunctionMetadata) {
        val func = metadata.func
        val instance = metadata.instance
        val path = metadata.path

        //TODO test
        val commandId = metadata.commandId?.also {
            if (!checkCommandId(manager, instance, it, path)) {
                return
            }
        }

        if (!checkTestCommand(manager, func, metadata.annotation.scope, context)) {
            return
        }

        val annotation = metadata.annotation
        manager.userCommand(path.name, annotation.scope, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func, annotation)

            defaultLocked = annotation.defaultLocked

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }

    private fun ApplicationCommandBuilder<*>.processOptions(
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