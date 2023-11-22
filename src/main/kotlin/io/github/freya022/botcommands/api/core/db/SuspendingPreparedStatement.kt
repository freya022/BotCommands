package io.github.freya022.botcommands.api.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet

class SuspendingPreparedStatement @PublishedApi internal constructor(
    preparedStatement: PreparedStatement
): AbstractPreparedStatement(preparedStatement) {
    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun execute(): Boolean = throw UnsupportedOperationException()

    /**
     * Executes the SQL statement in this PreparedStatement object with the supplied parameters.
     *
     * The parameters are set in the order they are passed in,
     * supported types are implementation-specific,
     * see [PreparedStatement.setObject] and its implementation by your JDBC driver.
     *
     * @see PreparedStatement.execute
     */
    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.execute()
    }

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(): Int = throw UnsupportedOperationException()

    /**
     * Executes the SQL statement in this PreparedStatement object with the supplied parameters.
     *
     * The parameters are set in the order they are passed in,
     * supported types are implementation-specific,
     * see [PreparedStatement.setObject] and its implementation by your JDBC driver.
     *
     * @see PreparedStatement.executeUpdate
     */
    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.executeUpdate()
    }

    /**
     * Executes the SQL statement in this PreparedStatement object with the supplied parameters,
     * and returns a [DBResult] with the [generated keys][PreparedStatement.getGeneratedKeys].
     *
     * The parameters are set in the order they are passed in,
     * supported types are implementation-specific,
     * see [PreparedStatement.setObject] and its implementation by your JDBC driver.
     *
     * @see PreparedStatement.executeUpdate
     */
    suspend fun executeReturningUpdate(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.executeUpdate()
        generatedKeys
    }

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeQuery(): ResultSet = throw UnsupportedOperationException()

    /**
     * Executes the SQL statement in this PreparedStatement object with the supplied parameters.
     *
     * The parameters are set in the order they are passed in,
     * supported types are implementation-specific,
     * see [PreparedStatement.setObject] and its implementation by your JDBC driver.
     *
     * @see PreparedStatement.executeQuery
     */
    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        setParameters(params)
        DBResult(preparedStatement.executeQuery())
    }
}
