package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.internal.commands.application.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.UserContextFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.castFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType
import kotlin.reflect.KClass

@BService
@RequiresApplicationCommands
internal class UserContextCommandAutoBuilder(
    applicationConfig: BApplicationConfig,
    resolverContainer: ResolverContainer,
    functionAnnotationsMap: FunctionAnnotationsMap,
    serviceContainer: ServiceContainer
) : ContextCommandAutoBuilder<UserContextFunctionMetadata>(serviceContainer, applicationConfig, resolverContainer) {

    override val commandAnnotation: KClass<out Annotation> get() = JDAUserCommand::class
    override val commandType: CommandType
        get() = CommandType.USER

    override val rootAnnotatedCommands = functionAnnotationsMap
        .getWithClassAnnotation<Command, JDAUserCommand>()
        .requiredFilter(FunctionFilter.nonStatic())
        .requiredFilter(FunctionFilter.firstArg(GlobalUserEvent::class))
        .map {
            val func = it.function
            val annotation = func.findAnnotationRecursive<JDAUserCommand>() ?: throwInternal("${annotationRef<JDAUserCommand>()} should be present")
            val path = CommandPath.ofName(annotation.name)
            val commandId = func.findAnnotationRecursive<CommandId>()?.value

            UserContextFunctionMetadata(it, annotation, path, commandId)
        }

    context(logger: SkipLogger)
    override fun declareTopLevel(
        manager: AbstractApplicationCommandManager,
        rootCommand: UserContextFunctionMetadata,
    ) {
        val func = rootCommand.func

        if (!checkDeclarationFilter(manager, rootCommand))
            return // Already logged

        val annotation = rootCommand.annotation
        manager.userCommand(rootCommand.path.name, func.castFunction()) {
            fillCommandBuilder(func)
            fillApplicationCommandBuilder(func)

            contexts = if (forceGuildCommands) {
                setOf(InteractionContextType.GUILD)
            } else {
                annotation.contexts.toEnumSetOr(manager.defaultContexts)
            }
            integrationTypes = annotation.integrationTypes.toEnumSetOr(manager.defaultIntegrationTypes)
            isDefaultLocked = annotation.defaultLocked
            nsfw = annotation.nsfw

            processOptions((manager as? GuildApplicationCommandManager)?.guild, func, rootCommand.instance, rootCommand.commandId)
        }
    }
}
