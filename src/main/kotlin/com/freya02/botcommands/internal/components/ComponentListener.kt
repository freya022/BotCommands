package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.components.ComponentErrorReason
import com.freya02.botcommands.api.components.ComponentFilteringData
import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.components.ComponentType
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.SelectionEvent
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.getDeepestCause
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

private val LOGGER = Logging.getLogger()

@BService
internal class ComponentListener(
    private val context: BContextImpl,
    private val componentsHandlerContainer: ComponentsHandlerContainer,
    private val componentsManager: ComponentManager
) {
    @BEventListener
    internal fun onButtonClick(event: ButtonInteractionEvent) {
        onComponentInteraction(event)
    }

    @BEventListener
    internal fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        onComponentInteraction(event)
    }

    private fun onComponentInteraction(event: GenericComponentInteractionCreateEvent) {
        try {
            for (componentFilter in context.componentFilters) {
                if (!componentFilter.isAccepted(ComponentFilteringData(context, event))) {
                    return
                }
            }

            val scope = context.config.coroutineScopesConfig.componentsScope

            componentsManager.fetchComponent(event.componentId) { fetchResult ->
                val fetchedComponent = fetchResult.fetchedComponent ?: run {
                    event.reply_(context.getDefaultMessages(event).componentNotFoundErrorMsg, ephemeral = true).queue()

                    return@fetchComponent
                }

                val idType = fetchedComponent.type
                if ((idType == ComponentType.LAMBDA_BUTTON || idType == ComponentType.PERSISTENT_BUTTON) && event !is ButtonInteractionEvent) {
                    throwInternal("Received a button id type but event is not a ButtonInteractionEvent")
                }

                if ((idType == ComponentType.PERSISTENT_SELECTION_MENU || idType == ComponentType.LAMBDA_SELECTION_MENU) && event !is SelectMenuInteractionEvent) {
                    throwInternal("Received a selection menu id type but event is not a SelectMenuInteractionEvent")
                }

                when (idType) {
                    ComponentType.PERSISTENT_BUTTON -> {
                        componentsManager.handlePersistentButton(event, fetchResult, { onError(event, it) }) {
                            val descriptor = componentsHandlerContainer.getButtonDescriptor(it.handlerName)
                                ?: throwUser("No component descriptor found for component handler '${it.handlerName}'")

                            scope.launch {
                                handlePersistentComponent(event, descriptor, it.args) {
                                    ButtonEvent(descriptor.method, context, event as ButtonInteractionEvent)
                                }
                            }
                        }
                    }
                    ComponentType.LAMBDA_BUTTON -> {
                        componentsManager.handleLambdaButton(event, fetchResult, { onError(event, it) }) {
                            scope.launch { //The scope is used for consuming the event asynchronously, as to free the ongoing transaction
                                it.consumer.accept(ButtonEvent(null, context, event as ButtonInteractionEvent))
                            }
                        }
                    }
                    ComponentType.PERSISTENT_SELECTION_MENU -> {
                        componentsManager.handlePersistentSelectMenu(event, fetchResult, { onError(event, it) }) {
                            val descriptor = componentsHandlerContainer.getSelectMenuDescriptor(it.handlerName)
                                ?: throwUser("No component descriptor found for component handler '${it.handlerName}'")

                            scope.launch {
                                handlePersistentComponent(event, descriptor, it.args) {
                                    SelectionEvent(descriptor.method, context, event as SelectMenuInteractionEvent)
                                }
                            }
                        }
                    }
                    ComponentType.LAMBDA_SELECTION_MENU -> {
                        componentsManager.handleLambdaSelectMenu(event, fetchResult, { onError(event, it) }) {
                            scope.launch { //The scope is used for consuming the event asynchronously, as to free the ongoing transaction
                                it.consumer.accept(SelectionEvent(null, context, event as SelectMenuInteractionEvent))
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            handleException(e, event)
        }
    }

    private fun handleException(e: Throwable, event: GenericComponentInteractionCreateEvent) {
        val handler = context.uncaughtExceptionHandler
        if (handler != null) {
            handler.onException(context, event, e)
            return
        }

        val baseEx = e.getDeepestCause()

        LOGGER.error(
            "Unhandled exception in thread '${Thread.currentThread().name}' while executing a component", baseEx
        )

        val generalErrorMsg = context.getDefaultMessages(event).generalErrorMsg
        when {
            event.isAcknowledged -> event.hook.send(generalErrorMsg).queue()
            else -> event.reply_(generalErrorMsg, ephemeral = true).queue()
        }

        context.dispatchException("Exception in component handler", baseEx)
    }

    private suspend fun handlePersistentComponent(
        event: GenericComponentInteractionCreateEvent,
        descriptor: ComponentDescriptor,
        buttonArgs: Array<out String>,
        eventSupplier: () -> GenericComponentInteractionCreateEvent
    ) {
        val parameters = descriptor.parameters
        if (parameters.optionCount != buttonArgs.size) {
            throwUser("Resolver for ${descriptor.method.shortSignature} has ${parameters.optionCount} arguments but component had ${buttonArgs.size} arguments")
        }

        val objects: MutableMap<KParameter, Any?> = hashMapOf()
        objects[descriptor.method.instanceParameter!!] = descriptor.instance
        objects[descriptor.method.valueParameters.first()] = eventSupplier()

        var optionIndex = 0
        parameters.forEach { parameter ->
            when (parameter.methodParameterType) {
                MethodParameterType.COMMAND -> {
                    parameter as ComponentHandlerParameter

                    val buttonArg = buttonArgs[optionIndex++]
                    val resolved = parameter.resolver.resolve(context, descriptor, event, buttonArg)
                        ?: throwUser("Component id ${event.componentId}, tried to resolve '${buttonArg}' with option resolver ${parameter.resolver.javaClass.simpleName} on method ${descriptor.method.shortSignature} but result was null")

                    objects[parameter.kParameter] = resolved
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter

                    val resolved = parameter.resolver.resolve(context, descriptor, event)
                        ?: throwUser("Component id ${event.componentId}, tried to resolve custom option with ${parameter.resolver.javaClass.simpleName} on method ${descriptor.method.shortSignature} but result was null")

                    objects[parameter.kParameter] = resolved
                }
                else -> TODO()
            }
        }

        descriptor.method.callSuspendBy(objects)
    }

    private fun onError(event: GenericComponentInteractionCreateEvent, reason: ComponentErrorReason) {
        event.reply_(reason.getReason(context.getDefaultMessages(event)), ephemeral = true).queue()
    }
}