package io.github.freya022.botcommands.internal.core.db.query

import io.github.freya022.botcommands.api.core.db.query.ParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement

internal object NonParametrizedQueryFactory : ParametrizedQueryFactory<NonParametrizedQueryFactory.NonParametrizedQuery> {
    internal class NonParametrizedQuery internal constructor(private val sql: String) : ParametrizedQuery {
        override fun clear() { }
        override fun addValue(index: Int, value: Any?) { }

        override fun toSql(): String = removeCommentsAndInline(sql)
    }

    override fun isSupported(connection: Connection, databaseMetaData: DatabaseMetaData): Boolean = true

    override fun get(preparedStatement: PreparedStatement, sql: String): NonParametrizedQuery = NonParametrizedQuery(sql)
}