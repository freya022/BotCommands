package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.UserContextFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.*
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.full.findAnnotation
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType

private val logger = KotlinLogging.logger { }

@BService
@RequiresApplicationCommands
internal class UserContextCommandAutoBuilder(
    applicationConfig: BApplicationConfig,
    resolverContainer: ResolverContainer,
    functionAnnotationsMap: FunctionAnnotationsMap,
    serviceContainer: ServiceContainer
) : ContextCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer) {
    private val userFunctions: List<UserContextFunctionMetadata>

    init {
        userFunctions = functionAnnotationsMap
            .getWithClassAnnotation<Command, JDAUserCommand>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(GlobalUserEvent::class))
            .map {
                val func = it.function
                val annotation = func.findAnnotation<JDAUserCommand>() ?: throwInternal("${annotationRef<JDAUserCommand>()} should be present")
                val path = CommandPath.ofName(annotation.name)
                val commandId = func.findAnnotation<CommandId>()?.value

                UserContextFunctionMetadata(it, annotation, path, commandId)
            }
    }

    //Separated functions so global command errors don't prevent guild commands from being registered
    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) = declareUser(manager)
    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) = declareUser(manager)

    private fun declareUser(manager: AbstractApplicationCommandManager) {
        with(SkipLogger(logger)) {
            userFunctions.forEachWithDelayedExceptions { metadata ->
                runFiltered(
                    manager,
                    forceGuildCommands,
                    metadata,
                    metadata.annotation.scope
                ) { processUserCommand(manager, metadata) }
            }
            log((manager as? GuildApplicationCommandManager)?.guild, CommandType.USER)
        }
    }

    private fun processUserCommand(manager: AbstractApplicationCommandManager, metadata: UserContextFunctionMetadata) {
        val func = metadata.func
        val instance = metadata.instance
        val path = metadata.path
        val commandId = metadata.commandId

        val annotation = metadata.annotation
        val actualScope = if (forceGuildCommands) CommandScope.GUILD else annotation.scope
        manager.userCommand(path.name, actualScope, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func)

            isDefaultLocked = annotation.defaultLocked
            nsfw = annotation.nsfw

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, JDAUserCommand::class, instance, commandId)
        }
    }
}