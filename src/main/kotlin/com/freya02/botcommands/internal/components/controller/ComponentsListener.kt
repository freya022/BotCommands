package com.freya02.botcommands.internal.components.controller

import com.freya02.botcommands.api.components.ComponentFilteringData
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.api.core.db.Database
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExceptionHandler
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.components.ComponentHandlerOption
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.EphemeralHandler
import com.freya02.botcommands.internal.components.data.EphemeralComponentData
import com.freya02.botcommands.internal.components.data.PersistentComponentData
import com.freya02.botcommands.internal.components.repositories.ComponentRepository
import com.freya02.botcommands.internal.components.repositories.ComponentsHandlerContainer
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.InsertOptionResult
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import com.freya02.botcommands.internal.utils.mapFinalParameters
import com.freya02.botcommands.internal.utils.mapOptions
import com.freya02.botcommands.internal.utils.tryInsertNullableOption
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

@ConditionalService(dependencies = [Components::class, Database::class])
internal class ComponentsListener(
    private val context: BContextImpl,
    private val componentsConfig: BComponentsConfig,
    private val componentRepository: ComponentRepository,
    private val componentController: ComponentController,
    private val coroutinesScopesConfig: BCoroutineScopesConfig,
    private val componentsHandlerContainer: ComponentsHandlerContainer
) {
    private val logger = KotlinLogging.logger { }
    private val exceptionHandler = ExceptionHandler(context, logger)

    @BEventListener
    internal fun onComponentInteraction(event: GenericComponentInteractionCreateEvent) = coroutinesScopesConfig.componentsScope.launch {
        try {
            componentsConfig.componentFilters.forEach {
                if (!it.isAccepted(ComponentFilteringData(context, event))) {
                    logger.trace { "Rejected ${event.componentType} interaction: ${event.componentId}" }
                    return@launch
                }
            }

            logger.trace { "Received ${event.componentType} interaction: ${event.componentId}" }

            val component = event.componentId.toIntOrNull()?.let {
                componentRepository.getComponent(it)
            }
            if (component == null) {
                event.reply_(context.getDefaultMessages(event).componentExpiredErrorMsg, ephemeral = true).queue()
                return@launch
            }

            component.constraints?.let { constraints ->
                if (!constraints.isAllowed(event)) {
                    event.reply_(context.getDefaultMessages(event).componentNotAllowedErrorMsg, ephemeral = true).queue()
                    return@launch
                }
            }

            if (component.oneUse) {
                componentController.deleteComponent(component)
            }

            when (component) {
                is PersistentComponentData -> {
                    transformEvent(event)?.let { evt ->
                        componentController.removeContinuations(component.componentId).forEach {
                            @Suppress("UNCHECKED_CAST")
                            (it as Continuation<GenericComponentInteractionCreateEvent>).resume(evt)
                        }

                        val (handlerName, userData) = component.handler ?: return@launch

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
                    transformEvent(event)?.let { evt ->
                        componentController.removeContinuations(component.componentId).forEach {
                            @Suppress("UNCHECKED_CAST")
                            (it as Continuation<GenericComponentInteractionCreateEvent>).resume(evt)
                        }

                        val ephemeralHandler = component.handler ?: return@launch

                        @Suppress("UNCHECKED_CAST")
                        (ephemeralHandler as EphemeralHandler<GenericComponentInteractionCreateEvent>).handler(evt)
                    }
                }
            }
        } catch (e: Throwable) {
            handleException(event, e)
        }
    }

    private fun transformEvent(event: GenericComponentInteractionCreateEvent): GenericComponentInteractionCreateEvent? {
        return when (event) {
            is ButtonInteractionEvent -> ButtonEvent(context, event)
            is StringSelectInteractionEvent -> StringSelectEvent(context, event)
            is EntitySelectInteractionEvent -> EntitySelectEvent(context, event)
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
            else -> throwInternal("MethodParameterType#${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}