package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.internal.components.builder.group.AbstractComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.builder.mixin.BaseComponentBuilderMixin
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import kotlinx.datetime.Clock
import kotlin.time.Instant

@InterfacedService(acceptMultiple = false)
internal abstract class ComponentController {
    protected val logger = objectLogger()

    internal abstract val context: BContext
    internal abstract val continuationManager: ComponentContinuationManager
    protected abstract val timeoutManager: ComponentTimeoutManager

    // This service might be used in classes that use components and also declare rate limiters
    private val rateLimitContainer: RateLimitContainer by lazy { context.getService() }
    private val rateLimitReferences: MutableSet<ComponentRateLimitReference> = hashSetOf()

    internal suspend fun <R> withNewComponent(builder: BaseComponentBuilderMixin<*>, block: (internalId: Int, componentId: String) -> R): R {
        builder.rateLimitReference?.let { rateLimitReference ->
            require(rateLimitReference.group in rateLimitContainer) {
                "Rate limit group '${rateLimitReference.group}' was not registered using ${classRef<RateLimitProvider>()}"
            }
        }

        builder.filters.onEach { filter ->
            val filterClass = filter.javaClass
            require(!filter.global) {
                "Global filter ${filterClass.simpleNestedName} cannot be used explicitly, see ${Filter::global.reference}"
            }

            requireNotNull(context.serviceContainer.getServiceOrNull(filterClass)) {
                "Component filters must be accessible via dependency injection, " +
                        "filters such as composite filters created with 'and' / 'or' cannot be passed. " +
                        "See ${classRef<ComponentInteractionFilter>()} for more details."
            }
        }

        if (builder.resetTimeoutOnUse && builder.timeoutDuration?.takeIfFinite() == null) {
            logger.warn { "Using 'resetTimeoutOnUse' has no effect when no timeout is set" }
        }

        val component = createComponent(builder)

        component.expiresAt?.let { expirationTimestamp ->
            timeoutManager.scheduleTimeout(component.internalId, expirationTimestamp)
        }

        val internalId = component.internalId
        return block(internalId, getComponentId(internalId))
    }

    protected abstract suspend fun createComponent(builder: BaseComponentBuilderMixin<*>): ComponentData

    internal suspend fun getActiveComponent(componentId: Int): ComponentData? {
        return getComponent(componentId)
            ?.takeUnless {
                val expiresAt = it.expiresAt
                expiresAt != null && expiresAt <= Clock.System.now()
            }
    }

    internal abstract suspend fun getComponent(componentId: Int): ComponentData?

    internal suspend fun tryResetTimeout(component: ComponentData) {
        // Components in groups cannot have timeouts,
        // so if there's a group, only reset the group timeout
        val group = (component as? ActionComponentData)?.group
        if (group != null) {
            tryResetTimeout(group)
        } else {
            if (component.resetTimeoutOnUseDuration == null) return

            // Cancel, reset in DB, schedule
            timeoutManager.cancelTimeout(component.internalId)
            val newExpirationTimestamp = resetExpiration(component.internalId)
            timeoutManager.scheduleTimeout(component.internalId, newExpirationTimestamp)
        }
    }

    protected abstract suspend fun resetExpiration(internalId: Int): Instant

    internal suspend fun deleteComponent(component: ComponentData, throwTimeouts: Boolean) =
        deleteComponentsById(listOf(component.internalId), throwTimeouts)

    internal suspend fun createGroup(builder: AbstractComponentGroupBuilder<*>): ComponentGroup {
        val group = createGroupData(builder)

        group.expiresAt?.let { expirationTimestamp ->
            timeoutManager.scheduleTimeout(group.internalId, expirationTimestamp)
        }

        return ComponentGroup(this, group.internalId)
    }

    protected abstract suspend fun createGroupData(builder: AbstractComponentGroupBuilder<*>): ComponentGroupData

    internal abstract suspend fun deleteComponentsById(ids: Collection<Int>, throwTimeouts: Boolean)

    internal fun createRateLimitReference(group: String, discriminator: String): ComponentRateLimitReference {
        val ref = ComponentRateLimitReference(group, discriminator)
        check(rateLimitReferences.add(ref)) {
            "A component rate limit reference already exists with such group and discriminator. " +
                    "As a reminder, each component must use a different discriminator."
        }
        return ref
    }

    internal fun getRateLimitReference(group: String, discriminator: String): ComponentRateLimitReference? {
        val ref = ComponentRateLimitReference(group, discriminator)
        return ref.takeIf { ref in rateLimitReferences }
    }

    internal companion object {
        private const val PREFIX = "BotCommands-Components-"
        private const val PREFIX_LENGTH = PREFIX.length

        internal fun isCompatibleComponent(id: String): Boolean = id.startsWith(PREFIX)

        internal fun parseComponentId(id: String): Int = Integer.parseInt(id, PREFIX_LENGTH, id.length, 10)

        internal fun getComponentId(internalId: Int): String = PREFIX + internalId
    }
}
