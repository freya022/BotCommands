package com.freya02.botcommands.internal.core.db

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.Dependencies
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.db.Database
import com.freya02.botcommands.api.core.db.preparedStatement
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.sql.Connection

@BService
@ServiceType([Database::class])
@Dependencies([ConnectionSupplier::class])
internal class DatabaseImpl internal constructor(
    private val connectionSupplier: ConnectionSupplier,
    override val config: BConfig
) : Database {
    //Prevents deadlock when a paused coroutine holds a Connection,
    // but cannot be resumed and freed because of the coroutine scope being full (from another component event)
    private val semaphore = Semaphore(connectionSupplier.maxConnections)

    init {
        runBlocking {
            preparedStatement("select version from bc.bc_version", readOnly = true) {
                val rs = executeQuery(*emptyArray()).readOnce() ?:
                    throw IllegalStateException("No version found, please create the BotCommands tables with the migration scripts in 'bc_database_scripts', in the resources folder")

                val version = rs.getString("version")

                if (version != latestVersion) {
                    throw IllegalStateException("The current database version is '$version' and the version needed is '$latestVersion', please upgrade/downgrade the database with the help of the migration scripts, don't forget about backups if needed")
                }
            }
        }
    }

    override suspend fun fetchConnection(readOnly: Boolean): Connection = semaphore.withPermit {
        connectionSupplier.connection.also {
            it.isReadOnly = readOnly
        }
    }

    companion object {
        private const val latestVersion = "3.0.0-alpha.7" // Change in base migration script too
    }
}