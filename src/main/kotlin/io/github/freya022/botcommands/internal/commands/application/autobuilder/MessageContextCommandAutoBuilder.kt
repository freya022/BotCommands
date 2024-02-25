package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.declaration.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.declaration.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.MessageContextFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.full.findAnnotation
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType

private val logger = KotlinLogging.logger { }

@BService
internal class MessageContextCommandAutoBuilder(
    context: BContextImpl,
    resolverContainer: ResolverContainer
) : ContextCommandAutoBuilder(context, resolverContainer) {
    private val messageFunctions: List<MessageContextFunctionMetadata>

    init {
        messageFunctions = context.instantiableServiceAnnotationsMap
            .getInstantiableFunctionsWithAnnotation<Command, JDAMessageCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalMessageEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDAMessageCommand>() ?: throwInternal("${annotationRef<JDAMessageCommand>()} should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                MessageContextFunctionMetadata(it, annotation, path, commandId)
            }
    }

    //Separated functions so message errors don't prevent user commands from being registered
    fun declareGlobalMessage(manager: GlobalApplicationCommandManager) = declareMessage(manager)

    fun declareGuildMessage(manager: GuildApplicationCommandManager) = declareMessage(manager)

    private fun declareMessage(manager: AbstractApplicationCommandManager) {
        val skipLogger = SkipLogger(logger)
        messageFunctions.forEachWithDelayedExceptions { metadata ->
            runFiltered(
                manager,
                skipLogger,
                forceGuildCommands,
                metadata.path,
                metadata.instance,
                metadata.commandId,
                metadata.func,
                metadata.annotation.scope
            ) { processMessageCommand(manager, metadata) }
        }
        skipLogger.log((manager as? GuildApplicationCommandManager)?.guild, CommandType.MESSAGE)
    }

    private fun processMessageCommand(manager: AbstractApplicationCommandManager, metadata: MessageContextFunctionMetadata) {
        val func = metadata.func
        val instance = metadata.instance
        val path = metadata.path
        val commandId = metadata.commandId

        val annotation = metadata.annotation
        val actualScope = if (forceGuildCommands) CommandScope.GUILD else annotation.scope
        manager.messageCommand(path.name, actualScope, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func)

            isDefaultLocked = annotation.defaultLocked
            nsfw = annotation.nsfw

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, instance, commandId)
        }
    }
}