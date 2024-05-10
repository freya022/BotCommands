package io.github.freya022.botcommands.api.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("FunctionName")
class SuspendingPreparedStatement @PublishedApi internal constructor(
    preparedStatement: PreparedStatement
): AbstractPreparedStatement(preparedStatement) {
    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun execute(): Boolean = preparedStatement.execute()

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
    override fun executeUpdate(): Int = preparedStatement.executeUpdate()

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
    override fun executeQuery(): ResultSet = preparedStatement.executeQuery()

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

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeBatch(): IntArray = preparedStatement.executeBatch()

    /**
     * Submits a batch of commands to the database for execution and
     * if all commands execute successfully, returns an array of update counts.
     * The `Int` elements of the array that is returned are ordered
     * to correspond to the commands in the batch, which are ordered
     * according to the order in which they were added to the batch.
     *
     * @see PreparedStatement.executeBatch
     */
    suspend fun executeBatch_(): IntArray = withContext(Dispatchers.IO) {
        preparedStatement.executeBatch()
    }

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeLargeBatch(): LongArray = preparedStatement.executeLargeBatch()

    /**
     * Submits a batch of commands to the database for execution and
     * if all commands execute successfully, returns an array of update counts.
     * The `Long` elements of the array that is returned are ordered
     * to correspond to the commands in the batch, which are ordered
     * according to the order in which they were added to the batch.
     *
     * @see PreparedStatement.executeLargeBatch
     */
    suspend fun executeLargeBatch_(): LongArray = withContext(Dispatchers.IO) {
        preparedStatement.executeLargeBatch()
    }
}
