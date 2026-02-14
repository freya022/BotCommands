package io.github.freya022.botcommands.internal.components.repositories.persistent

import io.github.freya022.botcommands.api.core.db.Transaction
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.mapToArray
import io.github.freya022.botcommands.internal.components.annotations.RequiresPersistentComponents
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.components.timeout.persistent.EphemeralTimeoutHandlers
import io.github.freya022.botcommands.internal.utils.throwInternal

@BService
@RequiresPersistentComponents
internal class ComponentTimeoutRepository(
    private val ephemeralTimeoutHandlers: EphemeralTimeoutHandlers
) {
    context(transaction: Transaction)
    internal suspend fun getPersistentTimeout(id: Int): PersistentTimeout? {
        return transaction.preparedStatement(
            "SELECT handler_name, user_data FROM bc_persistent_timeout WHERE component_id = ?"
        ) {
            val dbResult = executeQuery(id).readOrNull() ?: return@preparedStatement null

            PersistentTimeout.fromData(
                dbResult["handler_name"],
                dbResult["user_data"]
            )
        }
    }

    context(transaction: Transaction)
    internal suspend fun insertPersistentTimeout(componentId: Int, timeout: PersistentTimeout) {
        transaction.preparedStatement("INSERT INTO bc_persistent_timeout (component_id, handler_name, user_data) VALUES (?, ?, ?)") {
            executeUpdate(componentId, timeout.handlerName, timeout.userData.mapToArray { it?.asBytes() })
        }
    }

    context(transaction: Transaction)
    internal suspend fun getEphemeralTimeout(id: Int): EphemeralTimeout? {
        return transaction.preparedStatement(
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

    context(transaction: Transaction)
    internal suspend fun insertEphemeralTimeout(componentId: Int, timeout: EphemeralTimeout) {
        transaction.preparedStatement("INSERT INTO bc_ephemeral_timeout (component_id, handler_id) VALUES (?, ?)") {
            executeUpdate(componentId, timeout.handler.let(ephemeralTimeoutHandlers::put))
        }
    }
}
