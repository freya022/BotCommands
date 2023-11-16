package io.github.freya022.botcommands.api.core.db.query

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import java.sql.Connection
import java.sql.PreparedStatement

@InterfacedService(acceptMultiple = true)
interface ParametrizedQueryFactory<R : ParametrizedQuery> {
    fun isSupported(connection: Connection): Boolean

    fun get(preparedStatement: PreparedStatement, sql: String): R
}