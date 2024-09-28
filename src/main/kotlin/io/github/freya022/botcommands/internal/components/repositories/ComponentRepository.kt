package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.db.*
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.BaseComponentBuilderMixin
import io.github.freya022.botcommands.internal.components.builder.IComponentBuilderMixin
import io.github.freya022.botcommands.internal.components.builder.ITimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.builder.group.AbstractComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.controller.ComponentFilters
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.components.data.EphemeralComponentData
import io.github.freya022.botcommands.internal.components.data.PersistentComponentData
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler
import io.github.freya022.botcommands.internal.core.db.InternalDatabase
import io.github.freya022.botcommands.internal.core.exceptions.internalErrorMessage
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.Permission
import java.sql.Timestamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger { }

@BService
@RequiresComponents
internal class ComponentRepository(
    private val database: InternalDatabase,
    private val componentTimeoutRepository: ComponentTimeoutRepository,
    private val componentHandlerRepository: ComponentHandlerRepository,
    private val componentFilters: ComponentFilters
) {
    internal class DeletedComponent(val componentId: Int, val ephemeralComponentHandlerId: Int?, val ephemeralTimeoutHandlerId: Int?) {
        operator fun component1() = componentId
        operator fun component2() = ephemeralComponentHandlerId
        operator fun component3() = ephemeralTimeoutHandlerId
    }

    internal class PersistentComponentTimeout(
        val componentId: Int,
        val instant: Instant
    )

    suspend fun getPersistentComponentTimeouts(): List<PersistentComponentTimeout> {
        return database.preparedStatement(
            "select component_id, expires_at from bc_component where lifetime_type = ? and expires_at is not null",
            readOnly = true
        ) {
            executeQuery(LifetimeType.PERSISTENT.key).map {
                PersistentComponentTimeout(
                    it.getInt("component_id"),
                    it.getKotlinInstant("expires_at")
                )
            }
        }
    }

    suspend fun removeEphemeralComponents(): Int = database.transactional {
        preparedStatement("truncate table bc_ephemeral_timeout") {
            executeUpdate()
        }

        preparedStatement("truncate table bc_ephemeral_handler") {
            executeUpdate()
        }

        val ids: List<Int> = preparedStatement("select component_id from bc_component where lifetime_type = ?") {
            executeQuery(LifetimeType.EPHEMERAL.key).map { it.getInt("component_id") }
        }

        if (ids.isNotEmpty())
            deleteComponentsById(ids)
        ids.size
    }

    suspend fun createComponent(builder: BaseComponentBuilderMixin<*>): ComponentData {
        return database.transactional {
            // Create base component
            val componentId: Int = insertBaseComponent(builder, builder.singleUse, builder.rateLimitReference, getFilterNames(builder.filters))

            // Add constraints
            preparedStatement("insert into bc_component_constraints (component_id, users, roles, permissions) VALUES (?, ?, ?, ?)") {
                executeUpdate(
                    componentId,
                    builder.constraints.allowedUsers.toArray(),
                    builder.constraints.allowedRoles.toArray(),
                    Permission.getRaw(builder.constraints.allowingPermissions)
                )
            }

            // Add handler
            val handler = builder.handler
            if (handler is EphemeralHandler<*>) {
                componentHandlerRepository.insertEphemeralHandler(componentId, handler)
            } else if (handler is PersistentHandler) {
                componentHandlerRepository.insertPersistentHandler(componentId, handler)
            }

            // Add timeout
            insertTimeoutData(builder, componentId)

            getComponent(componentId) ?: throwInternal("Could not find back component with id '$componentId'")
        }
    }

    private fun getFilterNames(list: List<ComponentInteractionFilter<*>>): Array<out String> {
        return Array(list.size) { list[it].javaClass.name }
    }

    suspend fun getComponent(id: Int): ComponentData? = database.transactional(readOnly = true) {
        preparedStatement(
            """
            select lifetime_type, component_type, expires_at, reset_timeout_on_use_duration_ms, one_use, users, roles, permissions, group_id, rate_limit_group, rate_limit_discriminator, filters
            from bc_component component
                     left join bc_component_constraints constraints using (component_id)
                     left join bc_component_component_group componentGroup on componentGroup.component_id = component.component_id
            where component.component_id = ?""".trimIndent()
        ) {
            val dbResult = executeQuery(id).readOrNull() ?: return@preparedStatement null

            val lifetimeType = LifetimeType.fromId(dbResult["lifetime_type"])
            val componentType = ComponentType.fromId(dbResult["component_type"])
            val expiresAt = dbResult.getKotlinInstantOrNull("expires_at")
            val resetTimeoutOnUseDuration: Duration? = dbResult.getOrNull<Int>("reset_timeout_on_use_duration_ms")?.milliseconds

            if (componentType == ComponentType.GROUP) {
                return@preparedStatement getGroup(id, lifetimeType, expiresAt, resetTimeoutOnUseDuration)
            }

            val oneUse: Boolean = dbResult["one_use"]
            val filters = componentFilters.getFilters(dbResult["filters"])
            val rateLimitReference: ComponentRateLimitReference? = dbResult.getString("rate_limit_group")?.let {
                ComponentRateLimitReference(it, dbResult.getString("rate_limit_discriminator"))
            }

            val constraints = InteractionConstraints.of(
                dbResult["users"],
                dbResult["roles"],
                dbResult["permissions"]
            )

            val group = dbResult.getOrNull<Int>("group_id")?.let {
                val group = getComponent(it)
                if (group == null)
                    logger.warn { internalErrorMessage("Could not get group with id $it") }
                else if (group !is ComponentGroupData)
                    logger.warn { internalErrorMessage("Data with id $it was expected to be a group") }
                group as? ComponentGroupData?
            }

            when (lifetimeType) {
                LifetimeType.PERSISTENT -> {
                    val handler = componentHandlerRepository.getPersistentHandler(id)
                    // Avoid request if no timeout is set
                    val timeout = expiresAt?.let {
                        componentTimeoutRepository.getPersistentTimeout(id)
                    }

                    PersistentComponentData(
                        id, componentType,
                        expiresAt, resetTimeoutOnUseDuration,
                        filters,
                        oneUse,
                        rateLimitReference,
                        handler, timeout,
                        constraints,
                        group
                    )
                }

                LifetimeType.EPHEMERAL -> {
                    val handler = componentHandlerRepository.getEphemeralHandler(id)
                    // Avoid request if no timeout is set
                    val timeout = expiresAt?.let {
                        componentTimeoutRepository.getEphemeralTimeout(id)
                    }

                    EphemeralComponentData(
                        id, componentType,
                        expiresAt, resetTimeoutOnUseDuration,
                        filters,
                        oneUse,
                        rateLimitReference,
                        handler, timeout,
                        constraints,
                        group
                    )
                }
            }
        }
    }

    context(Transaction)
    private suspend fun getGroup(
        id: Int,
        lifetimeType: LifetimeType,
        expiresAt: Instant?,
        resetTimeoutOnUseDuration: Duration?,
    ): ComponentGroupData {
        val timeout = when (lifetimeType) {
            LifetimeType.PERSISTENT -> componentTimeoutRepository.getPersistentTimeout(id)
            LifetimeType.EPHEMERAL -> componentTimeoutRepository.getEphemeralTimeout(id)
        }

        val componentIds: List<Int> = preparedStatement(
            """
                select component_id
                from bc_component_component_group
                where group_id = ?
            """.trimIndent()
        ) {
            executeQuery(id).map { it["component_id"] }
        }

        return ComponentGroupData(id, lifetimeType, expiresAt, resetTimeoutOnUseDuration, timeout, componentIds)
    }

    suspend fun insertGroup(builder: AbstractComponentGroupBuilder<*>): ComponentGroupData = database.transactional {
        val groupId: Int = insertBaseComponent(builder, false, null, emptyArray())

        // Add timeout
        insertTimeoutData(builder, groupId)

        // Associate group id to its components, and group id to itself
        suspend fun insertComponentGroupAssociation(componentId: Int) {
            preparedStatement("insert into bc_component_component_group (group_id, component_id) VALUES (?, ?)") {
                executeUpdate(groupId, componentId)
            }
        }
        builder.componentIds.forEach { componentId -> insertComponentGroupAssociation(componentId) }
        insertComponentGroupAssociation(groupId)

        // Check if components inside the group have timeouts
        val hasTimeouts: Boolean = preparedStatement(
            """
                select count(*) > 0
                from (select component_id from bc_component where expires_at is not null) as timeouted_components
                where component_id = any (?);
            """.trimIndent()
        ) {
            executeQuery(builder.componentIds.toTypedArray()).read().getBoolean(1)
        }

        if (hasTimeouts) {
            throwArgument("Cannot put components inside groups if they have a timeout set")
        }

        return@transactional getComponent(groupId) as? ComponentGroupData
            ?: throwInternal("Could not find back component with id '$groupId'")
    }

    context(Transaction)
    private suspend fun insertBaseComponent(
        builder: IComponentBuilderMixin<*>,
        singleUse: Boolean,
        rateLimitReference: ComponentRateLimitReference?,
        filterNames: Array<out String>
    ): Int {
        val expiresAt: Instant?
        val resetTimeoutOnUseDurationMs: Long?
        if (builder is ITimeoutableComponentMixin<*>) {
            expiresAt = builder.timeoutDuration?.let { Clock.System.now() + it }
            resetTimeoutOnUseDurationMs = builder.timeoutDuration
                ?.takeIf { builder.resetTimeoutOnUse }
                ?.inWholeMilliseconds
        } else {
            expiresAt = null
            resetTimeoutOnUseDurationMs = null
        }

        return preparedStatement(
            "insert into bc_component (component_type, lifetime_type, expires_at, reset_timeout_on_use_duration_ms, one_use, rate_limit_group, rate_limit_discriminator, filters) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            columnNames = arrayOf("component_id")
        ) {
            executeReturningUpdate(builder.componentType.key, builder.lifetimeType.key, expiresAt?.toSqlTimestamp(), resetTimeoutOnUseDurationMs, singleUse, rateLimitReference?.group, rateLimitReference?.discriminator, filterNames)
                .read()
                .getInt("component_id")
        }
    }

    context(Transaction)
    private suspend fun insertTimeoutData(timeoutableComponentBuilder: ITimeoutableComponentMixin<*>, componentId: Int) {
        val timeout = timeoutableComponentBuilder.timeout
        if (timeout is EphemeralTimeout) {
            componentTimeoutRepository.insertEphemeralTimeout(componentId, timeout)
        } else if (timeout is PersistentTimeout) {
            componentTimeoutRepository.insertPersistentTimeout(componentId, timeout)
        }
    }

    suspend fun deleteComponentsById(ids: Collection<Int>): List<DeletedComponent> = database.transactional {
        // If the component is a group, then delete the component, and it's contained components
        // If the component is not a group, then delete the component as well as it's group

        val deletedComponents: List<DeletedComponent> = preparedStatement(
            """
                select c.component_id, eh.handler_id as component_handler_id, et.handler_id as timeout_handler_id
                from bc_component c
                         left join bc_ephemeral_handler eh using (component_id)
                         left join bc_ephemeral_timeout et using (component_id)
                where c.component_id = any (?) -- Delete this component
                   or c.component_id = any
                      (select component_id -- (This component is a group) Delete all components from the same group
                       from bc_component_component_group
                       where group_id = any (?))
                   or c.component_id = any
                      (select g.component_id -- (This component is not a group) Find all components from the same group and delete them
                       from bc_component_component_group c
                                join bc_component_component_group g on c.group_id = g.group_id
                       where c.component_id = any (?))
            """.trimIndent()
        ) {
            val idArray = ids.toTypedArray()
            executeQuery(idArray, idArray, idArray).map { DeletedComponent(it["component_id"], it.getOrNull("component_handler_id"), it.getOrNull("timeout_handler_id")) }
        }
        val deletedComponentIds = deletedComponents.map { it.componentId }

        preparedStatement("delete from bc_component where component_id = any (?)") {
            executeUpdate(deletedComponentIds.toTypedArray())
        }

        logger.trace { "Deleted components: ${deletedComponentIds.joinToString()}" }

        return@transactional deletedComponents
    }

    internal suspend fun resetExpiration(componentId: Int): Instant? = database.transactional {
        preparedStatement(
            """
                update bc_component
                set expires_at = now() + (reset_timeout_on_use_duration_ms || 'milliseconds')::interval
                where component_id = ?
                returning expires_at
            """.trimIndent(),
            columnNames = arrayOf("expires_at")
        ) {
            executeReturningUpdate(componentId)
                .read()
                // now() + null = null
                .getKotlinInstantOrNull("expires_at")
        }
    }

    private fun Instant.toSqlTimestamp(): Timestamp = Timestamp.from(this.toJavaInstant())
}