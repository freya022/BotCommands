package io.github.freya022.botcommands.api.core.db

import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.Statement

class Transaction @PublishedApi internal constructor(val connection: Connection) {
    /**
     * Creates a statement from the given SQL statement, runs the [block] and closes the statement.
     *
     * The returned keys are accessible using [getGeneratedKeys][AbstractPreparedStatement.getGeneratedKeys].
     *
     * @param sql           An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param generatedKeys `true` to return the generated keys
     *
     * @see Connection.prepareStatement
     */
    @Suppress("SqlSourceToSinkFlow")
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, generatedKeys: Boolean = false, block: SuspendingPreparedStatement.() -> R): R {
        return SuspendingPreparedStatement(connection.prepareStatement(
            sql,
            if (generatedKeys) Statement.RETURN_GENERATED_KEYS else Statement.NO_GENERATED_KEYS
        )).use(block)
    }

    /**
     * Creates a statement from the given SQL statement, runs the [block] and closes the statement.
     *
     * The returned keys are accessible using [getGeneratedKeys][AbstractPreparedStatement.getGeneratedKeys].
     *
     * @param sql           An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows
     *
     * @see Connection.prepareStatement
     */
    @Suppress("SqlSourceToSinkFlow")
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, columnIndexes: IntArray, block: SuspendingPreparedStatement.() -> R): R {
        return SuspendingPreparedStatement(connection.prepareStatement(sql, columnIndexes)).use(block)
    }

    /**
     * Creates a statement from the given SQL statement, runs the [block] and closes the statement.
     *
     * The returned keys are accessible using [getGeneratedKeys][AbstractPreparedStatement.getGeneratedKeys].
     *
     * @param sql         An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnNames An array of column names indicating the columns that should be returned from the inserted row or rows
     *
     * @see Connection.prepareStatement
     */
    @Suppress("SqlSourceToSinkFlow")
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, columnNames: Array<out String>, block: SuspendingPreparedStatement.() -> R): R {
        return SuspendingPreparedStatement(connection.prepareStatement(sql, columnNames)).use(block)
    }
}