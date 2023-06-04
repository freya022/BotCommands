package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.FirstReadyEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.autobuilder.TextCommandAutoBuilder
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.reflection.FunctionAnnotationsMap
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.core.service.ServiceContainerImpl
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import mu.KotlinLogging
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.jvmErasure

@BService
internal class TextCommandsBuilder(
    private val serviceContainer: ServiceContainerImpl,
    functionAnnotationsMap: FunctionAnnotationsMap
) {
    private val logger = KotlinLogging.logger {  }

    private val declarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

    init {
        declarationFunctions += ClassPathFunction(serviceContainer.getService<TextCommandAutoBuilder>(), TextCommandAutoBuilder::declare)

        for (classPathFunction in functionAnnotationsMap
            .getFunctionsWithAnnotation<TextDeclaration>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(TextCommandManager::class))
        ) {
            declarationFunctions.add(classPathFunction)
        }

        logger.debug("Loaded ${declarationFunctions.size} text command declaration functions")
        if (declarationFunctions.isNotEmpty()) {
            logger.trace { "Text command declaration functions:\n" + declarationFunctions.joinToString("\n") { it.function.shortSignature } }
        }
    }

    @BEventListener
    internal suspend fun onFirstReady(event: FirstReadyEvent, context: BContextImpl, cooldownService: CooldownService) {
        try {
            val manager = TextCommandManager(context)
            declarationFunctions.forEach { classPathFunction ->
                runDeclarationFunction(classPathFunction, manager)
            }

            val helpCommandInfo: HelpCommandInfo? = getHelpCommandInfo(manager, context)

            manager.textCommands.map.values.forEach { context.textCommandsContext.addTextCommand(it) }

            context.eventDispatcher.addEventListener(TextCommandsListener(context, cooldownService, helpCommandInfo))
        } catch (e: Throwable) {
            logger.error("An error occurred while updating global commands", e)
        }
    }

    private fun getHelpCommandInfo(manager: TextCommandManager, context: BContextImpl): HelpCommandInfo? {
        val helpCommandInfo = manager.textCommands.map.values.firstOrNull { it.path.fullPath == "help" }

        return when {
            helpCommandInfo != null -> {
                logger.debug("Using a custom 'help' text command implementation")

                val helpCommand = helpCommandInfo.variations.firstNotNullOfOrNull { it.instance as? IHelpCommand }
                    ?: throwUser("Help command must at least one variation of the 'help' command path, where the instance implements IHelpCommand")
                HelpCommandInfo(helpCommand, helpCommandInfo)
            }
            else -> when {
                context.isHelpDisabled -> {
                    logger.debug("Using no 'help' text command implementation")
                    null
                }
                else -> {
                    val service = context.serviceContainer.getService(HelpCommand::class)
                    service.declare(manager)

                    val info = manager.textCommands.map.values.firstOrNull { it.path.fullPath == "help" }
                        ?: throwInternal("Default help command was declared incorrectly")
                    HelpCommandInfo(service, info)
                }
            }
        }
    }

    private suspend fun runDeclarationFunction(classPathFunction: ClassPathFunction, manager: TextCommandManager) {
        val (instance, function) = classPathFunction
        val args = serviceContainer.getParameters(function.nonInstanceParameters.drop(1).map { it.type.jvmErasure }).toTypedArray()
        function.callSuspend(instance, manager, *args)
    }
}