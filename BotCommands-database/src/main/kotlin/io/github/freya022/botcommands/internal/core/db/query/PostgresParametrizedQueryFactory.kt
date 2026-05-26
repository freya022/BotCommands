package io.github.freya022.botcommands.internal.core.db.query

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.db.query.AbstractParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement

/**
 * This lets pgjdbc fill out the parameters by itself,
 * if a parameter is not replaced, it should be contributed to pgjdbc.
 */
@Lazy
@BService
@RequiresDatabase
internal class PostgresParametrizedQueryFactory : ParametrizedQueryFactory<PostgresParametrizedQueryFactory.PostgresParametrizedQuery> {
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

            return removeCommentsAndInline(preparedStatement.toString())
        }
    }

    override fun isSupported(connection: Connection, databaseMetaData: DatabaseMetaData): Boolean =
        databaseMetaData.driverName == "PostgreSQL JDBC Driver"

    override fun get(preparedStatement: PreparedStatement, sql: String): PostgresParametrizedQuery =
        PostgresParametrizedQuery(preparedStatement.unwrap(PreparedStatement::class.java), sql)
}
