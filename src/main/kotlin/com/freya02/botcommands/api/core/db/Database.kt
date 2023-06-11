package com.freya02.botcommands.api.core.db

import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.SQLException

/**
 * Utility class to use connections given by the [ConnectionSupplier].
 *
 * **Note:** The framework uses its own `bc` schema internally,
 * you will need to create the schema if this is the first time you use the library.
 *
 * **Note 2:** If you use Flyway to manage your database, you can manage the framework's tables with the following snippet:
 * ```kt
 * Flyway.configure()
 *      .dataSource(source)
 *      .schemas("bc")
 *      .locations("bc_database_scripts")
 *      .validateMigrationNaming(true)
 *      .loggers("slf4j")
 *      .load()
 *      .migrate()
 * ```
 * In this case, you do not have to create the `bc` schema manually.
 */
@InjectedService("Requires a ConnectionSupplier service")
interface Database {
    val config: BConfig

    @Throws(SQLException::class)
    suspend fun fetchConnection(readOnly: Boolean = false): Connection

    //TODO java methods
}

@Throws(SQLException::class)
suspend inline fun <R> Database.transactional(readOnly: Boolean = false, block: Transaction.() -> R): R {
    val connection = fetchConnection(readOnly)

    try {
        connection.autoCommit = false
        return block(Transaction(this, connection))
    } catch (e: Throwable) {
        connection.rollback()
        throw e
    } finally {
        // Always commit, if the connection was rolled back, this is a no-op
        // Using a "finally" block is mandatory, as code after the "block" function can be skipped if the user does a non-local return
        connection.commit()
        // HikariCP already resets the properties of the Connection when releasing (closing) it.
        // If a connection pool isn't used then it'll simply recreate a new Connection anyway.
        connection.close()
    }
}

@Throws(SQLException::class)
@Suppress("MemberVisibilityCanBePrivate")
suspend inline fun <R> Database.preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, block: KPreparedStatement.() -> R): R {
    return fetchConnection(readOnly).use {
        KPreparedStatement(this, it.prepareStatement(sql)).use(block)
    }
}