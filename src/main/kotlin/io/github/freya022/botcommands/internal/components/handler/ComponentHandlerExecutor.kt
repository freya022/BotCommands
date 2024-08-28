package io.github.freya022.botcommands.internal.components.handler

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import io.github.freya022.botcommands.internal.components.data.EphemeralComponentData
import io.github.freya022.botcommands.internal.components.data.PersistentComponentData
import io.github.freya022.botcommands.internal.components.handler.options.ComponentHandlerOption
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

@BService
@RequiresComponents
internal class ComponentHandlerExecutor internal constructor(
    private val defaultMessagesFactory: DefaultMessagesFactory,
    private val componentHandlerContainer: ComponentHandlerContainer,
) {
    internal suspend fun runHandler(component: ActionComponentData, event: GenericComponentInteractionCreateEvent): Boolean {
        if (component is PersistentComponentData) {
            val (handlerName, userData) = component.handler ?: return true

            val descriptor = when (component.componentType) {
                ComponentType.BUTTON -> componentHandlerContainer.getButtonDescriptor(handlerName)
                    ?: throwArgument("Missing ${annotationRef<JDAButtonListener>()} named '$handlerName'")

                ComponentType.SELECT_MENU -> componentHandlerContainer.getSelectMenuDescriptor(handlerName)
                    ?: throwArgument("Missing ${annotationRef<JDASelectMenuListener>()} named '$handlerName'")

                else -> throwInternal("Invalid component type being handled: ${component.componentType}")
            }

            if (userData.size != descriptor.optionSize) {
                // This is on debug as this is supposed to happen only in development
                // Or if a user clicked on an old incompatible button,
                // in which case the developer can enable debug logs if complained about
                logger.debug {
                    """
                        Mismatch between component options and ${descriptor.function.shortSignature}
                        Component had ${userData.size} options, function has ${descriptor.optionSize} options
                        Component raw data: $userData
                    """.trimIndent()
                }
                event.reply_(defaultMessagesFactory.get(event).componentExpiredErrorMsg, ephemeral = true).queue()
                return false
            }

            return handlePersistentComponent(descriptor, event, userData.iterator())
        } else if (component is EphemeralComponentData) {
            val ephemeralHandler = component.handler ?: return true

            @Suppress("UNCHECKED_CAST")
            (ephemeralHandler as EphemeralHandler<GenericComponentInteractionCreateEvent>).handler(event)
        }

        return true
    }

    private suspend fun handlePersistentComponent(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent, // already a BC event
        userDataIterator: Iterator<String?>
    ): Boolean {
        checkEventType(event, descriptor)

        with(descriptor) {
            val optionValues = parameters.mapOptions { option ->
                if (tryInsertOption(event, descriptor, option, this, userDataIterator) == InsertOptionResult.ABORT)
                    return false
            }

            function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))
        }
        return true
    }

    private fun checkEventType(event: GenericComponentInteractionCreateEvent, descriptor: ComponentDescriptor) {
        if (event !is SelectMenuInteraction<*, *>) return

        val expectedEventType = when (event) {
            is EntitySelectInteractionEvent -> EntitySelectEvent::class
            is StringSelectInteractionEvent -> StringSelectEvent::class
            else -> throwInternal("Unchecked select menu event type: ${event.javaClass.simpleNestedName}")
        }

        val eventType = descriptor.eventFunction.firstParameter.type
        requireAt(eventType.jvmErasure.isInstance(event), descriptor.function) {
            "Received an ${expectedEventType.simpleNestedName} but handler only accepts a ${eventType.simpleNestedName}"
        }
    }

    private suspend fun tryInsertOption(
        event: GenericComponentInteractionCreateEvent,
        descriptor: ComponentDescriptor,
        option: OptionImpl,
        optionMap: MutableMap<OptionImpl, Any?>,
        userDataIterator: Iterator<String?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as ComponentHandlerOption

                val obj = userDataIterator.next()?.let { option.resolver.resolveSuspend(event, it) }
                if (obj == null && option.isRequired && event.isAcknowledged)
                    return InsertOptionResult.ABORT

                obj
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(descriptor, event)
            }
            OptionType.SERVICE -> (option as ServiceMethodOption).getService()
            OptionType.GENERATED, OptionType.CONSTANT -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}