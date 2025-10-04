package io.github.freya022.botcommands.internal.core.db.query

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import io.github.freya022.botcommands.api.core.db.query.AbstractParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.internal.core.exceptions.internalErrorMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement

/**
 * This lets pgjdbc fill out the parameters by itself,
 * then we replace those it hasn't replaced.
 */
@Lazy
@BService
internal object PostgresParametrizedQueryFactory : ParametrizedQueryFactory<PostgresParametrizedQueryFactory.PostgresParametrizedQuery> {
    internal class PostgresParametrizedQuery internal constructor(
        preparedStatement: PreparedStatement,
        private val rawSql: String
    ) : AbstractParametrizedQuery(preparedStatement) {
        private val values: TIntObjectMap<Any?> = TIntObjectHashMap()

        override fun clear() = values.clear()

        override fun addValue(index: Int, value: Any?) {
            values.put(index, value)
        }

        override fun toSql(): String {
            if (values.isEmpty)
                return removeCommentsAndInline(rawSql)

            val queryParts = splitByQueryParameter(rawSql)
            val postgresQuery = preparedStatement.toString()

            // Rebuild the query using the split parts,
            // but for each part, check if there is a '?' after it,
            // if absent, it means postgres replaced it
            return removeCommentsAndInline(buildString {
                var lastIndex = 0
                // Drop last part as it has no query parameter after it
                for ((i, part) in queryParts.dropLast(1).withIndex()) {
                    val partIndex = postgresQuery.indexOf(part, lastIndex)
                    if (partIndex == -1) {
                        logger.error { internalErrorMessage("Could not find part #$i '${part}' in '$postgresQuery'") }
                        return removeCommentsAndInline(postgresQuery)
                    }

                    append(part)
                    if (postgresQuery[partIndex + part.length] == '?') {
                        // The driver did not replace the value, use our own replacement
                        append(formatParameter(values[i + 1]))
                        lastIndex = partIndex + part.length + 1
                    } else {
                        // The driver replaced the value, find where it stops based on the next part
                        val nextPart = queryParts[i + 1]
                        if (nextPart.isBlank()) { // When the query ends with a parameter, the last part is empty
                            append(postgresQuery.substring(partIndex + part.length))
                            break
                        }
                        val nextPartIndex = postgresQuery.indexOf(nextPart, partIndex)
                        append(postgresQuery.substring(partIndex + part.length, nextPartIndex))
                        lastIndex = nextPartIndex
                    }
                }

                append(queryParts.last())
            })
        }

        private fun splitByQueryParameter(query: String): List<String> = buildList {
            val builder = StringBuilder()
            var inComment = false
            for ((index, char) in query.withIndex()) {
                if (char == '?' && !inComment) {
                    add(builder.toString())
                    builder.clear()
                } else {
                    builder.append(char)
                    if (char == '-' && query.getOrNull(index + 1) == '-') // -- comment
                        inComment = true
                    if (char == '\n')
                        inComment = false
                }
            }
            add(builder.toString())
        }

        private companion object {

            private val logger = KotlinLogging.logger { }
        }
    }

    override fun isSupported(connection: Connection, databaseMetaData: DatabaseMetaData): Boolean =
        databaseMetaData.driverName == "PostgreSQL JDBC Driver"

    override fun get(preparedStatement: PreparedStatement, sql: String): PostgresParametrizedQuery =
        PostgresParametrizedQuery(preparedStatement.unwrap(PreparedStatement::class.java), sql)
}
