package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.components.builder.*
import com.freya02.botcommands.api.components.data.LambdaButtonData
import com.freya02.botcommands.api.components.data.LambdaSelectionMenuData
import com.freya02.botcommands.api.components.data.PersistentButtonData
import com.freya02.botcommands.api.components.data.PersistentSelectionMenuData
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.components.sql.SQLFetchResult
import com.freya02.botcommands.internal.components.sql.SQLFetchedComponent
import com.freya02.botcommands.internal.components.sql.SQLLambdaComponentData
import com.freya02.botcommands.internal.components.sql.SQLPersistentComponentData
import com.freya02.botcommands.internal.core.db.Database
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.time.Duration.Companion.milliseconds

class DefaultComponentManager internal constructor(private val database: Database, private val context: BContextImpl) : ComponentManager {
    private val logger = Logging.getLogger()

    private val buttonLambdaMap: MutableMap<Long, ButtonConsumer> = hashMapOf()
    private val selectionMenuLambdaMap: MutableMap<Long, SelectionConsumer> = hashMapOf()

    init {
        database.transactional {
            preparedStatement("delete from bc_component_data using bc_lambda_component_data where type in (?, ?);") {
                runBlocking {
                    executeUpdate(ComponentType.LAMBDA_BUTTON.key, ComponentType.LAMBDA_SELECTION_MENU.key)
                }
            }
        }
    }

    override fun fetchComponent(id: String, callback: Consumer<FetchResult>) {
        database.transactional {
            runCatching {
                preparedStatement(
                    """
                    select *
                    from bc_component_data
                             left join bc_lambda_component_data using (component_id)
                             left join bc_persistent_component_data using (component_id)
                    where component_id = ?
                    limit 1;""".trimIndent()
                ) {
                    runBlocking {
                        when (val result = executeQuery(*arrayOf(id)).readOnce()) {
                            null -> SQLFetchResult(null, null)
                            else -> SQLFetchResult(SQLFetchedComponent(result), this@transactional)
                        }
                    }
                }
            }.onFailure { e ->
                logger.error("Unable to fetch a component of ID '$id'", e)
                SQLFetchResult(null, null).use { callback.accept(it) }
            }.onSuccess { result ->
                result.use { callback.accept(it) }
            }
        }
    }

    override fun handleLambdaButton(
        event: GenericComponentInteractionCreateEvent,
        fetchResult: FetchResult,
        onError: Consumer<ComponentErrorReason>,
        dataConsumer: Consumer<LambdaButtonData>
    ) {
        handleLambdaComponent(event, fetchResult, onError, buttonLambdaMap) {
            dataConsumer.accept(LambdaButtonData(it))
        }
    }

    override fun handleLambdaSelectMenu(
        event: GenericComponentInteractionCreateEvent,
        fetchResult: FetchResult,
        onError: Consumer<ComponentErrorReason>,
        dataConsumer: Consumer<LambdaSelectionMenuData>
    ) {
        handleLambdaComponent(event, fetchResult, onError, selectionMenuLambdaMap) {
            dataConsumer.accept(LambdaSelectionMenuData(it))
        }
    }

    private fun <C : ComponentConsumer<*>> handleLambdaComponent(
        event: GenericComponentInteractionCreateEvent,
        fetchResult: FetchResult,
        onError: Consumer<ComponentErrorReason>,
        consumerMap: MutableMap<Long, C>,
        consumerHandler: (C) -> Unit
    ) {
        fetchResult as SQLFetchResult

        val fetchedComponent = fetchResult.fetchedComponent ?: throwInternal("Cannot handle lambda component with a fetch error")
        val componentData = SQLLambdaComponentData.fromFetchedComponent(fetchedComponent)

        val result = componentData.handleComponentData(event)
        result.errorReason?.let {
            onError.accept(it)
            return
        }

        val handlerId = componentData.handlerId
        val consumer = when {
            result.shouldDelete -> consumerMap.remove(handlerId).also { componentData.delete(fetchResult.transaction) }
            else -> consumerMap[handlerId]
        } ?: throwUser("Could not find a consumer for handler id '$handlerId' on component ${event.componentId}")

        consumerHandler(consumer)
    }

    override fun handlePersistentButton(
        event: GenericComponentInteractionCreateEvent,
        fetchResult: FetchResult,
        onError: Consumer<ComponentErrorReason>,
        dataConsumer: Consumer<PersistentButtonData>
    ) {
        handlePersistentComponent(event, fetchResult, onError) { handlerName, args ->
            dataConsumer.accept(PersistentButtonData(handlerName, args))
        }
    }

    override fun handlePersistentSelectMenu(
        event: GenericComponentInteractionCreateEvent,
        fetchResult: FetchResult,
        onError: Consumer<ComponentErrorReason>,
        dataConsumer: Consumer<PersistentSelectionMenuData>
    ) {
        handlePersistentComponent(event, fetchResult, onError) { handlerName, args ->
            dataConsumer.accept(PersistentSelectionMenuData(handlerName, args))
        }
    }

    private fun handlePersistentComponent(
        event: GenericComponentInteractionCreateEvent,
        fetchResult: FetchResult,
        onError: Consumer<ComponentErrorReason>,
        consumerHandler: (String, Array<out String>) -> Unit
    ) {
        fetchResult as SQLFetchResult

        val fetchedComponent = fetchResult.fetchedComponent ?: throwInternal("Cannot handle lambda component with a fetch error")
        val componentData = SQLPersistentComponentData.fromFetchedComponent(fetchedComponent)

        val result = componentData.handleComponentData(event)
        result.errorReason?.let {
            onError.accept(it)
            return
        }

        if (result.shouldDelete) {
            componentData.delete(fetchResult.transaction)
        }

        consumerHandler(componentData.handlerName, componentData.args)
    }

