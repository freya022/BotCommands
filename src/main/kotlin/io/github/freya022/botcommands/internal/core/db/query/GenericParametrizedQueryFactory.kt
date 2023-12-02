package io.github.freya022.botcommands.internal.core.db.query

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import io.github.freya022.botcommands.api.core.db.query.AbstractParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement

internal object GenericParametrizedQueryFactory : ParametrizedQueryFactory<GenericParametrizedQueryFactory.GenericParametrizedQuery> {
    internal class GenericParametrizedQuery internal constructor(
        preparedStatement: PreparedStatement,
        private val sql: String
    ): AbstractParametrizedQuery(preparedStatement) {
        private val values: TIntObjectMap<String> = TIntObjectHashMap()

        override fun clear() = values.clear()

        override fun addValue(index: Int, value: Any?) {
            values.put(index, formatParameter(value))
        }

        override fun toSql(): String {
            val builder = StringBuilder(removeCommentsAndInline(sql))

            var paramIndex = 1
            var builderIndex = 0
            while (builder.indexOf("?", builderIndex).also { builderIndex = it } >= 0) {
                val value = values[paramIndex] ?: "?"

                builder.replace(builderIndex, builderIndex + 1, value)
                builderIndex++ //as to not keep seeking the same character
                paramIndex++
            }

            return builder.toString()
        }
    }

    override fun isSupported(connection: Connection, databaseMetaData: DatabaseMetaData): Boolean = true

    override fun get(preparedStatement: PreparedStatement, sql: String): GenericParametrizedQuery = GenericParametrizedQuery(preparedStatement, sql)
}