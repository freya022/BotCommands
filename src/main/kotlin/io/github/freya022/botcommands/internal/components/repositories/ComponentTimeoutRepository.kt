package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.db.Transaction
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.data.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.PersistentTimeout
import io.github.freya022.botcommands.internal.components.timeout.EphemeralTimeoutHandlers
import io.github.freya022.botcommands.internal.utils.throwInternal

@BService
@RequiresComponents
internal class ComponentTimeoutRepository(
    private val ephemeralTimeoutHandlers: EphemeralTimeoutHandlers
) {
    context(Transaction)
    internal suspend fun getPersistentTimeout(id: Int): PersistentTimeout? {
        return preparedStatement(
            "SELECT handler_name, user_data FROM bc_persistent_timeout WHERE component_id = ?"
        ) {
            val dbResult = executeQuery(id).readOrNull() ?: return@preparedStatement null

            PersistentTimeout.fromData(
                dbResult["handler_name"],
                dbResult["user_data"]
            )
        }
    }

    context(Transaction)
    internal suspend fun insertPersistentTimeout(componentId: Int, timeout: PersistentTimeout) {
        preparedStatement("INSERT INTO bc_persistent_timeout (component_id, handler_name, user_data) VALUES (?, ?, ?)") {
            executeUpdate(componentId, timeout.handlerName, timeout.userData.toTypedArray())
        }
    }

    context(Transaction)
    internal suspend fun getEphemeralTimeout(id: Int): EphemeralTimeout? {
        return preparedStatement(
            "SELECT handler_id FROM bc_ephemeral_timeout WHERE component_id = ?"
        ) {
            val dbResult = executeQuery(id).readOrNull() ?: return@preparedStatement null

            val handlerId: Int = dbResult["handler_id"]
            return EphemeralTimeout(
                ephemeralTimeoutHandlers[handlerId]
                    ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
            )
        }
    }

    context(Transaction)
    internal suspend fun insertEphemeralTimeout(componentId: Int, timeout: EphemeralTimeout) {
        preparedStatement("INSERT INTO bc_ephemeral_timeout (component_id, handler_id) VALUES (?, ?)") {
            executeUpdate(componentId, timeout.handler.let(ephemeralTimeoutHandlers::put))
        }
    }
}