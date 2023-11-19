package io.github.freya022.botcommands.api.core.db

import org.intellij.lang.annotations.Language
import java.sql.Connection

class Transaction @PublishedApi internal constructor(val connection: Connection) {
    /**
     * Creates a statement from the given SQL statement, runs the [block] and closes the statement.
     */
    @Suppress("SqlSourceToSinkFlow")
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, block: SuspendingPreparedStatement.() -> R): R {
        return SuspendingPreparedStatement(connection.prepareStatement(sql)).use(block)
    }
}