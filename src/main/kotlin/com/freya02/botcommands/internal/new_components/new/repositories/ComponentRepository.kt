package com.freya02.botcommands.internal.new_components.new.repositories

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.api.new_components.builder.ComponentGroupBuilder
import com.freya02.botcommands.internal.core.db.Database
import com.freya02.botcommands.internal.core.db.Transaction
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.*
import com.freya02.botcommands.internal.new_components.new.*
import com.freya02.botcommands.internal.new_components.new.EphemeralComponentData
import com.freya02.botcommands.internal.new_components.new.PersistentComponentData
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

@BService //TODO removing it still works
internal class ComponentRepository(
    private val database: Database,
    private val ephemeralComponentHandlers: EphemeralComponentHandlers,
    private val ephemeralTimeoutHandlers: EphemeralTimeoutHandlers
) {
    private val logger = KotlinLogging.logger { }

    init {
        cleanupEphemeral()

//        runBlocking {
//            val component = getComponent(9)
//
//            println(component)
//
//            exitProcess(0)
//        }
    }

    fun createComponent(builder: ComponentBuilder): Int = database.transactional {
        runBlocking { //TODO not sure about coroutines
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
            val timeout = builder.timeout
            if (timeout is EphemeralTimeout) {
                preparedStatement("insert into bc_ephemeral_timeout (component_id, expiration_timestamp, handler_id) VALUES (?, ?, ?)") {
                    executeUpdate(
                        componentId,
                        Timestamp.from(timeout.expirationTimestamp.toJavaInstant()),
                        ephemeralTimeoutHandlers.put(timeout.handler)
                    )
                }
            } else if (timeout is PersistentTimeout) {
                preparedStatement("insert into bc_persistent_timeout (component_id, expiration_timestamp, handler_name, user_data) VALUES (?, ?, ?, ?)") {
                    executeUpdate(componentId, timeout.expirationTimestamp.toSqlTimestamp(), timeout.handlerName, timeout.userData)
                }
            }

            return@runBlocking componentId
        }
    }

    suspend fun getComponent(id: Int): ComponentData? = database.transactional {
        preparedStatement(
            """
            select lifetime_type, component_type, one_use, users, roles, permissions, group_id
            from bc_component
                     natural left join bc_component_constraints
                     natural left join bc_component_component_group
            where component_id = ?""".trimIndent()
        ) {
            val dbResult = executeQuery(id).readOnce() ?: return null

            val lifetimeType = LifetimeType.fromId(dbResult["lifetime_type"])
            val componentType = ComponentType.fromId(dbResult["component_type"])
            val oneUse: Boolean = dbResult["one_use"]
            val groupId: Int? = dbResult.getOrNull<Int>("group_id")

            if (componentType == ComponentType.GROUP) {
                return@preparedStatement getGroup(id, oneUse, groupId ?: throwInternal("Group didn't have an ID"))
            }

            val constraints = InteractionConstraints().apply {
                userList.addAll(dbResult.get<List<Long>>("users"))
                roleList.addAll(dbResult.get<List<Long>>("roles"))
                permissions += Permission.getPermissions(dbResult["permissions"])
            }

            when (lifetimeType) {
                LifetimeType.PERSISTENT -> getPersistentComponent(id, componentType, lifetimeType, oneUse, constraints, groupId)
                LifetimeType.EPHEMERAL -> getEphemeralComponent(id, componentType, lifetimeType, oneUse, constraints, groupId)
            }
        }
    }

    suspend fun insertGroup(group: ComponentGroupBuilder): Int = database.transactional {
        val groupId: Int = runCatching {
            preparedStatement(
                """
                insert into bc_component (component_type, lifetime_type, one_use)
                VALUES (?, ?, ?)
                returning component_id""".trimIndent()
            ) {
                executeQuery(ComponentType.GROUP.key, LifetimeType.PERSISTENT.key, group.oneUse).readOnce()!!.get<Int>("component_id")
            }
        }.onFailure {
            if (it is SQLException && it.errorCode == 23523) { //foreign_key_violation, the component does not exist
                rethrowUser("Attempted to put a group ID to an external component: ${it.message}", it)
            }
        }.getOrThrow()

        //TODO INSERT TIMEOUT

        group.componentIds.forEach { componentId ->
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
            executeQuery(group.componentIds.toTypedArray()).readOnce()!!["exists"]
        }

        if (hasTimeouts) {
            throwUser("Cannot put components inside groups if they have a timeout set")
        }

        return groupId
    }

    /** Returns additional deleted components */
    suspend fun deleteComponent(component: ComponentData): List<Int> = database.transactional {
        val additionalComponents = arrayListOf<Int>()
        if (component.groupId != null) {
            preparedStatement("delete from bc_component c using bc_component_component_group g where g.group_id = ? returning c.component_id") {
                additionalComponents += executeQuery(component.groupId).map { it["component_id"] }
            }
        }

        // Also delete the group
        preparedStatement("delete from bc_component where component_id = ?") {
            executeUpdate(component.componentId)
        }

        return@transactional additionalComponents
    }

    context(Transaction)
    private suspend fun getPersistentComponent(
        id: Int,
        componentType: ComponentType,
        lifetimeType: LifetimeType,
        oneUse: Boolean,
        constraints: InteractionConstraints,
        groupId: Int?
    ): PersistentComponentData = preparedStatement(
        """
           select ph.handler_name         as handler_handler_name,
                  ph.user_data            as handler_user_data,
                  pt.expiration_timestamp as timeout_expiration_timestamp,
                  pt.handler_name         as timeout_handler_name,
                  pt.user_data            as timeout_user_data
           from bc_persistent_handler ph
                    left join bc_persistent_timeout pt using (component_id)
           where ph.component_id = ?;
        """.trimIndent()
    ) {
        val dbResult = executeQuery(id).readOnce() ?: throwInternal("Component seem to have been deleted in the same transaction")

        val handler = PersistentHandler(
            dbResult["handler_handler_name"],
            dbResult["handler_user_data"]
        )

        val timeout = dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
            PersistentTimeout(
                timestamp.toInstant().toKotlinInstant(),
                dbResult["timeout_handler_name"],
                dbResult["timeout_user_data"]
            )
        }

        PersistentComponentData(id, componentType, lifetimeType, oneUse, handler, timeout, constraints, groupId)
    }

    context(Transaction)
    private suspend fun getEphemeralComponent(
        id: Int,
        componentType: ComponentType,
        lifetimeType: LifetimeType,
        oneUse: Boolean,
        constraints: InteractionConstraints,
        groupId: Int?
    ): EphemeralComponentData = preparedStatement(
        """
            select ph.handler_id           as handler_handler_id,
                   pt.expiration_timestamp as timeout_expiration_timestamp,
                   pt.handler_id           as timeout_handler_id
            from bc_ephemeral_handler ph
                     left join bc_ephemeral_timeout pt using (component_id)
            where component_id = ?;
        """.trimIndent()
    ) {
        val dbResult = executeQuery(id).readOnce() ?: throwInternal("Component seem to have been deleted in the same transaction")

        val handler = dbResult.get<Long>("handler_handler_id").let { handlerId ->
            ephemeralComponentHandlers[handlerId]
                ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
        }

        val timeout = dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
            EphemeralTimeout(
                timestamp.toInstant().toKotlinInstant(),
                dbResult.get<Long>("timeout_handler_id").let { handlerId ->
                    ephemeralTimeoutHandlers[handlerId]
                        ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
                }
            )
        }

        EphemeralComponentData(id, componentType, lifetimeType, oneUse, handler, timeout, constraints, groupId)
    }

    context(Transaction)
            private suspend fun getGroup(id: Int, oneUse: Boolean, groupId: Int): ComponentGroupData {
        val timeout = preparedStatement(
            """
               select pt.expiration_timestamp as timeout_expiration_timestamp,
                      pt.handler_name         as timeout_handler_name,
                      pt.user_data            as timeout_user_data
               from bc_persistent_timeout pt
               where component_id = ?;
            """.trimIndent()
        ) {
            val dbResult = executeQuery(id).readOnce() ?: throwInternal("Component seem to have been deleted in the same transaction")

            dbResult.getOrNull<Timestamp>("timeout_expiration_timestamp")?.let { timestamp ->
                PersistentTimeout(
                    timestamp.toInstant().toKotlinInstant(),
                    dbResult["timeout_handler_name"],
                    dbResult["timeout_user_data"]
                )
            }
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

        return ComponentGroupData(id, oneUse, timeout, groupId, componentIds)
    }

    private fun cleanupEphemeral() = runBlocking {
        database.transactional {
            preparedStatement("delete from bc_component where lifetime_type = 1") {
                val deletedRows = executeUpdate()
                logger.trace { "Deleted $deletedRows ephemeral components" }
            }
        }
    }

    private fun Instant.toSqlTimestamp(): Timestamp? = Timestamp.from(this.toJavaInstant())
}