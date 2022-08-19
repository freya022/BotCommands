package com.freya02.botcommands.internal.components.sql

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.freya02.botcommands.api.components.ComponentType
import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.components.builder.PersistentComponentTimeoutInfo
import com.freya02.botcommands.internal.core.db.DBResult
import com.freya02.botcommands.internal.core.db.Transaction
import kotlinx.coroutines.runBlocking
import java.util.*

internal class SQLPersistentComponentData private constructor(
    componentId: String,
    groupId: Long,
    oneUse: Boolean,
    interactionConstraints: InteractionConstraints,
    expirationTimestamp: Long,
    val handlerName: String,
    val args: Array<String>
) : SQLComponentData(componentId, groupId, oneUse, interactionConstraints, expirationTimestamp) {
    override fun toString(): String {
        return "SQLPersistentComponentData(handlerName='$handlerName', args=${args.contentToString()}) ${super.toString()}"
    }

    companion object {
        private val mapper = ObjectMapper()
        private fun writeStringArray(strings: Array<out String>): String = try {
            mapper.writeValueAsString(strings)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Unable to serialize data: " + strings.contentToString(), e)
        }

        private fun readStringArray(json: String): Array<String> = try {
            mapper.readValue(json, Array<String>::class.java)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Unable to deserialize data: $json", e)
        }

        fun fromFetchedComponent(fetchedComponent: SQLFetchedComponent) = fromResult(fetchedComponent.resultSet)

        private fun fromResult(resultSet: DBResult): SQLPersistentComponentData {
            return SQLPersistentComponentData(
                resultSet["component_id"],
                resultSet["group_id"],
                resultSet["one_use"],
                InteractionConstraints.fromJson(resultSet["constraints"]),
                resultSet["expiration_timestamp"],
                resultSet["handler_name"],
                readStringArray(resultSet["args"])
            )
        }

        context(Transaction)
        suspend fun read(componentId: String?): SQLPersistentComponentData? {
            return preparedStatement(
                """
                select *
                from bc_persistent_component_data
                         join bc_component_data using (component_id)
                where component_id = ?
                limit 1""".trimIndent()
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
            timeout: PersistentComponentTimeoutInfo,
            handlerName: String?,
            args: Array<String>
        ): String = runBlocking {
            allocateComponent(type, oneUse, constraints, timeout).also { componentId ->
                preparedStatement(
                    """
                    insert into bc_persistent_component_data (component_id, handler_name, args)
                    values (?, ?, ?);""".trimIndent()
                ) {
                    executeUpdate(componentId, handlerName, writeStringArray(args))
                }
            }
        }
    }
}