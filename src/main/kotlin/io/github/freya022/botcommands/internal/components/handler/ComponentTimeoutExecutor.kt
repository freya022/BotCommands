package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.components.timeout.ComponentTimeoutHandlers
import io.github.freya022.botcommands.internal.components.timeout.GroupTimeoutHandlers
import io.github.freya022.botcommands.internal.components.timeout.TimeoutDescriptor
import io.github.freya022.botcommands.internal.components.timeout.options.TimeoutHandlerOption
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.full.callSuspendBy

private val logger = KotlinLogging.logger { }

@BService
@RequiresComponents
internal class ComponentTimeoutExecutor internal constructor(
    private val groupTimeoutHandlers: GroupTimeoutHandlers,
    private val componentTimeoutHandlers: ComponentTimeoutHandlers,
) {
    internal suspend fun handleTimeout(component: ComponentData) {
        val componentTimeout = component.timeout

        if (componentTimeout is PersistentTimeout) {
            val handlerName = componentTimeout.handlerName
            val descriptor = when (component.componentType) {
                ComponentType.GROUP ->
                    groupTimeoutHandlers[handlerName]
                        ?: return logger.warn { "Missing ${annotationRef<GroupTimeoutHandler>()} named '$handlerName'" }

                else ->
                    componentTimeoutHandlers[handlerName]
                        ?: return logger.warn { "Missing ${annotationRef<ComponentTimeoutHandler>()} named '$handlerName'" }
            }

            val firstParameter: Any = when (component.componentType) {
                ComponentType.GROUP -> GroupTimeoutData((component as ComponentGroupData).componentIds)
                ComponentType.BUTTON, ComponentType.SELECT_MENU -> ComponentTimeoutData(component.internalId)
            }

            val userData = componentTimeout.userData
            if (userData.size != descriptor.optionSize) {
                return logger.warn {
                    """
                        Mismatch between component options and ${descriptor.function.shortSignature}
                        Component had ${userData.size} options, function has ${descriptor.optionSize} options
                        Component raw data: $userData
                    """.trimIndent()
                }
            }

            handlePersistentTimeout(descriptor, firstParameter, userData.iterator())
        } else if (componentTimeout is EphemeralTimeout) {
            componentTimeout.handler()
        }
    }

    private suspend fun handlePersistentTimeout(
        descriptor: TimeoutDescriptor<*>,
        firstArgument: Any,
        userDataIterator: Iterator<String?>
    ): Boolean {
        with(descriptor) {
            val optionValues = parameters.mapOptions { option ->
                if (tryInsertOption(option, this, userDataIterator) == InsertOptionResult.ABORT)
                    return false
            }

            function.callSuspendBy(parameters.mapFinalParameters(firstArgument, optionValues))
        }
        return true
    }

    private suspend fun tryInsertOption(
        option: OptionImpl,
        optionMap: MutableMap<OptionImpl, Any?>,
        userDataIterator: Iterator<String?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as TimeoutHandlerOption

                userDataIterator.next()?.let { option.resolver.resolveSuspend(it) }
            }

            OptionType.SERVICE -> (option as ServiceMethodOption).getService()
            OptionType.CUSTOM, OptionType.CONSTANT, OptionType.GENERATED -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}