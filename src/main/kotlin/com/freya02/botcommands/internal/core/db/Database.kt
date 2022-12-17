package com.freya02.botcommands.internal.core.db

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.referenceString
import org.intellij.lang.annotations.Language
import java.sql.Connection

@ConditionalService
class Database internal constructor(internal val config: BConfig) {
    init {
        config.connectionProvider.get().use { conn ->
            conn.prepareStatement("select version from bc_version").use {
                it.executeQuery().use { rs ->
                    if (!rs.next())
                        throw IllegalStateException("No version found, please create the BotCommands tables with the 'sql/CreateDatabase.sql' file")

                    val version = rs.getString("version")

                    if (version != latestVersion) {
                        throw IllegalStateException("The current database version is '$version' and the version needed is '$latestVersion', please upgrade/downgrade the database with the help of the migration scripts, don't forget about backups if needed")
                    }
                }
            }
        }
    }

    fun fetchConnection(): Connection = config.connectionProvider.get()

    inline fun <R> transactional(block: Transaction.() -> R): R {
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

    companion object : ConditionalServiceChecker {
        private const val latestVersion = "3.0.0-alpha.1" // Change in CreateDatabase.sql too

        override fun checkServiceAvailability(context: BContext): String? {
            val config = (context as BContextImpl).getService<BConfig>()
            if (!config.hasConnectionProvider()) {
                return "${BConfig::connectionProvider.referenceString} needs to be set"
            }

            return null
        }
    }
}