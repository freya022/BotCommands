package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.autobuilder.TextCommandAutoBuilder
import com.freya02.botcommands.internal.core.*
import com.freya02.botcommands.internal.core.events.FirstReadyEvent
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class TextCommandsBuilder(
    private val serviceContainer: ServiceContainer,
    classPathContainer: ClassPathContainer
) {
    private val declarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

    init {
        declarationFunctions += ClassPathFunction(serviceContainer.getService<TextCommandAutoBuilder>(), TextCommandAutoBuilder::declare)

        for (classPathFunction in classPathContainer
            .functionsWithAnnotation<TextDeclaration>()
            .requireNonStatic()
            .requireFirstArg(TextCommandManager::class)
        ) {
            declarationFunctions.add(classPathFunction)
        }

        LOGGER.debug("Loaded ${declarationFunctions.size} text command declaration functions")
        if (declarationFunctions.isNotEmpty()) {
            LOGGER.trace("Text command declaration functions:\n" + declarationFunctions.joinToString("\n") { it.function.shortSignature })
        }
    }

    @BEventListener
    internal suspend fun onFirstReady(event: FirstReadyEvent, context: BContextImpl) {
        try {
            val manager = TextCommandManager(context)
            declarationFunctions.forEach { classPathFunction ->
                runDeclarationFunction(classPathFunction, manager)
            }

            if (manager.textCommands.any { it.path.fullPath == "help" }) {
                LOGGER.debug("Using a custom 'help' text command implementation")
            } else {
                if (context.isHelpDisabled) {
                    LOGGER.debug("Using no 'help' text command implementation")
                } else {
                    context.serviceContainer.getService(HelpCommand::class, useNonClasspath = true).declare(manager)
                }
            }

            manager.textCommands.forEach { context.textCommandsContext.addTextCommand(it) }
        } catch (e: Throwable) {
            LOGGER.error("An error occurred while updating global commands", e)
        }
    }

    private suspend fun runDeclarationFunction(classPathFunction: ClassPathFunction, manager: TextCommandManager) {
        val function = classPathFunction.function
        val args = serviceContainer.getParameters(function.nonInstanceParameters.drop(1).map { it.type.jvmErasure }).toTypedArray()

        function.callSuspend(classPathFunction.instance, manager, *args)
    }
}