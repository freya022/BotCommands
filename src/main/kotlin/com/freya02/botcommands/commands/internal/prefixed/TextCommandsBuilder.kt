package com.freya02.botcommands.commands.internal.prefixed

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.TextDeclaration
import com.freya02.botcommands.api.prefixed.builder.TextCommandManager
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.*
import com.freya02.botcommands.core.internal.events.FirstReadyEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class TextCommandsBuilder(
    private val context: BContextImpl,
    private val serviceContainer: ServiceContainer,
    classPathContainer: ClassPathContainer
) {
    private val declarationFunctions: MutableList<ClassPathFunction> = arrayListOf()

    init {
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
    internal suspend fun onFirstRun(event: FirstReadyEvent, context: BContextImpl) {
        try {
            val manager = TextCommandManager(context)
            declarationFunctions.forEach { classPathFunction ->
                runDeclarationFunction(classPathFunction, manager)
            }

            manager.textCommands.forEach {
                context.textCommandsContext.addTextCommand(it)
            }
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