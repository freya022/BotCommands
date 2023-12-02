package io.github.freya022.botcommands.internal.core.db.query

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import io.github.freya022.botcommands.api.core.db.query.AbstractParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.ServiceStart
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.Array as SqlArray

@BService(start = ServiceStart.LAZY)
internal object PostgresParametrizedQueryFactory : ParametrizedQueryFactory<PostgresParametrizedQueryFactory.PostgresParametrizedQuery> {
    internal class PostgresParametrizedQuery internal constructor(
        preparedStatement: PreparedStatement,
        private val rawSql: String
    ) : AbstractParametrizedQuery(preparedStatement) {
        private val values: TIntObjectMap<String> = TIntObjectHashMap()

        override fun clear() = values.clear()

        // The PostgreSQL driver won't replace all parameters, such as arrays.
        // Attempt to replace those with our own representation
        override fun addValue(index: Int, value: Any?) {
            if (value == null) return
            if (value.javaClass.isArray || value is SqlArray) {
                values.put(index, formatParameter(value))
            }
        }

        override fun toSql(): String {
            val cleanedStatement = removeCommentsAndInline(preparedStatement.toString())
            if (values.isEmpty)
                return cleanedStatement

            // If there is a logic error and the number of parameters doesn't match,
            // such as a parameter type not handled by the driver, neither by us nor the driver,
            // then the original query is used.
            // #addValue would need to be fixed
            val unresolvedParameterIndices = cleanedStatement.mapIndexedNotNull { index, c -> index.takeIf { c == '?' } }
            if (values.size() != unresolvedParameterIndices.size) {
                if (warnedQueries.add(rawSql))
                    logger.warn { "Unresolvable parameter in query '$cleanedStatement'" }
                return cleanedStatement
            }

            // Convert parameter indexes to array indexes
            val values = values.values(arrayOfNulls(values.size()))
            val builder = StringBuilder(cleanedStatement)
            // Traverse in reverse order so replaced values dont offset other values
            for (i in unresolvedParameterIndices.size - 1 downTo 0) {
                val indice = unresolvedParameterIndices[i]
                val value = values[i] ?: "?"

                builder.replace(indice, indice + 1, value)
            }

            return builder.toString()
        }

        companion object {
            private val logger = KotlinLogging.logger { }
            private val warnedQueries: MutableSet<String> = hashSetOf()
        }
    }

    override fun isSupported(connection: Connection, databaseMetaData: DatabaseMetaData): Boolean =
        databaseMetaData.driverName == "PostgreSQL JDBC Driver"

    override fun get(preparedStatement: PreparedStatement, sql: String): PostgresParametrizedQuery =
        PostgresParametrizedQuery(preparedStatement.unwrap(PreparedStatement::class.java), sql)
}