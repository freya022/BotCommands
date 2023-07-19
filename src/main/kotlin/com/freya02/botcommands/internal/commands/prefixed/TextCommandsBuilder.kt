package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.FirstReadyEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.autobuilder.TextCommandAutoBuilder
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.core.service.FunctionAnnotationsMap
import com.freya02.botcommands.internal.core.service.ServiceContainerImpl
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

        declarationFunctions += functionAnnotationsMap
            .getFunctionsWithAnnotation<TextDeclaration>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(TextCommandManager::class))

        logger.debug("Loaded ${declarationFunctions.size} text command declaration functions")
        if (declarationFunctions.isNotEmpty()) {
            logger.trace { "Text command declaration functions:\n" + declarationFunctions.joinToString("\n") { it.function.shortSignature } }
        }
    }

    @BEventListener
    internal suspend fun onFirstReady(event: FirstReadyEvent, context: BContextImpl) {
        try {
            val manager = TextCommandManager(context)
            declarationFunctions.forEach { classPathFunction ->
                runDeclarationFunction(classPathFunction, manager)
            }

            manager.textCommands.map.values.forEach { context.textCommandsContext.addTextCommand(it) }
        } catch (e: Throwable) {
            logger.error("An error occurred while updating global commands", e)
        }
    }

    private suspend fun runDeclarationFunction(classPathFunction: ClassPathFunction, manager: TextCommandManager) {
        val (instance, function) = classPathFunction
        val args = serviceContainer.getParameters(function.nonInstanceParameters.drop(1).map { it.type.jvmErasure }).toTypedArray()
        function.callSuspend(instance, manager, *args)
    }
}