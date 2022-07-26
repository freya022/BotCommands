package com.freya02.botcommands.core.internal.db

import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.api.config.BConfig
import org.intellij.lang.annotations.Language
import java.sql.Connection

private const val latestVersion = 3

@BService //TODO conditional service instancing
internal class Database internal constructor(private val config: BConfig) {
    init {
        config.connectionProvider.get().use { conn ->
            conn.prepareStatement("select version from version").use {
                if (!it.resultSet.next())
                    throw IllegalStateException("No version found, please create the BotCommands tables with the 'sql/CreateDatabase.sql' file")

                val version = it.resultSet.getInt("version")

                if (version != latestVersion) {
                    throw IllegalStateException("The current database version is '$version' and the version needed is '$latestVersion', please upgrade/downgrade the database with the help of the migration scripts")
                }
            }
        }
    }

    fun fetchConnection(): Connection = config.connectionProvider.get()

    suspend fun <R> transactional(block: suspend Transaction.() -> R): R {
        val connection = fetchConnection()

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

    suspend fun <R> preparedStatement(@Language("PostgreSQL") sql: String, block: suspend KPreparedStatement.() -> R): R {
        return fetchConnection().use { connection -> block(KPreparedStatement(this, connection.prepareStatement(sql))) }
    }
}