package com.freya02.botcommands.internal.components.sql

import com.freya02.botcommands.api.components.ComponentType
import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.components.builder.LambdaComponentTimeoutInfo
import com.freya02.botcommands.core.internal.db.DBResult
import com.freya02.botcommands.core.internal.db.Transaction
import kotlinx.coroutines.runBlocking

internal class SQLLambdaComponentData private constructor(
    componentId: String,
    groupId: Long,
    oneUse: Boolean,
    interactionConstraints: InteractionConstraints,
    expirationTimestamp: Long,
    val handlerId: Long
) : SQLComponentData(componentId, groupId, oneUse, interactionConstraints, expirationTimestamp) {
    override fun toString(): String {
        return "SQLLambdaComponentData(handlerId=$handlerId) ${super.toString()}"
    }

    companion object {
        fun fromFetchedComponent(fetchedComponent: SQLFetchedComponent) = fromResult(fetchedComponent.resultSet)

        private fun fromResult(resultSet: DBResult): SQLLambdaComponentData = SQLLambdaComponentData(
            resultSet["component_id"],
            resultSet["group_id"],
            resultSet["one_use"],
            InteractionConstraints.fromJson(resultSet["constraints"]),
            resultSet["expiration_timestamp"],
            resultSet["handler_id"]
        )

        context(Transaction)
        suspend fun read(componentId: String): SQLLambdaComponentData? {
            return preparedStatement(
                """
                select *
                from bc_lambda_component_data
                         join bc_component_data using (component_id)
                where component_id = ?
                limit 1;""".trimIndent()
            ) {
                when (val resultSet = executeQuery(*arrayOf(componentId)).readOnce()) {
                    null -> null
                    else -> fromResult(resultSet)
                }
            }
        }

        context(Transaction)
        fun create(
            type: ComponentType,
            oneUse: Boolean,
            constraints: InteractionConstraints,
            timeout: LambdaComponentTimeoutInfo
        ): SQLLambdaCreateResult = runBlocking {
            val componentId = allocateComponent(type, oneUse, constraints, timeout)

            preparedStatement(
                """
                insert into bc_lambda_component_data (component_id)
                values (?)
                returning handler_id;""".trimIndent()
            ) {
                when (executeQuery(*arrayOf(componentId)).readOnce()) {
                    null -> throw IllegalStateException("Lambda component insert into didn't return the handler id")
                    else -> SQLLambdaCreateResult(componentId, resultSet.getLong("handler_id"))
                }
            }
        }
    }
}