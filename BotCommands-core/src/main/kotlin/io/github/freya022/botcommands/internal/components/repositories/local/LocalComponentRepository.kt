package io.github.freya022.botcommands.internal.components.repositories.local

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import io.github.freya022.botcommands.api.components.annotations.RequiresLocalComponents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.group.AbstractComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.builder.mixin.*
import io.github.freya022.botcommands.internal.components.data.*
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

private val logger = KotlinLogging.logger { }

@BService
@RequiresLocalComponents
internal class LocalComponentRepository {
    @JvmInline
    internal value class DeletedComponent(val componentId: Int) {
        operator fun component1() = componentId
    }

    private val components: TIntObjectMap<ComponentData> = TIntObjectHashMap()
    private var nextId = 0
    private val lock = ReentrantLock()

    internal fun createComponent(builder: BaseComponentBuilderMixin<*>): ComponentData {
        val internalId = lock.withLock { nextId++ }
        val expiresAt: Instant? = builder.timeoutDuration?.let { Clock.System.now() + it }
        val resetTimeoutOnUseDuration: Duration? = builder.timeoutDuration
            ?.takeIf { builder.resetTimeoutOnUse }

        val data: ComponentData = when (builder.lifetimeType) {
            LifetimeType.EPHEMERAL -> {
                builder as IEphemeralActionableComponentMixin<*, *>
                builder as IEphemeralTimeoutableComponentMixin<*>

                EphemeralComponentData(
                    internalId,
                    builder.componentType,
                    expiresAt,
                    resetTimeoutOnUseDuration,
                    builder.filters.toList(),
                    builder.singleUse,
                    builder.rateLimitReference,
                    builder.handler,
                    builder.timeout,
                    builder.constraints.copy(),
                    null,
                )
            }

            LifetimeType.PERSISTENT -> {
                builder as IPersistentActionableComponentMixin<*>
                builder as IPersistentTimeoutableComponentMixin<*>

                PersistentComponentData(
                    internalId,
                    builder.componentType,
                    expiresAt,
                    resetTimeoutOnUseDuration,
                    builder.filters.toList(),
                    builder.singleUse,
                    builder.rateLimitReference,
                    builder.handler,
                    builder.timeout,
                    builder.constraints.copy(),
                    null,
                )
            }
        }

        lock.withLock { components.put(internalId, data) }

        return data
    }

    internal fun getComponent(id: Int): ComponentData? = lock.withLock { components[id] }

    internal fun insertGroup(builder: AbstractComponentGroupBuilder<*>): ComponentGroupData {
        val internalId = lock.withLock { nextId++ }
        val expiresAt: Instant? = builder.timeoutDuration?.let { Clock.System.now() + it }
        val resetTimeoutOnUseDuration: Duration? = builder.timeoutDuration
            ?.takeIf { builder.resetTimeoutOnUse }

        val data = ComponentGroupData(
            internalId,
            builder.lifetimeType,
            expiresAt,
            resetTimeoutOnUseDuration,
            builder.timeout,
            builder.componentIds,
        )

        lock.withLock {
            for (innerComponentId in builder.componentIds) {
                val innerComponent = components[innerComponentId]
                    ?: error("Registered a group with a deleted component")
                if (innerComponent !is ActionComponentData)
                    error("Cannot set a group on a group")

                if (innerComponent.timeout != null) {
                    throwArgument("Cannot put components inside groups if they have a timeout set")
                }

                components.put(innerComponentId, innerComponent.withGroup(data))
            }
        }

        return data
    }

    internal fun deleteComponentsById(ids: Collection<Int>): List<DeletedComponent> = lock.withLock {
        val deletedComponents = ids.mapNotNull { id ->
            components.remove(id)?.let { DeletedComponent(it.internalId) }
        }

        logger.trace { "Deleted components: ${deletedComponents.joinToString()}" }

        return deletedComponents
    }

    internal fun resetExpiration(componentId: Int): Instant = lock.withLock {
        val component = components[componentId]
            ?: throwInternal("Could not find component $componentId to reset expiration")
        if (component !is ActionComponentData) {
            throwInternal("Component to reset expiration is not an action component")
        }

        val newExpiration = Clock.System.now() + component.resetTimeoutOnUseDuration!!
        components.put(componentId, component.withExpiration(newExpiration))

        return newExpiration
    }
}
