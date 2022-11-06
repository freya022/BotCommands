package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.SelectionEvent
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.LateService
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.api.new_components.NewComponents
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.components.ComponentHandlerParameter
import com.freya02.botcommands.internal.components.ComponentsHandlerContainer
import com.freya02.botcommands.internal.data.DataStoreService
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

@LateService
@ConditionalService
internal class NewComponentsListener(
    private val context: BContextImpl,
    private val componentsHandlerContainer: ComponentsHandlerContainer,
    private val dataStore: DataStoreService,
    private val coroutinesScopesConfig: BCoroutineScopesConfig
) {
    private val logger = KotlinLogging.logger { }

    @BEventListener
    internal suspend fun onComponentInteraction(event: GenericComponentInteractionCreateEvent) {
        coroutinesScopesConfig.componentsScope.launch {
            logger.trace { "Received ${event.componentType} interaction: ${event.componentId}" }

            try {
                val data = dataStore.getData(event.componentId) ?: let {
                    event.reply_("This button has expired", ephemeral = true).queue()
                    return@launch
                }

                when (data.lifetimeType) {
                    LifetimeType.PERSISTENT -> {
                        val componentData = data.decodeData<PersistentComponentData>()
                        val (handlerName, userData) = componentData.persistentHandler
                        val descriptor = componentsHandlerContainer.getButtonDescriptor(handlerName)
                            ?: throwUser("Could not find a button description named $handlerName")

                        handlePersistentComponent(descriptor, event, userData)
                    }
                    LifetimeType.EPHEMERAL -> TODO()
                }

            } catch (e: Throwable) {
                handleException(event, e)
            }
        }
    }

    private suspend fun handlePersistentComponent(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        userData: Array<String>
    ) {
        var userArgsIndex = 0
        val args = hashMapOf<KParameter, Any?>()
        args[descriptor.method.instanceParameter!!] = descriptor.instance
        args[descriptor.method.valueParameters.first()] = when (event) {
            is ButtonInteractionEvent -> ButtonEvent(descriptor.method, context, event)
            is SelectMenuInteractionEvent -> SelectionEvent(descriptor.method, context, event)
            else -> throwInternal("Unhandled persistent component event: $event")
        }

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
            event.isAcknowledged -> event.hook.sendMessage(generalErrorMsg).setEphemeral(true).queue()
            else -> event.reply(generalErrorMsg).setEphemeral(true).queue()
        }

        context.dispatchException(
            "Exception in component handler with id ${event.componentId}", baseEx
        )
    }

    companion object : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext) = NewComponents.checkServiceAvailability(context)
    }
}