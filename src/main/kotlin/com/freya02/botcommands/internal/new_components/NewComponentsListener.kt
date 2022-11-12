package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.SelectionEvent
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.api.new_components.ComponentGroup
import com.freya02.botcommands.api.new_components.ComponentTimeoutData
import com.freya02.botcommands.api.new_components.GroupTimeoutData
import com.freya02.botcommands.api.new_components.NewComponents
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.components.ComponentHandlerParameter
import com.freya02.botcommands.internal.components.ComponentsHandlerContainer
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.core.db.Database
import com.freya02.botcommands.internal.data.DataEntity
import com.freya02.botcommands.internal.data.DataStoreService
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.data.annotations.DataStoreTimeoutHandler
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@ConditionalService(dependencies = [NewComponents::class, DataStoreService::class, Database::class])
internal class NewComponentsListener(
    private val context: BContextImpl,
    private val componentsHandlerContainer: ComponentsHandlerContainer,
    private val dataStore: DataStoreService,
    private val coroutinesScopesConfig: BCoroutineScopesConfig,
    private val componentTimeoutHandlers: ComponentTimeoutHandlers,
    private val groupTimeoutHandlers: GroupTimeoutHandlers,
    private val serviceContainer: ServiceContainer,
    private val database: Database
) {
    private val logger = KotlinLogging.logger { }

    @BEventListener
    internal fun onComponentInteraction(event: GenericComponentInteractionCreateEvent) = coroutinesScopesConfig.componentsScope.launch {
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

                    if (!componentData.oneUse) return@launch

                    database.transactional {
                        //Delete all components of the group
                        preparedStatement("select * from bc_data where jsonb_exists(data::jsonb->'componentsIds', ?)") {
                            executeQuery(*arrayOf(data.id)).forEach {
                                val componentGroup = DataEntity.fromDBResult(it).decodeData<ComponentGroup>()
                                dataStore.deleteData(componentGroup.componentsIds).also { deletedComponents ->
                                    logger.trace { "Deleted $deletedComponents/${componentGroup.componentsIds.size} components from a group" }
                                }
                            }
                        }

                        //Delete this component, in case it wasn't in a group
                        preparedStatement("delete from bc_data where id = ?") {
                            executeUpdate(*arrayOf(data.id))
                        }
                    }
                }
                LifetimeType.EPHEMERAL -> TODO()
            }
        } catch (e: Throwable) {
            handleException(event, e)
        }
    }

    @DataStoreTimeoutHandler(TIMEOUT_HANDLER_NAME)
    internal fun onComponentTimeout(dataEntity: DataEntity) = coroutinesScopesConfig.componentsScope.launch {
        try {
            when (dataEntity.getDataType<ComponentType>()) {
                ComponentType.GROUP -> handleGroupTimeout(dataEntity)
                ComponentType.BUTTON, ComponentType.SELECT_MENU -> handleComponentTimeout(dataEntity)
            }
        } catch (e: Throwable) {
            handleTimeoutException(dataEntity.id, e)
        }
    }

    private suspend fun handleGroupTimeout(dataEntity: DataEntity) {
        val componentGroup = dataEntity.decodeData<ComponentGroup>()
        dataStore.deleteData(componentGroup.componentsIds).also { deletedComponents ->
            logger.trace { "Deleted $deletedComponents/${componentGroup.componentsIds.size} components from a group" }
        }

        componentGroup.timeout?.let {
            val handlerName = it.handlerName ?: return
            val handler = groupTimeoutHandlers[handlerName] ?: let {
                logger.warn("Could not find group timeout handler: $handlerName")
                return
            }

            callTimeoutHandler(handler, GroupTimeoutData(componentGroup.componentsIds))
        }
    }

    private suspend fun callTimeoutHandler(handler: KFunction<*>, firstParameter: Any): Any? {
        val args = hashMapOf<KParameter, Any?>()
        args[handler.instanceParameter!!] = serviceContainer.getFunctionService(handler)
        args[handler.valueParameters.first()] = firstParameter

        handler.valueParameters.drop(1).forEach { kParameter ->
            args[kParameter] = serviceContainer.getService(kParameter.type.jvmErasure)
        }

        return handler.callSuspendBy(args)
    }

    private suspend fun handleComponentTimeout(dataEntity: DataEntity) {
        when (dataEntity.lifetimeType) {
            LifetimeType.PERSISTENT -> {
                val componentData = dataEntity.decodeData<PersistentComponentData>()
                componentData.timeoutInfo?.let {
                    val handlerName = it.handlerName ?: return
                    val handler = componentTimeoutHandlers[handlerName] ?: let {
                        logger.warn("Could not find component timeout handler: $handlerName")
                        return
                    }

                    callTimeoutHandler(handler, ComponentTimeoutData(dataEntity.id))
                }
            }
            LifetimeType.EPHEMERAL -> TODO()
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

        context.dispatchException("Exception in component handler with id ${event.componentId}", baseEx)
    }

    private fun handleTimeoutException(componentId: String, e: Throwable) {
        context.uncaughtExceptionHandler?.let { handler ->
            handler.onException(context, null, e)
            return
        }

        val baseEx = e.getDeepestCause()

        logger.error("Unhandled exception while executing a component timeout with id $componentId", baseEx)
        context.dispatchException("Exception in component handler with id $componentId", baseEx)
    }

    companion object {
        internal const val TIMEOUT_HANDLER_NAME = "NewComponentsListener: timeoutHandler"
    }
}