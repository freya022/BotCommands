package io.github.freya022.botcommands.api.core.db.query

import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement

/**
 * Factory for [ParametrizedQuery].
 *
 * A parametrized query is only used if any of these conditions is met:
 * - [BDatabaseConfig.logQueries] is enabled,
 *   and the logger of the class that created the prepared statement has its `TRACE` logs enabled,
 * - [BDatabaseConfig.queryLogThreshold] is configured
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * ### Built-in RDBMS support
 * - PostgreSQL
 *
 * If no compatible factory is found, a generic parametrized query is used.
 *
 * @see ParametrizedQuery
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface ParametrizedQueryFactory<R : ParametrizedQuery> {
    /**
     * Returns whether this connection is supported by this parametrized query factory.
     *
     * @param connection          The database connection
     * @param databaseMetaData    The database metadata
     */
    fun isSupported(connection: Connection, databaseMetaData: DatabaseMetaData): Boolean

    /**
     * Returns a [ParametrizedQuery] for this statement.
     */
    fun get(preparedStatement: PreparedStatement, sql: String): R
}