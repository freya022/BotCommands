package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.prefixed.TextCommandManager
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextDeclaration
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.FirstGuildReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.prefixed.autobuilder.TextCommandAutoBuilder
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.core.service.ServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.getParameters
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.shortSignature
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
            logger.trace { "Text command declaration functions:\n" + declarationFunctions.joinAsList { it.function.shortSignature } }
        }
    }

    @BEventListener
    internal suspend fun onFirstReady(event: FirstGuildReadyEvent, context: BContextImpl) {
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