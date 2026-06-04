package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.internal.commands.application.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.MessageContextFunctionMetadata
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
internal class MessageContextCommandAutoBuilder(
    applicationConfig: BApplicationConfig,
    resolverContainer: ResolverContainer,
    functionAnnotationsMap: FunctionAnnotationsMap,
    serviceContainer: ServiceContainer
) : ContextCommandAutoBuilder<MessageContextFunctionMetadata>(serviceContainer, applicationConfig, resolverContainer) {

    override val commandAnnotation: KClass<out Annotation> get() = JDAMessageCommand::class
    override val commandType: CommandType
        get() = CommandType.MESSAGE

    override val rootAnnotatedCommands = functionAnnotationsMap
        .getWithClassAnnotation<Command, JDAMessageCommand>()
        .requiredFilter(FunctionFilter.nonStatic())
        .requiredFilter(FunctionFilter.firstArg(GlobalMessageEvent::class))
        .map {
            val func = it.function
            val annotation = func.findAnnotationRecursive<JDAMessageCommand>() ?: throwInternal("${annotationRef<JDAMessageCommand>()} should be present")
            val path = CommandPath.ofName(annotation.name)
            val commandId = func.findAnnotationRecursive<CommandId>()?.value

            MessageContextFunctionMetadata(it, annotation, path, commandId)
        }

    context(logger: SkipLogger)
    override fun declareTopLevel(
        manager: AbstractApplicationCommandManager,
        rootCommand: MessageContextFunctionMetadata,
    ) {
        val func = rootCommand.func

        if (!checkDeclarationFilter(manager, rootCommand))
            return // Already logged

        val annotation = rootCommand.annotation
        manager.messageCommand(rootCommand.path.name, func.castFunction()) {
            fillCommandBuilder(ApplicationCommandUnit(serviceContainer, rootCommand))
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
