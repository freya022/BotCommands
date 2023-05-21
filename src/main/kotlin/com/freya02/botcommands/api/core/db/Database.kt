package com.freya02.botcommands.api.core.db

import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.api.core.config.BConfig
import org.intellij.lang.annotations.Language
import java.sql.SQLException

@InjectedService("Requires a ConnectionSupplier service")
interface Database {
    val config: BConfig

    @Throws(SQLException::class)
    suspend fun fetchConnection(readOnly: Boolean = false): KConnection
}

@Throws(SQLException::class)
suspend inline fun <R> Database.transactional(readOnly: Boolean = false, block: Transaction.() -> R): R {
    val connection = fetchConnection(readOnly)

    try {
        connection.autoCommit = false

        return block(Transaction(this, connection)).also { connection.commit() }
    } catch (e: Throwable) {
        connection.rollback()
        throw e
    } finally {
        connection.autoCommit = true
        connection.close()
    }
}

@Throws(SQLException::class)
suspend inline fun <R> Database.withConnection(readOnly: Boolean = false, block: KConnection.() -> R): R {
    return fetchConnection(readOnly).use(block)
}

@Throws(SQLException::class)
@Suppress("MemberVisibilityCanBePrivate")
suspend inline fun <R> Database.preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, block: KPreparedStatement.() -> R): R {
    return withConnection(readOnly) {
        preparedStatement(sql, block)
    }
}