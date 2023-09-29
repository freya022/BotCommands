package com.freya02.botcommands.internal.components.controller

import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.config.BComponentsConfigBuilder
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Dependencies
import com.freya02.botcommands.api.core.service.getInterfacedServices
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExceptionHandler
import com.freya02.botcommands.internal.commands.withRateLimit
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.components.ComponentHandlerOption
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.EphemeralHandler
import com.freya02.botcommands.internal.components.data.AbstractComponentData
import com.freya02.botcommands.internal.components.data.ComponentGroupData
import com.freya02.botcommands.internal.components.data.EphemeralComponentData
import com.freya02.botcommands.internal.components.data.PersistentComponentData
import com.freya02.botcommands.internal.components.repositories.ComponentRepository
import com.freya02.botcommands.internal.components.repositories.ComponentsHandlerContainer
import com.freya02.botcommands.internal.core.db.InternalDatabase
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.utils.*
import com.freya02.botcommands.internal.utils.ReflectionUtils.referenceString
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.reflect.full.callSuspendBy

@BService
@Dependencies(Components::class, InternalDatabase::class)
internal class ComponentsListener(
    private val context: BContextImpl,
    private val componentRepository: ComponentRepository,
    private val componentController: ComponentController,
    private val coroutinesScopesConfig: BCoroutineScopesConfig,
    private val componentsHandlerContainer: ComponentsHandlerContainer
) {
    private val logger = KotlinLogging.logger { }
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val filters = context.getInterfacedServices<ComponentInteractionFilter>()

    @BEventListener
    internal fun onComponentInteraction(event: GenericComponentInteractionCreateEvent) = coroutinesScopesConfig.componentsScope.launch {
        try {
            logger.trace { "Received ${event.componentType} interaction: ${event.componentId}" }

            val componentId = event.componentId.toIntOrNull()
                ?: return@launch logger.error { "Received an interaction for an external token format: '${event.componentId}', " +
                        "please only use the framework's components or disable ${BComponentsConfigBuilder::useComponents.referenceString}" }
            val component = componentRepository.getComponent(componentId)
                ?: return@launch event.reply_(context.getDefaultMessages(event).componentExpiredErrorMsg, ephemeral = true).queue()

            component.withRateLimit(context, event, !context.isOwner(event.user.idLong)) { cancellableRateLimit ->
                component.constraints?.let { constraints ->
                    if (!constraints.isAllowed(event)) {
                        event.reply_(context.getDefaultMessages(event).componentNotAllowedErrorMsg, ephemeral = true).queue()
                        return@withRateLimit false
                    }
                }

                for (filter in filters) {
                    if (!filter.isAcceptedSuspend(event, (component as? PersistentComponentData)?.handler?.handlerName)) {
                        if (event.isAcknowledged) {
                            logger.trace { "${filter::class.simpleNestedName} rejected ${event.componentType} interaction (handler: ${component.handler})" }
                        } else {
                            logger.error { "${filter::class.simpleNestedName} rejected ${event.componentType} interaction (handler: ${component.handler}) but did not acknowledge the interaction" }
                        }
                        return@withRateLimit false
                    }
                }

                if (component.oneUse) {
                    componentController.deleteComponent(component)
                }

                when (component) {
                    is PersistentComponentData -> {
                        transformEvent(event, cancellableRateLimit)?.let { evt ->
                            resumeCoroutines(component, evt)

                            val (handlerName, userData) = component.handler ?: return@withRateLimit true

                            val descriptor = when (component.componentType) {
                                ComponentType.BUTTON -> componentsHandlerContainer.getButtonDescriptor(handlerName)
                                    ?: throwUser("Could not find a button handler named $handlerName")
                                ComponentType.SELECT_MENU -> componentsHandlerContainer.getSelectMenuDescriptor(handlerName)
                                    ?: throwUser("Could not find a select menu handler named $handlerName")
                                else -> throwInternal("Invalid component type being handled: ${component.componentType}")
                            }

                            handlePersistentComponent(descriptor, evt, userData.iterator())
                        }
                    }
                    is EphemeralComponentData -> {
                        transformEvent(event, cancellableRateLimit)?.let { evt ->
                            resumeCoroutines(component, evt)

                            val ephemeralHandler = component.handler ?: return@withRateLimit true

                            @Suppress("UNCHECKED_CAST")
                            (ephemeralHandler as EphemeralHandler<GenericComponentInteractionCreateEvent>).handler(evt)
                        }
                    }
                    is ComponentGroupData -> throwInternal("Somehow received an interaction with a component ID that was a group")
                }

                true
            }
        } catch (e: Throwable) {
            handleException(event, e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resumeCoroutines(component: AbstractComponentData, evt: GenericComponentInteractionCreateEvent) {
        component.groupId?.let { groupId ->
            componentController.removeContinuations(groupId).forEach {
                (it as Continuation<GenericComponentInteractionCreateEvent>).resume(evt)
            }
        }

        componentController.removeContinuations(component.componentId).forEach {
            (it as Continuation<GenericComponentInteractionCreateEvent>).resume(evt)
        }
    }

    private fun transformEvent(
        event: GenericComponentInteractionCreateEvent,
        cancellableRateLimit: CancellableRateLimit
    ): GenericComponentInteractionCreateEvent? {
        return when (event) {
            is ButtonInteractionEvent -> ButtonEvent(context, event, cancellableRateLimit)
            is StringSelectInteractionEvent -> StringSelectEvent(context, event, cancellableRateLimit)
            is EntitySelectInteractionEvent -> EntitySelectEvent(context, event, cancellableRateLimit)
            else -> {
                logger.warn("Unhandled component event: ${event::class.simpleName}")
                null
            }
        }
    }

    private suspend fun handlePersistentComponent(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent, // already a BC event
        userDataIterator: Iterator<String>
    ) {
        with(descriptor) {
            val optionValues = parameters.mapOptions { option ->
                if (tryInsertOption(event, descriptor, option, this, userDataIterator) == InsertOptionResult.ABORT)
                    throwInternal("${::tryInsertOption.shortSignatureNoSrc} shouldn't have been aborted")
            }

            function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))
        }
    }

    private fun handleException(event: GenericComponentInteractionCreateEvent, e: Throwable) {
        exceptionHandler.handleException(event, e, "component interaction, ID: '${event.componentId}'")

        val generalErrorMsg = context.getDefaultMessages(event).generalErrorMsg
        when {
            event.isAcknowledged -> event.hook.send(generalErrorMsg, ephemeral = true).queue()
            else -> event.reply_(generalErrorMsg, ephemeral = true).queue()
        }
    }

    private suspend fun tryInsertOption(
        event: GenericComponentInteractionCreateEvent,
        descriptor: ComponentDescriptor,
        option: Option,
        optionMap: MutableMap<Option, Any?>,
        userDataIterator: Iterator<String>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as ComponentHandlerOption

                option.resolver.resolveSuspend(context, descriptor, event, userDataIterator.next())
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(context, descriptor, event)
            }
            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}