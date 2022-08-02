package com.freya02.botcommands.internal.components.sql

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.components.ComponentErrorReason
import com.freya02.botcommands.api.components.ComponentType
import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.components.builder.ComponentTimeoutInfo
import com.freya02.botcommands.core.internal.db.Transaction
import com.freya02.botcommands.core.internal.db.isUniqueViolation
import com.freya02.botcommands.internal.components.HandleComponentResult
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.Utils
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.sql.SQLException

internal abstract class SQLComponentData(
    private val componentId: String,
    private val groupId: Long,
    private val isOneUse: Boolean,
    private val interactionConstraints: InteractionConstraints,
    private val expirationTimestamp: Long
) {
    fun delete(transaction: Transaction) = runBlocking {
        with(transaction) {
            when {
                groupId > 0 -> preparedStatement("delete from bc_component_data where group_id = ?;") {
                    val i = executeUpdate(groupId)
                    logger.trace("Deleted {} components from group {}", i, groupId)
                }
                else -> preparedStatement("delete from bc_component_data where component_id = ?;") {
                    executeUpdate(*arrayOf(componentId))
                }
            }
        }
    }

    fun handleComponentData(event: GenericComponentInteractionCreateEvent): HandleComponentResult {
        val oneUse = isOneUse || groupId > 0

        if (expirationTimestamp > 0 && System.currentTimeMillis() > expirationTimestamp) {
            return HandleComponentResult(ComponentErrorReason.EXPIRED, true)
        }

        return when {
            checkConstraints(event, interactionConstraints) -> HandleComponentResult(null, oneUse)
            else -> HandleComponentResult(ComponentErrorReason.NOT_ALLOWED, false)
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

    override fun toString(): String {
        return "SQLComponentData(componentId='$componentId', groupId=$groupId, isOneUse=$isOneUse, interactionConstraints=$interactionConstraints, expirationTimestamp=$expirationTimestamp)"
    }

    companion object {
        private val logger = Logging.getLogger()

        context(Transaction)
        @JvmStatic
        protected suspend fun allocateComponent(
            type: ComponentType,
            oneUse: Boolean,
            constraints: InteractionConstraints,
            timeout: ComponentTimeoutInfo
        ): String {
            val timeoutMillis = timeout.toMillis()
            val expirationTimestamp = if (timeoutMillis == 0L) 0 else System.currentTimeMillis() + timeoutMillis

            for (count in 1..10) {
                val randomId = Utils.randomId(64)

                try {
                    preparedStatement(
                        """
                        insert into bc_component_data (type, component_id, one_use, constraints, expiration_timestamp)
                        values (?, ?, ?, ?, ?);""".trimIndent()
                    ) {
                        execute(type.key, randomId, oneUse, constraints.toJson(), expirationTimestamp)
                    }
                } catch (ex: SQLException) {
                    when {
                        !ex.isUniqueViolation() -> {} //ID already exists
                        else -> throw ex
                    }
                }

                return randomId
            }

            throwUser("Unable to generate a component ID, this may indicate that there are no more IDs left")
        }
    }
}