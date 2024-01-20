package io.github.freya022.botcommands.internal.components.controller

import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.config.BComponentsConfigBuilder
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.ratelimit.withRateLimit
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.data.AbstractComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.components.data.EphemeralComponentData
import io.github.freya022.botcommands.internal.components.data.PersistentComponentData
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor
import io.github.freya022.botcommands.internal.components.handler.ComponentHandlerContainer
import io.github.freya022.botcommands.internal.components.handler.ComponentHandlerOption
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import io.github.freya022.botcommands.internal.components.repositories.ComponentRepository
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.core.db.InternalDatabase
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.options.isRequired
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
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
    private val context: BContext,
    filters: List<ComponentInteractionFilter<Any>>,
    rejectionHandler: ComponentInteractionRejectionHandler<Any>?,
    private val componentRepository: ComponentRepository,
    private val componentController: ComponentController,
    private val coroutinesScopesConfig: BCoroutineScopesConfig,
    private val componentHandlerContainer: ComponentHandlerContainer
) {
    private val logger = KotlinLogging.logger { }
    private val exceptionHandler = ExceptionHandler(context, logger)

    // Types are crosschecked anyway
    private val globalFilters: List<ComponentInteractionFilter<Any>> = filters.filter { it.global }
    private val rejectionHandler: ComponentInteractionRejectionHandler<Any>? = when {
        globalFilters.isEmpty() -> null
        else -> rejectionHandler
            ?: throw IllegalStateException("A ${classRef<ComponentInteractionRejectionHandler<*>>()} must be available if ${classRef<ComponentInteractionFilter<*>>()} is used")
    }

    @BEventListener
    internal fun onComponentInteraction(event: GenericComponentInteractionCreateEvent) = coroutinesScopesConfig.componentScope.launch {
        try {
            logger.trace { "Received ${event.componentType} interaction: ${event.componentId}" }

            val componentId = event.componentId.toIntOrNull()
                ?: return@launch logger.error { "Received an interaction for an external token format: '${event.componentId}', " +
                        "please only use the framework's components or disable ${BComponentsConfigBuilder::useComponents.reference}" }
            val component = componentRepository.getComponent(componentId)
                ?: return@launch event.reply_(context.getDefaultMessages(event).componentExpiredErrorMsg, ephemeral = true).queue()

            if (component.filters === ComponentFilters.INVALID_FILTERS) {
                return@launch event.reply_(context.getDefaultMessages(event).componentNotAllowedErrorMsg, ephemeral = true).queue()
            }

            component.filters.onEach { filter ->
                require(!filter.global) {
                    "Global filter ${filter.javaClass.simpleNestedName} cannot be used explicitly, see ${Filter::global.reference}"
                }
            }

            component.withRateLimit(context, event, !context.isOwner(event.user.idLong)) { cancellableRateLimit ->
                component.constraints?.let { constraints ->
                    if (!constraints.isAllowed(event)) {
                        event.reply_(context.getDefaultMessages(event).componentNotAllowedErrorMsg, ephemeral = true).queue()
                        return@withRateLimit false
                    }
                }

                checkFilters(globalFilters, component.filters) { filter ->
                    val handlerName = (component as? PersistentComponentData)?.handler?.handlerName
                    val userError = filter.checkSuspend(event, handlerName)
                    if (userError != null) {
                        rejectionHandler!!.handleSuspend(event, handlerName, userError)
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
                                ComponentType.BUTTON -> componentHandlerContainer.getButtonDescriptor(handlerName)
                                    ?: throwUser("Missing ${annotationRef<JDAButtonListener>()} named '$handlerName'")
                                ComponentType.SELECT_MENU -> componentHandlerContainer.getSelectMenuDescriptor(handlerName)
                                    ?: throwUser("Missing ${annotationRef<JDASelectMenuListener>()} named '$handlerName'")
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
                                event.reply_(context.getDefaultMessages(event).componentExpiredErrorMsg, ephemeral = true).queue()
                                return@withRateLimit false
                            }

                            if (!handlePersistentComponent(descriptor, evt, userData.iterator())) {
                                return@withRateLimit false
                            }
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
                logger.warn { "Unhandled component event: ${event::class.simpleName}" }
                null
            }
        }
    }

    private suspend fun handlePersistentComponent(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent, // already a BC event
        userDataIterator: Iterator<String?>
    ): Boolean {
        with(descriptor) {
            val optionValues = parameters.mapOptions { option ->
                if (tryInsertOption(event, descriptor, option, this, userDataIterator) == InsertOptionResult.ABORT)
                    return false
            }

            function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))
        }
        return true
    }

    private fun handleException(event: GenericComponentInteractionCreateEvent, e: Throwable) {
        exceptionHandler.handleException(event, e, "component interaction, ID: '${event.componentId}'", mapOf(
            "Message" to event.message.jumpUrl,
            "Component" to event.component
        ))

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
        userDataIterator: Iterator<String?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as ComponentHandlerOption

                val obj = userDataIterator.next()?.let { option.resolver.resolveSuspend(descriptor, event, it) }
                if (obj == null && option.isRequired && event.isAcknowledged)
                    return InsertOptionResult.ABORT

                obj
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(descriptor, event)
            }
            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}