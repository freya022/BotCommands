package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.db.Transaction
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.handler.EphemeralComponentHandlers
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler
import io.github.freya022.botcommands.internal.utils.throwInternal

@BService
@RequiresComponents
internal class ComponentHandlerRepository(
    private val ephemeralComponentHandlers: EphemeralComponentHandlers
) {
    context(Transaction)
    internal suspend fun getPersistentHandler(id: Int): PersistentHandler? {
        return preparedStatement(
            "SELECT handler_name, user_data FROM bc_persistent_handler WHERE component_id = ?"
        ) {
            val dbResult = executeQuery(id).readOrNull() ?: return@preparedStatement null

            PersistentHandler.fromData(
                dbResult["handler_name"],
                dbResult["user_data"]
            )
        }
    }

    context(Transaction)
    internal suspend fun insertPersistentHandler(componentId: Int, handler: PersistentHandler) {
        preparedStatement("INSERT INTO bc_persistent_handler (component_id, handler_name, user_data) VALUES (?, ?, ?)") {
            executeUpdate(componentId, handler.handlerName, handler.userData.toTypedArray())
        }
    }

    context(Transaction)
    internal suspend fun getEphemeralHandler(id: Int): EphemeralHandler<*>? {
        return preparedStatement(
            "SELECT handler_id FROM bc_ephemeral_handler WHERE component_id = ?"
        ) {
            val dbResult = executeQuery(id).readOrNull() ?: return@preparedStatement null

            dbResult.getInt("handler_id").let { handlerId ->
                ephemeralComponentHandlers[handlerId]
                    ?: throwInternal("Unable to find ephemeral handler with id $handlerId")
            }
        }
    }

    context(Transaction)
    internal suspend fun insertEphemeralHandler(componentId: Int, handler: EphemeralHandler<*>) {
        preparedStatement("INSERT INTO bc_ephemeral_handler (component_id, handler_id) VALUES (?, ?)") {
            executeUpdate(componentId, ephemeralComponentHandlers.put(handler))
        }
    }
}