    private fun scheduleTimeout(timeout: Long, timeoutUnit: TimeUnit, block: suspend () -> Unit) {
        if (timeout == 0L) throwInternal("Scheduled timeout should be positive")

        context.config.coroutineScopesConfig.componentsScope.launch {
            try {
                delay(timeoutUnit.toMillis(timeout).milliseconds)
                block()
            } catch (e: Throwable) {
                logger.error("An error occurred in a scheduled timeout handler", e)
                context.dispatchException("An error occurred in a scheduled timeout handler", e)
            }
        }
    }

    private fun scheduleLambdaTimeout(map: MutableMap<Long, *>, timeout: LambdaComponentTimeoutInfo, handlerId: Long, componentId: String) {
        scheduleTimeout(timeout.timeout, timeout.timeoutUnit) {
            database.transactional {
                val componentData = SQLLambdaComponentData.read(componentId) ?: return@scheduleTimeout

                map.remove(handlerId)
                componentData.delete(this@transactional)
                timeout.timeoutCallback.run()
            }
        }
    }

    private fun schedulePersistentTimeout(timeout: PersistentComponentTimeoutInfo, componentId: String) {
        scheduleTimeout(timeout.timeout, timeout.timeoutUnit) {
            database.transactional {
                val componentData = SQLPersistentComponentData.read(componentId) ?: return@scheduleTimeout
                componentData.delete(this@transactional)
            }
        }
    }

    override fun putLambdaButton(builder: LambdaButtonBuilder) = putLambdaComponent(
        buttonLambdaMap,
        builder,
        ComponentType.LAMBDA_BUTTON
    )

    override fun putLambdaSelectMenu(builder: LambdaSelectionMenuBuilder) = putLambdaComponent(
        selectionMenuLambdaMap,
        builder,
        ComponentType.LAMBDA_SELECTION_MENU
    )

    private fun <C : ComponentConsumer<*>> putLambdaComponent(
        map: MutableMap<Long, C>, builder: LambdaComponentBuilder<*, C>,
        componentType: ComponentType
    ): String {
        try {
            database.transactional {
                val result = SQLLambdaComponentData.create(
                    componentType,
                    builder.isOneUse,
                    builder.interactionConstraints,
                    builder.timeout
                )

                map[result.handlerId] = builder.consumer

                if (builder.timeout.timeout() > 0) {
                    scheduleLambdaTimeout(buttonLambdaMap, builder.timeout, result.handlerId, result.componentId)
                }

                return result.componentId
            }
        } catch (e: Throwable) {
            throw RuntimeException("Unable to insert a lambda component", e)
        }
    }

    override fun putPersistentButton(builder: PersistentButtonBuilder) = putPersistentComponent(builder, ComponentType.PERSISTENT_BUTTON)

    override fun putPersistentSelectMenu(builder: PersistentSelectionMenuBuilder) = putPersistentComponent(builder,
        ComponentType.PERSISTENT_SELECTION_MENU
    )

    private fun putPersistentComponent(builder: PersistentComponentBuilder<*>, type: ComponentType): String {
        try {
            database.transactional {
                val componentId = SQLPersistentComponentData.create(
                    type,
                    builder.isOneUse,
                    builder.interactionConstraints,
                    builder.timeout,
                    builder.handlerName,
                    builder.args
                )


                if (builder.timeout.timeout() > 0) {
                    schedulePersistentTimeout(builder.timeout, componentId)
                }

                return componentId
            }
        } catch (e: Throwable) {
            throw RuntimeException("Unable to insert a lambda component", e)
        }
    }

    override fun registerGroup(ids: Collection<String>) {
        try {
            database.transactional {
                preparedStatement(
                    """
                        select nextval('bc_component_group_seq');
                        
                        update bc_component_data
                        set group_id = currval('bc_component_group_seq')
                        where component_id = any (?);
                        """.trimIndent()
                ) {
                    runBlocking { execute(connection.createArrayOf("text", ids.toTypedArray())) } //TODO test
                }
            }
        } catch (e: Throwable) {
            throw RuntimeException("Unable to register a component group", e)
        }
    }

    override fun deleteIds(ids: Collection<String>): Int {
        if (ids.isEmpty()) return 0

        try {
            database.transactional {
                return runBlocking {
                    preparedStatement("delete from bc_lambda_component_data where component_id = any(?) returning handler_id;") {
                        //TODO test
                        executeQuery(connection.createArrayOf("text", ids.toTypedArray())).forEach {
                            val handlerId: Long = it["handler_id"]

                            //handler id is actually a shared sequence so there can't be duplicates even if we merged both maps
                            buttonLambdaMap.remove(handlerId) ?: selectionMenuLambdaMap.remove(handlerId)
                        }
                    }

                    preparedStatement("delete from bc_component_data where component_id = any(?);") {
                        return@runBlocking executeUpdate(connection.createArrayOf("text", ids.toTypedArray()))
                    }
                }
            }
        } catch (e: Throwable) {
            throw RuntimeException("Unable to delete components", e)
        }
    }
}