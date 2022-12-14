package com.freya02.botcommands.internal.components.controller

import com.freya02.botcommands.api.components.ComponentFilteringData
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.components.ComponentHandlerParameter
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.EphemeralHandler
import com.freya02.botcommands.internal.components.new.ComponentData
import com.freya02.botcommands.internal.components.new.EphemeralComponentData
import com.freya02.botcommands.internal.components.new.PersistentComponentData
import com.freya02.botcommands.internal.components.repositories.ComponentRepository
import com.freya02.botcommands.internal.components.repositories.ComponentsHandlerContainer
import com.freya02.botcommands.internal.core.db.Database
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

@ConditionalService(dependencies = [Components::class, Database::class])
internal class ComponentsListener(
    private val database: Database,
    private val context: BContextImpl,
    private val componentsConfig: BComponentsConfig,
    private val componentRepository: ComponentRepository,
    private val componentController: ComponentController,
    private val coroutinesScopesConfig: BCoroutineScopesConfig,
    private val componentsHandlerContainer: ComponentsHandlerContainer
) {
    private val logger = KotlinLogging.logger { }

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
                event.reply_(context.getDefaultMessages(event).componentNotFoundErrorMsg, ephemeral = true).queue()
                return@launch
            }

            component.constraints?.let {
                if (!checkConstraints(event, it)) {
                    event.reply_(context.getDefaultMessages(event).componentNotAllowedErrorMsg, ephemeral = true).queue()
                    return@launch
                }
            }

            if (component.oneUse) {
                deleteRelatedComponents(component)
            }

            when (component) {
                is PersistentComponentData -> {
                    val (handlerName, userData) = component.handler
                    val descriptor = when (component.componentType) {
                        ComponentType.BUTTON -> componentsHandlerContainer.getButtonDescriptor(handlerName)
                            ?: throwUser("Could not find a button handler named $handlerName")
                        ComponentType.SELECT_MENU -> componentsHandlerContainer.getSelectMenuDescriptor(handlerName)
                            ?: throwUser("Could not find a select menu handler named $handlerName")
                        else -> throwInternal("Invalid component type being handled: ${component.componentType}")
                    }

                    transformEvent(event, descriptor.method)?.let { evt ->
                        componentController.removeContinuations(component.componentId).forEach {
                            @Suppress("UNCHECKED_CAST")
                            (it as Continuation<GenericComponentInteractionCreateEvent>).resume(evt)
                        }

                        handlePersistentComponent(descriptor, evt, userData)
                    }
                }
                is EphemeralComponentData -> {
                    val ephemeralHandler = component.handler

                    transformEvent(event, null)?.let { evt ->
                        componentController.removeContinuations(component.componentId).forEach {
                            @Suppress("UNCHECKED_CAST")
                            (it as Continuation<GenericComponentInteractionCreateEvent>).resume(evt)
                        }

                        @Suppress("UNCHECKED_CAST")
                        (ephemeralHandler as EphemeralHandler<GenericComponentInteractionCreateEvent>).handler(evt)
                    }
                }
            }
        } catch (e: Throwable) {
            handleException(event, e)
        }
    }

    private fun checkConstraints(event: GenericComponentInteractionCreateEvent, constraints: InteractionConstraints): Boolean {
        if (constraints.isEmpty) return true

        if (event.user.idLong in constraints.userList) return true

        val member = event.member
        if (member != null) {
            if (constraints.permissions.isNotEmpty()) {
                if (member.hasPermission(event.guildChannel, constraints.permissions)) {
                    return true
                }
            }

            //If the member has any of these roles
            if (member.roles.any { it.idLong in constraints.roleList }) {
                return true
            }
        }

        return false
    }

    private fun transformEvent(event: GenericComponentInteractionCreateEvent, function: KFunction<*>?): GenericComponentInteractionCreateEvent? {
        return when (event) {
            is ButtonInteractionEvent -> ButtonEvent(function, context, event)
            is StringSelectInteractionEvent -> StringSelectEvent(function, context, event)
            is EntitySelectInteractionEvent -> EntitySelectEvent(function, context, event)
            else -> {
                logger.warn("Unhandled component event: ${event::class.simpleName}")
                null
            }
        }
    }

    private suspend fun deleteRelatedComponents(component: ComponentData): Unit = database.transactional {
        componentController.deleteComponent(component)
    }

    private suspend fun handlePersistentComponent(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent, // already a BC event
        userData: Array<out String>
    ) {
        var userArgsIndex = 0
        val args = hashMapOf<KParameter, Any?>()
        args[descriptor.method.instanceParameter!!] = descriptor.instance
        args[descriptor.method.valueParameters.first()] = event

        for (parameter in descriptor.parameters) {
            val value = when (parameter.methodParameterType) {
                MethodParameterType.OPTION -> {
                    parameter as ComponentHandlerParameter

                    parameter.resolver.resolve(context, descriptor, event, userData[userArgsIndex]).also {
                        userArgsIndex++
                    }
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter

                    parameter.resolver.resolveSuspend(context, descriptor, event)
                }
                else -> throwInternal("MethodParameterType#${parameter.methodParameterType} has not been implemented")
            }

            if (value == null && parameter.kParameter.isOptional) { //Kotlin optional, continue getting more parameters
                continue
            } else if (value == null && !parameter.isOptional) { // Not a kotlin optional and not nullable
                throwUser(
                    descriptor.method,
                    "Parameter '${parameter.kParameter.bestName}' is not nullable but its resolver returned null"
                )
            }

            args[parameter.kParameter] = value
        }

        descriptor.method.callSuspendBy(args)
    }

    private fun handleException(event: GenericComponentInteractionCreateEvent, e: Throwable) {
        context.uncaughtExceptionHandler?.let { handler ->
            handler.onException(context, event, e)
            return
        }

        val baseEx = e.getDeepestCause()

        logger.error("Unhandled exception while executing a component handler with id ${event.componentId}", baseEx)

        val generalErrorMsg = context.getDefaultMessages(event).generalErrorMsg
        when {
            event.isAcknowledged -> event.hook.send(generalErrorMsg, ephemeral = true).queue()
            else -> event.reply_(generalErrorMsg, ephemeral = true).queue()
        }

        context.dispatchException("Exception in component handler with id ${event.componentId}", baseEx)
    }
}