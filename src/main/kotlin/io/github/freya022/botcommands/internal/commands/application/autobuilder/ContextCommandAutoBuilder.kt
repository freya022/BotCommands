package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.MessageContextFunctionMetadata
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.UserContextFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.findOptionName
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@BService
internal class ContextCommandAutoBuilder(
    private val context: BContextImpl,
    private val resolverContainer: ResolverContainer
) {
    private val messageFunctions: List<MessageContextFunctionMetadata>
    private val userFunctions: List<UserContextFunctionMetadata>

    init {
        messageFunctions = context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, JDAMessageCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalMessageEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDAMessageCommand>() ?: throwInternal("@JDAMessageCommand should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                MessageContextFunctionMetadata(it, annotation, path, commandId)
            }

        userFunctions = context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, JDAUserCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalUserEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDAUserCommand>() ?: throwInternal("@JDAMessageCommand should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                UserContextFunctionMetadata(it, annotation, path, commandId)
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

        if (checkTestCommand(manager, func, metadata.annotation.scope, context) == TestState.EXCLUDE) {
            return
        }

        val annotation = metadata.annotation
        manager.messageCommand(path.name, annotation.scope, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func, annotation)

            isDefaultLocked = annotation.defaultLocked

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

        if (checkTestCommand(manager, func, metadata.annotation.scope, context) == TestState.EXCLUDE) {
            return
        }

        val annotation = metadata.annotation
        manager.userCommand(path.name, annotation.scope, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func, annotation)

            isDefaultLocked = annotation.defaultLocked

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
            if (kParameter.hasAnnotation<ContextOption>()) {
                when (this) {
                    is UserCommandBuilder -> option(kParameter.findDeclarationName())
                    is MessageCommandBuilder -> option(kParameter.findDeclarationName())
                }
            } else {
                when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> {
                        resolverContainer.requireCustomOption(func, kParameter, ContextOption::class)
                        customOption(kParameter.findDeclarationName())
                    }
                    else -> generatedOption(
                        kParameter.findDeclarationName(), instance.getGeneratedValueSupplier(
                            guild,
                            commandId,
                            CommandPath.ofName(name),
                            kParameter.findOptionName(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
            }
        }
    }
}