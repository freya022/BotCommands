package io.github.freya022.botcommands.internal.core.db.query

import io.github.freya022.botcommands.api.core.db.query.ParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import java.sql.Connection
import java.sql.PreparedStatement

@BService
internal object PostgresParametrizedQueryFactory : ParametrizedQueryFactory<PostgresParametrizedQueryFactory.PostgresParametrizedQuery> {
    internal class PostgresParametrizedQuery internal constructor(private val preparedStatement: PreparedStatement) : ParametrizedQuery {
        override fun clear() { }
        override fun addValue(index: Int, value: Any?) { }

        override fun toSql(): String = preparedStatement.toString()
    }

    override fun isSupported(connection: Connection, databaseProductName: String): Boolean =
        databaseProductName.startsWith("PostgreSQL")

    override fun get(preparedStatement: PreparedStatement, sql: String): PostgresParametrizedQuery =
        PostgresParametrizedQuery(preparedStatement.unwrap(PreparedStatement::class.java))
}