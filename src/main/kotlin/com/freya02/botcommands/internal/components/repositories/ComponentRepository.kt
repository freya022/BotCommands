package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.builder.BaseComponentBuilder
import com.freya02.botcommands.api.components.builder.ITimeoutableComponent
import com.freya02.botcommands.api.components.builder.group.ComponentGroupBuilder
import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.Dependencies
import com.freya02.botcommands.api.core.db.Transaction
import com.freya02.botcommands.api.core.db.transactional
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.EphemeralHandler
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.PersistentHandler
import com.freya02.botcommands.internal.components.controller.ComponentTimeoutManager
import com.freya02.botcommands.internal.components.data.*
import com.freya02.botcommands.internal.core.db.InternalDatabase
import com.freya02.botcommands.internal.rethrowUser
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import java.sql.SQLException
import java.sql.Timestamp

@BService
@Dependencies([Components::class])
internal class ComponentRepository(
    private val database: InternalDatabase,
    private val ephemeralComponentHandlers: EphemeralComponentHandlers,
    private val ephemeralTimeoutHandlers: EphemeralTimeoutHandlers
) {
    private val logger = KotlinLogging.logger { }

    init {
        cleanupEphemeral()
    }

    fun createComponent(builder: BaseComponentBuilder): Int = runBlocking {
        database.transactional {
            // Create base component
            val componentId: Int =
                preparedStatement("insert into bc_component (component_type, lifetime_type, one_use) VALUES (?, ?, ?) returning component_id") {
                    executeQuery(builder.componentType.key, builder.lifetimeType.key, builder.oneUse)
                        .readOnce()
                        ?.get<Int>("component_id") ?: throwInternal("Component was created without returning an ID")
                }

            // Add constraints
            preparedStatement("insert into bc_component_constraints (component_id, users, roles, permissions) VALUES (?, ?, ?, ?)") {
                executeUpdate(
                    componentId,
                    builder.constraints.userList.toArray(),
                    builder.constraints.roleList.toArray(),
                    Permission.getRaw(builder.constraints.permissions)
                )
            }

            // Add handler
            val handler = builder.handler
            if (handler is EphemeralHandler<*>) {
                preparedStatement("insert into bc_ephemeral_handler (component_id, handler_id) VALUES (?, ?)") {
                    executeUpdate(componentId, ephemeralComponentHandlers.put(handler))
                }
            } else if (handler is PersistentHandler) {
                preparedStatement("insert into bc_persistent_handler (component_id, handler_name, user_data) VALUES (?, ?, ?)") {
                    executeUpdate(componentId, handler.handlerName, handler.userData)
                }
            }

            // Add timeout
            insertTimeoutData(builder, componentId)

            return@transactional componentId
        }
    }

    suspend fun getComponent(id: Int): ComponentData? = database.transactional(readOnly = true) {
        preparedStatement(
            """
            select lifetime_type, component_type, one_use, users, roles, permissions
            from bc_component
                     natural left join bc_component_constraints
            where component_id = ?""".trimIndent()
        ) {
            val dbResult = executeQuery(id).readOnce() ?: return@preparedStatement null

            val lifetimeType = LifetimeType.fromId(dbResult["lifetime_type"])
            val componentType = ComponentType.fromId(dbResult["component_type"])
            val oneUse: Boolean = dbResult["one_use"]

            if (componentType == ComponentType.GROUP) {
                return@preparedStatement getGroup(id, oneUse)
            }

            val constraints = InteractionConstraints.of(
                dbResult["users"],
                dbResult["roles"],
                dbResult["permissions"]
            )

            when (lifetimeType) {
                LifetimeType.PERSISTENT -> getPersistentComponent(
                    id,
                    componentType,
                    lifetimeType,
                    oneUse,
                    constraints
                )
                LifetimeType.EPHEMERAL -> getEphemeralComponent(
                    id,
                    componentType,
                    lifetimeType,
                    oneUse,
                    constraints
                )
            }
        }
    }

    suspend fun insertGroup(builder: ComponentGroupBuilder): Int = database.transactional {
        val groupId: Int = runCatching {
            preparedStatement(
                """
                insert into bc_component (component_type, lifetime_type, one_use)
                VALUES (?, ?, false)
                returning component_id""".trimIndent()
            ) {
                executeQuery(ComponentType.GROUP.key, builder.lifetimeType.key).readOnce()!!
                    .get<Int>("component_id")
            }
        }.onFailure {
            if (it is SQLException && it.errorCode == 23523) { //foreign_key_violation, the component does not exist
                rethrowUser("Attempted to put a group ID to an external component: ${it.message}", it)
            }
        }.getOrThrow()

        // Add timeout
        insertTimeoutData(builder, groupId)

        (builder.componentIds + groupId).forEach { componentId ->
            preparedStatement("insert into bc_component_component_group (group_id, component_id) VALUES (?, ?)") {
                executeUpdate(groupId, componentId)
            }
        }

        // Check if components inside group have timeouts
        val hasTimeouts: Boolean = preparedStatement(
            """
            select count(*) > 0 as exists
            from bc_persistent_timeout
                     natural left join bc_ephemeral_timeout
            where component_id = any (?)""".trimIndent()
        ) {
            executeQuery(builder.componentIds.toTypedArray()).readOnce()!!["exists"]
        }

        if (hasTimeouts) {
            throwUser("Cannot put components inside groups if they have a timeout set")
        }

        return@transactional groupId
    }

    context(Transaction)
    private suspend fun insertTimeoutData(timeoutableComponentBuilder: ITimeoutableComponent, groupId: Int) {
        val timeout = timeoutableComponentBuilder.timeout
        if (timeout is EphemeralTimeout) {
            preparedStatement("insert into bc_ephemeral_timeout (component_id, expiration_timestamp, handler_id) VALUES (?, ?, ?)") {
                executeUpdate(
                    groupId,
                    Timestamp.from(timeout.expirationTimestamp.toJavaInstant()),
                    timeout.handler?.let { ephemeralTimeoutHandlers.put(it) }
                )
            }
        } else if (timeout is PersistentTimeout) {
            preparedStatement("insert into bc_persistent_timeout (component_id, expiration_timestamp, handler_name, user_data) VALUES (?, ?, ?, ?)") {
                executeUpdate(
                    groupId,
                    timeout.expirationTimestamp.toSqlTimestamp(),
                    timeout.handlerName,
                    timeout.userData
                )
            }
        }
    }

    /** Returns all deleted components */
    suspend fun deleteComponent(componentId: Int): List<Int> = deleteComponentsById(listOf(componentId))

    suspend fun deleteComponentsById(ids: List<Int>): List<Int> = database.transactional {
        // If the component is a group, then delete the component, and it's contained components
        // If the component is not a group, then delete the component as well as it's group

        val deletedComponents: List<Int> = preparedStatement(
            """
                delete
                from bc_component c
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
                returning c.component_id
            """.trimIndent()
        ) {
            val idArray = ids.toTypedArray()
            executeQuery(idArray, idArray, idArray).map { it["component_id"] }
        }

        logger.trace { "Deleted components: ${deletedComponents.joinToString()}" }

        return@transactional deletedComponents
    }

    suspend fun scheduleExistingTimeouts(timeoutManager: ComponentTimeoutManager) = database.transactional(readOnly = true) {
        preparedStatement("select component_id, expiration_timestamp, handler_name, user_data from bc_persistent_timeout") {
            executeQuery(*arrayOf()).forEach { dbResult ->
                timeoutManager.scheduleTimeout(
                    dbResult["component_id"], PersistentTimeout(
                        dbResult.get<Timestamp>("expiration_timestamp").toInstant().toKotlinInstant(),
                        dbResult["handler_name"],
                        dbResult["user_data"]
                    )
                )
            }
        }
    }

    context(Transaction)
    private suspend fun getPersistentComponent(
        id: Int,
        componentType: ComponentType,
        lifetimeType: LifetimeType,
        oneUse: Boolean,
        constraints: InteractionConstraints
    ): PersistentComponentData = preparedStatement(
        """
           select ph.handler_name         as handler_handler_name,
                  ph.user_data            as handler_user_data,
                  pt.expiration_timestamp as timeout_expiration_timestamp,
                  pt.handler_name         as timeout_handler_name,
                  pt.user_data            as timeout_user_data
           from bc_persistent_handler ph
                    full outer join bc_persistent_timeout pt using (component_id)
           where ph.component_id = ?;
        """.trimIndent()
    ) {
        // There is no rows if neither a handler nor a timeout has been set
        val dbResult = executeQuery(id).readOnce()
            ?: return PersistentComponentData(id, componentType, lifetimeType, oneUse, handler = null, timeout = null, constraints)

        val handler = dbResult.getOrNull<String>("handler_handler_name")?.let { handlerName ->
            PersistentHandler(
                handlerName,
                dbResult["handler_user_data"]
            )
        }

        val timeout = dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
            PersistentTimeout(
                timestamp.toInstant().toKotlinInstant(),
                dbResult["timeout_handler_name"],
                dbResult["timeout_user_data"]
            )
        }

        PersistentComponentData(id, componentType, lifetimeType, oneUse, handler, timeout, constraints)
    }

    context(Transaction)
    private suspend fun getEphemeralComponent(
        id: Int,
        componentType: ComponentType,
        lifetimeType: LifetimeType,
        oneUse: Boolean,
        constraints: InteractionConstraints
    ): EphemeralComponentData = preparedStatement(
        """
            select ph.handler_id           as handler_handler_id,
                   pt.expiration_timestamp as timeout_expiration_timestamp,
                   pt.handler_id           as timeout_handler_id
            from bc_ephemeral_handler ph
                     full outer join bc_ephemeral_timeout pt using (component_id)
            where component_id = ?;
        """.trimIndent()
    ) {
        // There is no rows if neither a handler nor a timeout has been set
        val dbResult = executeQuery(id).readOnce()
            ?: return EphemeralComponentData(id, componentType, lifetimeType, oneUse, handler = null, timeout = null, constraints)

        val handler = dbResult.getOrNull<Int>("handler_handler_id")?.let { handlerId ->
            ephemeralComponentHandlers[handlerId]
                ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
        }

        val timeout = dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
            EphemeralTimeout(
                timestamp.toInstant().toKotlinInstant(),
                dbResult.getOrNull<Int>("timeout_handler_id")?.let { handlerId ->
                    ephemeralTimeoutHandlers[handlerId]
                        ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
                }
            )
        }

        EphemeralComponentData(id, componentType, lifetimeType, oneUse, handler, timeout, constraints)
    }

    context(Transaction)
    private suspend fun getGroup(id: Int, oneUse: Boolean): ComponentGroupData {
        val timeout = getGroupTimeout(id)

        val componentIds: List<Int> = preparedStatement(
            """
                select component_id
                from bc_component_component_group
                where group_id = ?
            """.trimIndent()
        ) {
            executeQuery(id).map { it["component_id"] }
        }

        return ComponentGroupData(id, oneUse, timeout, componentIds)
    }

    context(Transaction)
    private suspend fun getGroupTimeout(id: Int): ComponentTimeout? {
        preparedStatement(
            """
               select pt.expiration_timestamp as timeout_expiration_timestamp,
                      pt.handler_name         as timeout_handler_name,
                      pt.user_data            as timeout_user_data
               from bc_persistent_timeout pt
               where component_id = ?;
            """.trimIndent()
        ) {
            val dbResult = executeQuery(id).readOnce() ?: return@preparedStatement null

            dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
                return PersistentTimeout(
                    timestamp.toInstant().toKotlinInstant(),
                    dbResult["timeout_handler_name"],
                    dbResult["timeout_user_data"]
                )
            }
        }

        //In case there's no persistent timeout handler
        preparedStatement(
            """
               select pt.expiration_timestamp as timeout_expiration_timestamp,
                      pt.handler_id           as timeout_handler_id
               from bc_ephemeral_timeout pt
               where component_id = ?;
            """.trimIndent()
        ) {
            val dbResult = executeQuery(id).readOnce() ?: return@preparedStatement null

            dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
                val handlerId: Int = dbResult["timeout_handler_id"]
                return EphemeralTimeout(
                    timestamp.toInstant().toKotlinInstant(),
                    ephemeralTimeoutHandlers[handlerId]
                        ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
                )
            }
        }

        return null
    }

    @Suppress("SqlWithoutWhere")
    private fun cleanupEphemeral() = runBlocking {
        database.transactional {
            preparedStatement("truncate bc_ephemeral_timeout") {
                val deletedRows = executeUpdate()
                logger.trace { "Deleted $deletedRows ephemeral timeout handlers" }
            }

            preparedStatement("truncate bc_ephemeral_handler") {
                val deletedRows = executeUpdate()
                logger.trace { "Deleted $deletedRows ephemeral handlers" }
            }

            preparedStatement("delete from bc_component where lifetime_type = ?") {
                val deletedRows = executeUpdate(LifetimeType.EPHEMERAL.key)
                logger.trace { "Deleted $deletedRows ephemeral components" }
            }
        }
    }

    private fun Instant.toSqlTimestamp(): Timestamp? = Timestamp.from(this.toJavaInstant())
}