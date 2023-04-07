package com.freya02.botcommands.api.core.db

import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BConfig
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import java.sql.SQLException

@ConditionalService(dependencies = [ConnectionSupplier::class])
class Database internal constructor(private val connectionSupplier: ConnectionSupplier, internal val config: BConfig) {
    init {
        runBlocking {
            preparedStatement("select version from bc_version", readOnly = true) {
                val rs = executeQuery(*emptyArray()).readOnce() ?:
                    throw IllegalStateException("No version found, please create the BotCommands tables with the 'sql/CreateDatabase.sql' file")

                val version = rs.getString("version")

                if (version != latestVersion) {
                    throw IllegalStateException("The current database version is '$version' and the version needed is '$latestVersion', please upgrade/downgrade the database with the help of the migration scripts, don't forget about backups if needed")
                }
            }
        }
    }

    @Throws(SQLException::class)
    fun fetchConnection(readOnly: Boolean = false): KConnection = KConnection(this, connectionSupplier.connection).also {
        it.isReadOnly = readOnly
    }

    @Throws(SQLException::class)
    inline fun <R> transactional(readOnly: Boolean = false, block: Transaction.() -> R): R {
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
    inline fun <R> withConnection(readOnly: Boolean = false, block: KConnection.() -> R): R {
        return fetchConnection(readOnly).use(block)
    }

    @Throws(SQLException::class)
    @Suppress("MemberVisibilityCanBePrivate")
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, block: KPreparedStatement.() -> R): R {
        return withConnection(readOnly) {
            preparedStatement(sql, block)
        }
    }

    companion object {
        private const val latestVersion = "3.0.0-alpha.1" // Change in CreateDatabase.sql too
    }
}